package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import engine.PhoneLocationEngine;
import mobilesafe.eoe.R;
import utils.MyConstants;
import utils.SpTools;

/**
 * 该服务主要用于监控来电电话，并且显示归属地
 * Created by Lenovo on 2016/6/25.
 */
public class ComingPhoneService extends Service {
    private TelephonyManager tm;
    private PhoneStateListener listener;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private View view;
    private OutCallReceiver outCallReceiver;
    private boolean isOutCall=false;//是否是外拨电话
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 外拨电话的广播
     */
    private class OutCallReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
             isOutCall=true;//记录是外拨电话
          String phoneNumber=getResultData();//获取外拨电话号码
          showLocationToast(phoneNumber);//显示外拨电话的土司
        }
    }
    @Override
    public void onCreate() {
              //监控外拨 注册广播
        outCallReceiver = new OutCallReceiver();
       IntentFilter filter=new IntentFilter("android.intent.action.NEW_OUTGOING_CALL");//外拨广播
        registerReceiver(outCallReceiver,filter);//注册广播

        // 初始化窗体管理器
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 初始化土司的参数
        initToastParams();

       /*初始化电话状态监听*/
        //获取电话管理器
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //电话状态监听器(仅限于来电)
        listener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state){
                    case TelephonyManager.CALL_STATE_IDLE:// 空闲，挂断，初始会先执行
                        // 关闭土司
                        closeLocationToast();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:// 通话状态
                        // 关闭土司
                        closeLocationToast();
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:// 响铃状态
                        // 显示土司
                        showLocationToast(incomingNumber);// 显示归属地的土司
                        break;

                    default:
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        tm.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);//注册电话监听
        super.onCreate();
    }
    private void initToastParams() {
        // TODO Auto-generated method stub
        // XXX This should be changed to use a Dialog, with a Theme.Toast
        // defined that sets up the layout params appropriately.

        // 土司的初始化参数
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;

        //对齐方式左上角
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		/* | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE */
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        //初始化土司的位置
        params.x = (int) Float.parseFloat(SpTools.getString(getApplicationContext(), MyConstants.TOASTX, "0"));
        params.y = (int) Float.parseFloat(SpTools.getString(getApplicationContext(), MyConstants.TOASTY, "0"));
        params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;// 土司本身并不响应事件,所以进行改变类型
        params.setTitle("Toast");
    }
    int bgStyles[] = new int[]{R.drawable.call_locate_blue,R.drawable.call_locate_gray,R.drawable.call_locate_green,R.drawable.call_locate_orange,R.drawable.call_locate_white};
    /**
     * 显示土司
     * @param incomingNumber 电话号码
     */
    private void showLocationToast(String incomingNumber) {
        if (!isOutCall)//如果不是外拨电话，直接关闭
        closeLocationToast(); // 关闭土司

        view = View.inflate(getApplicationContext(), R.layout.sys_toast, null);
        int index = Integer.parseInt(SpTools.getString(getApplicationContext(), MyConstants.STYLEBGINDEX, "0"));
        view.setBackgroundResource(bgStyles[index]);
        TextView tv_location = (TextView) view
                .findViewById(R.id.tv_toast_location);
        tv_location.setText(PhoneLocationEngine.locationQuery(incomingNumber,
                getApplicationContext()));

        // 初始化view的触摸事件
        view.setOnTouchListener(new View.OnTouchListener() {

            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println(event.getX() + ":" + event.getRawX());
                // 拖动土司
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// 按下
                        startX = event.getRawX();
                        startY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:// 按下移动，拖动
                        //新的 x  y坐标
                        float moveX = event.getRawX();//移动后的x坐标
                        float moveY = event.getRawY();//移动后的x坐标

                        //dx x方向的位置变化值 dy y方向的位置变化值
                        float dx = moveX - startX;
                        float dy = moveY - startY;
                        //改变土司的坐标
                        params.x += dx;
                        params.y += dy;
                        //重新获取新的x y坐标
                        startX = moveX;
                        startY = moveY;

                        //更新土司的位置
                        wm.updateViewLayout(view, params);
                        break;
                    case MotionEvent.ACTION_UP:// 松开
                        //记录当前土司位置,把x y坐标值保存到sp中
                        if (params.x < 0) {
                            params.x = 0;
                        } else if (params.x + view.getWidth() > wm.getDefaultDisplay().getWidth()) {
                            params.x =  wm.getDefaultDisplay().getWidth() - view.getWidth();
                        }

                        if (params.y < 0) {
                            params.y = 0;
                        } else if (params.y + view.getHeight() > wm.getDefaultDisplay().getHeight()) {
                            params.y = wm.getDefaultDisplay().getHeight() - view.getHeight();
                        }
                        SpTools.putString(getApplicationContext(), MyConstants.TOASTX, params.x + "");
                        SpTools.putString(getApplicationContext(), MyConstants.TOASTY, params.y + "");

                    default:
                        break;
                }
                return false;
            }
        });
        wm.addView(view, params);
    }
private Handler handler=new Handler(){
    @Override
    public void handleMessage(Message msg) {
        if (view != null) {
            wm.removeView(view);
            view = null;// 删除完 置为null
        }
    }
};
    /**
     * 关闭土司
     */
    private void closeLocationToast() {
        if (isOutCall){
            new Thread(new Runnable() {//外拨电话延迟关闭土司
                @Override
                public void run() {
                    SystemClock.sleep(6000);
                    handler.obtainMessage().sendToTarget();//发送消息关闭土司
                }
            }).start();
            isOutCall=false;
        }else {
            // 初始先执行一次
            if (view != null) {
                wm.removeView(view);
                view = null;// 删除完 置为null
            }
        }
    }

    @Override
    public void onDestroy() {
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);// 取消电话监听
        unregisterReceiver(outCallReceiver);//监控外拨 取消注册广播
        super.onDestroy();
    }
}
