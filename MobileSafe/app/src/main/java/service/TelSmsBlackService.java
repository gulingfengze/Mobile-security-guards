package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dao.BlackDao;
import domain.BlackTable;

/**
 * 监听电话和短信(黑名单)
 */
public class TelSmsBlackService extends Service {

    private SmsReceiver receiver;
    private BlackDao dao;
    private PhoneStateListener listener;
    private TelephonyManager tm;


    public TelSmsBlackService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    /**
     * 短信监听广播
     */
    private class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Object[] datas = (Object[]) intent.getExtras().get("pdus");
            for (Object sms : datas) {
                SmsMessage sm = SmsMessage.createFromPdu((byte[]) sms);//获取短信的数据
                String address = sm.getOriginatingAddress();//获取短信发件人号码
                /*进行判断：是否是黑名单中号码*/
                int mode = dao.getMode(address);
                if ((mode & BlackTable.SMS) != 0) {//短信号码是黑名单中的号码
                    abortBroadcast();//终止广播传递,拦截短信
                }
            }
        }
    }


    @Override
    public void onCreate() {
        /*初始化黑名单的业务类*/
        dao = new BlackDao(getApplicationContext());

        /*1.注册短信监听广播*/
        receiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");  //创建IntentFilter实例，并给其添加要接收的广播
        filter.setPriority(Integer.MAX_VALUE);//设置拦截模式为最高
        registerReceiver(receiver, filter);//注册短信广播
       /*2.注册电话监听广播(拦截电话出现问题，未解决)*/
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);//获取电话监听管理
        listener = new PhoneStateListener() {//监听电话的状态
            /*
                         * (non-Javadoc) 该方法用来监听电话的状态
                         *
                         * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
                         * java.lang.String)
                         */
            @Override
            public void onCallStateChanged(int state, final String incomingNumber) {
                /*stae 电话的状态    incomingNumber 打进来的号码*/
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE://挂断的状态
                        System.out.println("CALL_STATE_IDLE");
                        break;
                    case TelephonyManager.CALL_STATE_RINGING://响铃状态
                        System.out.println("CALL_STATE_RINGING");
                       /*判断是否是黑名单号码*/
                        int mode = dao.getMode(incomingNumber);
                        if ((mode & BlackTable.TEL) != 0) {
                            System.out.println("挂断电话");
                            //挂断电话之前先注册内容观察者
                            getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"),
                                    true, new ContentObserver(new Handler()) {
                                @Override
                                public void onChange(boolean selfChange) {//电话日志变化 触发此方法调用

                                    deleteCalllog(incomingNumber);//删除电话日志
                                    //取消内容观察者注册
                                    getContentResolver().unregisterContentObserver(this);
                                    super.onChange(selfChange);
                                }
                            });
                            endCall();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://通话状态
                        System.out.println("CALL_STATE_OFFHOOK");
                        break;
                    default:
                        break;
                }

                super.onCallStateChanged(state, incomingNumber);

            }
        };
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);//注册电话监听
        super.onCreate();
    }

    /**
     * 删除电话日志
     *
     * @param incomingNumber 要删除日志的号码
     */
    protected void deleteCalllog(String incomingNumber) {
        //只能内容提供者来删除电话日志
        Uri uri = Uri.parse("content://call_log/calls");
        //删除日志
        getContentResolver().delete(uri, "number=?", new String[]{incomingNumber});

    }

    /**
     * 挂断电话（未解决）
     */
    protected void endCall() {
        //ServiceManager.getService();想用该功能，采用反射调用方法实现
        //反射调用步骤：1 2 3 4
        //1.class
        try {
            Class clazz = Class.forName("android.os.ServiceManager");
            //2. method
            Method method = clazz.getDeclaredMethod("getService", String.class);

            //3.obj 不需要 静态方法
            //4. 调用
            IBinder binder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);



            //5.aidl  (出现问题)
           /* ITelephony iTelephony = ITelephony.Stub.asInterface(binder);
            iTelephony.endCall();//挂断电话*/



        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        /*取消短信监听*/
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
       /* 取消电话监听*/
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
