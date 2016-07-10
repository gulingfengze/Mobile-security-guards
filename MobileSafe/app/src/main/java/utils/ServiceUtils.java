package utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by Lenovo on 2016/6/19.
 * 判断服务的状态
 */
public class ServiceUtils {
public static final int SERVICENUM=50;

    /**
     * @param context
     * @param serviceName
     * service完整的名字（包名+类名）
     * @return
     * 该service是否在运行
     */
    public static boolean isServiceRunning(Context context,String serviceName) {
        boolean isRunning = false;
         //判断运行中的服务状态，ActivityManager
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
         //获取android手机中运行的所有服务
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(SERVICENUM);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            //判断服务的名字是否包含我们指定的服务名
            if (runningServiceInfo.service.getClassName().equals(serviceName)){
                //名字一致，该服务在运行中
                isRunning = true;
                //已经找到 退出循环
                break;
            }
        }

        return isRunning;
    }
}
