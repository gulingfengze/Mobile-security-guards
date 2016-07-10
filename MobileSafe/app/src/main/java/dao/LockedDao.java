package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import db.LockedDB;
import domain.LockedTable;

/**
 * Created by Lenovo on 2016/7/4.
 * 程序锁数据的业务封装类
 */
public class LockedDao {
    private LockedDB lockdb;
    private Context context;
    public LockedDao(Context context) {
        this.context = context;
        lockdb = new LockedDB(context);
    }

    /**
     * @param packName APP包名
     * @return 是否加锁
     */
    public boolean isLocked(String packName) {
        boolean res = false;
        SQLiteDatabase database = lockdb.getReadableDatabase();
        Cursor cursor = database.rawQuery("select 1 from "
                + LockedTable.TABLENAME + " where " + LockedTable.PACKNAME
                + "=?", new String[] { packName });
        if (cursor.moveToNext()) {
            res = true;
        }

        cursor.close();
        database.close();
        return res;
    }

    /**
     * @param packName 需要加锁的APP包名
     */
    public void add(String packName) {
        SQLiteDatabase database = lockdb.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LockedTable.PACKNAME, packName);
        //向表中添加一条数据
        database.insert(LockedTable.TABLENAME, null, values);

        database.close();//关闭数据库
        // 发送内容观察者的通知
       context.getContentResolver().notifyChange(LockedTable.uri, null);
    }

    /**
     * @param packName 解除加锁的APP包名
     */
    public void remove(String packName) {
        SQLiteDatabase database = lockdb.getWritableDatabase();
        //表中删除一条数据
        database.delete(LockedTable.TABLENAME, LockedTable.PACKNAME + "=?", new String[]{packName});

        database.close();//关闭数据库
        // 发送内容观察者的通知
        context.getContentResolver().notifyChange(LockedTable.uri, null);
    }

    /**
     * @return 获取所有加锁的APP包名
     */
    public List<String> getAllLockedDatas() {

        List<String> lockedNames = new ArrayList<String>();
        SQLiteDatabase database = lockdb.getReadableDatabase();
        /*取出所有的包名*/
        Cursor cursor = database.rawQuery("select " + LockedTable.PACKNAME + " from " + LockedTable.TABLENAME, null);
        while (cursor.moveToNext()) {
            lockedNames.add(cursor.getString(0)); //取出包名
        }
        cursor.close();
        database.close();
        return lockedNames;
    }
}
