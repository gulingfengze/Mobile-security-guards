package domain;

import android.graphics.drawable.Drawable;

/**
 * Created by Lenovo on 2016/7/2.
 * 进程管理功能中的进程数据封装类
 */
public class TaskBean {
    private Drawable icon;//APK图标
    private String name;//APK名称
    private String packName;//APK包名
    private long memSize;//APK占用的内存大小
    private boolean isSystem;//是否是系统APK
    private boolean isChecked;//是否被选中

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setIsSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public long getMemSize() {
        return memSize;
    }

    public void setMemSize(long memSize) {
        this.memSize = memSize;
    }
}
