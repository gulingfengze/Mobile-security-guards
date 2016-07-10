package db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lenovo on 2016/6/22.
 * 黑名单数据库
 */
public class BlackDB extends SQLiteOpenHelper {
    /**
     * 初始版本信息
     * @param context
     */
    public BlackDB(Context context) {
        super(context, "black.db", null, 1);
    }

    public BlackDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public BlackDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * 该方法只执行一次
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
          db.execSQL("create table blacktb(_id integer primary key autoincrement,phone text,mode integer)");//创建表
    }

    /**
     * @param db
     * @param oldVersion
     * @param newVersion
     * 版本号发生变化时执行该方法
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
          db.execSQL("drop table blacktb");//清空数据
          onCreate(db);//重新创建表
    }
}
