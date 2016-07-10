package engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import domain.AppBean;

/**
 * Created by Lenovo on 2016/6/28.
 * 获取所有安装的APK详细信息
 */
public class AppManagerEngine {
    /**
     * @param context
     * @return 返回sd卡的可用空间，单位byte
     */
    public static long getSDAvail(Context context) {
        long sdAvail = 0;
        //获取SD卡目录
        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        sdAvail = externalStorageDirectory.getFreeSpace();//获取剩余空间
        return sdAvail;
    }

    /**
     * @return 返回手机（rom）的可用空间，单位byte
     */
    public static long getRomAvail(Context context) {
        long romAvail = 0;
        //获取SD卡目录
        File dataDirectory = Environment.getExternalStorageDirectory();

        romAvail = dataDirectory.getFreeSpace();//获取剩余空间
        return romAvail;
    }

    /**
     * @param context
     * @return 所有安装的APK信息
     */
    public static List<AppBean> getAllApks(Context context) {
        //获取所有安装的APK信息
        List<AppBean> apks = new ArrayList<AppBean>();

        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //所有安装的APK
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : installedPackages) {//增强for循环
            AppBean bean = new AppBean();
            bean.setAppName(packageInfo.applicationInfo.loadLabel(pm) +"");//设置APK的名字
            bean.setPackName(packageInfo.packageName);//设置APK的包名
            bean.setIcon(packageInfo.applicationInfo.loadIcon(pm));//设置APK的图标
           /*获取APK大小*/
            String sourceDir = packageInfo.applicationInfo.sourceDir;
            File file = new File(sourceDir);
            bean.setSize(file.length());
            /*判断APK是否是系统APP以及存储在ROM中还是SD卡中*/
            int flags = packageInfo.applicationInfo.flags;

            if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                bean.setIsSystem(true);//系统APK
            } else {
                bean.setIsSystem(false);//非系统APK
            }

            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                bean.setIsSD(true);//安装在SD卡中
            } else {
                bean.setIsSD(false);//安装在R手机（ROM）中
            }
                                  //添加apk的路径
            bean.setApkPath(packageInfo.applicationInfo.sourceDir);
            //添加app的uid
            bean.setUid(packageInfo.applicationInfo.uid);
            apks.add(bean);//添加APK信息
        }
        return apks;
    }

}
