package domain;

import android.net.Uri;

/**
 * Created by Lenovo on 2016/7/4.
 * 程序锁数据库 加锁的表结构
 */
public interface LockedTable {
    String TABLENAME = "locked";//程序锁的表名
    String PACKNAME = "packname";//程序锁表的列名
    Uri uri = Uri.parse("content://mobilesafe/locked");
}
