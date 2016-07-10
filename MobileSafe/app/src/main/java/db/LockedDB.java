package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lenovo on 2016/7/4.
 * 创建程序锁的数据库 ，库中只有一张表，表中就一个名为packName的列
 */
public class LockedDB extends SQLiteOpenHelper {
    public LockedDB(Context context) {
        super(context, "locked.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table locked(_id integer primary key autoincrement,packName text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table locked");
        onCreate(db);
    }
}
