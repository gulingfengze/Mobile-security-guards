package domain;

import android.graphics.drawable.Drawable;

/**
 * Created by Lenovo on 2016/6/28.
 * APK信息封装
 */
public class AppBean {
    private int uid;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    private Drawable icon;//APK的图标
    private String appName;//APK的名字
    private long size;//APK占用的大小，单位为字节（byte）
    private boolean isSD;//是否存储在SD卡中
    private boolean isSystem;//是否是系统APP
    private String packName;//APK的包名
    private String apkPath;//APK安装路径

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isSD() {
        return isSD;
    }

    public void setIsSD(boolean isSD) {
        this.isSD = isSD;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setIsSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }
}
