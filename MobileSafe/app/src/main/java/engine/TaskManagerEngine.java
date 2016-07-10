package engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import domain.TaskBean;

/**
 * Created by Lenovo on 2016/7/1.
 * 获取所有运行中的信息
 */
public class TaskManagerEngine {

    /**
     * @param context
     * @return 运行APK的数据
     */
    public static List<TaskBean> getAllRunningTaskInfos(Context context) {
        List<TaskBean> datas = new ArrayList<TaskBean>();
             /*获取包管理器*/
        PackageManager pm = context.getPackageManager();
            /*获取运行中的进程*/
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();


        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
            TaskBean bean = new TaskBean();
               /*APK包名*/
            String processName = runningAppProcessInfo.processName;
            bean.setPackName(processName);
            //有些进程是无名进程
            PackageInfo packageInfo = null;
            try {
               /*APK图标和名称*/
                packageInfo = pm.getPackageInfo(processName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                continue;//继续循环 不添加没有名字的进程

            }
            bean.setIcon(packageInfo.applicationInfo.loadIcon(pm));//APK图标
            bean.setName(packageInfo.applicationInfo.loadLabel(pm) + "");//APK名称
                /*判断是否是系统APK*/
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                bean.setIsSystem(true);//系统APK
            } else {
                bean.setIsSystem(false);//用户APK
            }
                /*获取占用内存大小*/
            Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});
            long totalPrivateDirty = processMemoryInfo[0].getTotalPrivateDirty() * 1024;// 获取运行中占用的内存
            bean.setMemSize(totalPrivateDirty);

            datas.add(bean);//添加进程信息
        }
        return datas;

    }

    /**
     * @param context
     * @return 获取可用内存大小
     */
    public static long getAvailMemSize(Context context) {
        long size = 0;
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        // MemoryInfo 存放内存的信息
        am.getMemoryInfo(outInfo);

        // 把kb 转换成byte
        size = outInfo.availMem;

        return size;
    }

    /**
     * @param context
     * @return 总内存大小
     */
    public static long getTotalMemSize(Context context) {
        long size = 0;
             /*读取配置文件获取总内存大小*/
        File file = new File("/proc/meminfo");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            //IO流：zip流、 pipe流

            String totalMemInfo = reader.readLine();

            int startIndex = totalMemInfo.indexOf(':');
            int endIndex = totalMemInfo.indexOf('k');
            // 单位是kb
            totalMemInfo = totalMemInfo.substring(startIndex + 1, endIndex)
                    .trim();
            size = Long.parseLong(totalMemInfo);
            size *= 1024;// byte单位
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;
    }

}
