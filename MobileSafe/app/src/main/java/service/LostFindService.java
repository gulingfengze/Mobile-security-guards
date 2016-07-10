package service;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;

import mobilesafe.eoe.R;

/**
 * Created by Lenovo on 2016/6/19.
 */
public class LostFindService extends Service {

    private SmsReceiver receiver;
    private IntentFilter filter;
    private boolean isPlay;//false 音乐播放的标记

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 短信广播接收者
     */
    private class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //实现短信拦截功能
            Bundle extras = intent.getExtras();
            Object datas[] = (Object[]) extras.get("pdus");
            for (Object data : datas) {
                SmsMessage sm = SmsMessage.createFromPdu((byte[]) data);
                // System.out.println(sm.getMessageBody() + ":" + sm.getOriginatingAddress());
                      /*获取短信内容*/
                String mess = sm.getMessageBody();

                if (mess.equals("#*gps*#")) {//获取定位信息
                    /*定位功能放到服务中执行*/
                    Intent service = new Intent(context, LocationService.class);
                    startActivity(service);
                    abortBroadcast();//终止广播
                } else if (mess.equals("#*lockscreen*#")) {//一键锁屏
                    //获取设备管理器
                    DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                    dpm.resetPassword("123", 0);//设置密码
                    dpm.lockNow();//一键锁屏
                    abortBroadcast();//终止广播
                } else if (mess.equals("#*wipedata*#")) {//远程清除数据
                    //获取设备管理器
                    DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
                    dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);//清除SD卡数据
                    abortBroadcast();//终止广播
                } else if (mess.equals("#*music*#")) {
                    //只播放一次
                    abortBroadcast();
                    if (isPlay) {
                        return;
                    }
                    //播放音乐
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.warning);
                    mp.setVolume(1, 1);//设置左右声道声音为最大值
                    mp.start();//开始播放
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            isPlay = false;//音乐播放完毕，触发此方法
                        }
                    });
                    isPlay = true;
                }
            }
        }
    }
    @Override
    public void onCreate() {
        //创建SmsReceiver对象
        receiver = new SmsReceiver();
        //创建IntentFilter实例，并给其添加要接收的广播
        filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        //级别一样，清单文件，谁先注册谁先执行，如果级别一样，代码比清单要高
        filter.setPriority(Integer.MAX_VALUE);
        //注册短信监听
        registerReceiver(receiver, filter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        //取消注册短信监听广播
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
