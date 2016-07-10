package service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import java.util.List;

/**
 * 该服务主要完成 锁屏注册广播 或则取消注册
 */
public class ClearTaskService extends Service {

    private ClearTaskReceiver receiver;
    private ActivityManager am;

    public ClearTaskService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private class ClearTaskReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            /*获取正在运行的所有进程*/
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo:runningAppProcesses){
                am.killBackgroundProcesses(runningAppProcessInfo.processName);//清理进程
                System.out.println("锁屏清理进程");
            }
        }
    }
    @Override
    public void onCreate() {

        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);  //获取ActivityManager
        /*注册广播*/
        receiver = new ClearTaskReceiver();
        IntentFilter filter=new IntentFilter(Intent.ACTION_SCREEN_OFF);//添加锁屏意图
        registerReceiver(receiver,filter);

        super.onCreate();

    }

    @Override
    public void onDestroy() {
        /*取消注册广播*/
        unregisterReceiver(receiver);
        super.onDestroy();

    }
}
