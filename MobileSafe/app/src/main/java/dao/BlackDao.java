package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import db.BlackDB;
import domain.BlackBean;
import domain.BlackTable;

/**
 * Created by Lenovo on 2016/6/22.
 * 黑名单数据的业务封装类
 */
public class BlackDao {
    private BlackDB blackDB;

    /**
     * @param context 构造方法
     */
    public BlackDao(Context context) {
        this.blackDB = new BlackDB(context);
    }

    /**
     * @param phone 电话号码
     * @return 拦截模式 : 1 短信 2 电话 3 全部 0 不拦截
     */
    public int getMode(String phone) {
        SQLiteDatabase database = blackDB.getReadableDatabase();
        Cursor cursor = database.rawQuery("select " + BlackTable.MODE
                + " from " + BlackTable.BLACKTABLE + " where "
                + BlackTable.PHONE + "=?", new String[]{phone});
        int mode = 0;
        if (cursor.moveToNext()) {
            mode = cursor.getInt(0);//如果是黑名单号码，则取出对应号码的拦截模式
        } else {
            mode = 0;//不是黑名单号码，不进行拦截
        }
        return mode;
    }


    /**
     * @return 总数据个数
     */
    public int getTotalRows() {
        SQLiteDatabase database = blackDB.getReadableDatabase();
        Cursor cursor = database.rawQuery("select count(1) from "
                + BlackTable.BLACKTABLE, null);
        cursor.moveToNext();
        int totalRows = cursor.getInt(0);// 总行数
        cursor.close();// 关闭游标
        return totalRows;
    }

    /**
     * @param datasNumber 分批加载的数据条目数
     * @param startIndex  取数据的起始位置
     * @return 分批加载数据
     */
    public List<BlackBean> getMoreDatas(int datasNumber, int startIndex) {
        List<BlackBean> datas = new ArrayList<BlackBean>();
        SQLiteDatabase database = blackDB.getReadableDatabase();
        // 获取blacktb的所有数据游标 (2 + 3) + ""
        Cursor cursor = database.rawQuery("select " + BlackTable.PHONE + ","
                + BlackTable.MODE + " from " + BlackTable.BLACKTABLE
                + " order by _id desc limit ?,? ", new String[]{startIndex + "", datasNumber + ""});
        while (cursor.moveToNext()) {// 有数据，数据封装
            BlackBean blackBean = new BlackBean();
            blackBean.setPhone(cursor.getString(0));// 封装黑名单号码
            blackBean.setMode(cursor.getInt(1));// 封装拦截模式
            datas.add(blackBean);// 添加数据到集合中
        }
        cursor.close();// 关闭游标
        database.close();// 关闭数据库
        return datas;
    }

    /**
     * @param currentPage 当前页的页码
     * @param perPage     每页显示多少条数据
     * @return 当前页的数据
     */
    public List<BlackBean> getPageDatas(int currentPage, int perPage) {
        List<BlackBean> datas = new ArrayList<BlackBean>();
        SQLiteDatabase database = blackDB.getReadableDatabase();
        // 获取blacktb的所有数据游标 (2 + 3) + ""
        Cursor cursor = database.rawQuery("select " + BlackTable.PHONE + ","
                + BlackTable.MODE + " from " + BlackTable.BLACKTABLE
                + " limit ?,? ", new String[]{((currentPage - 1) * perPage) + "", perPage + ""});
        while (cursor.moveToNext()) {
            BlackBean blackBean = new BlackBean(); // 有数据，数据封装
            blackBean.setPhone(cursor.getString(0)); // 封装黑名单号码
            blackBean.setMode(cursor.getInt(1));// 封装拦截模式
            datas.add(blackBean);// 添加数据到集合中
        }
        cursor.close();// 关闭游标
        database.close();// 关闭数据库
        return datas;
    }

    /**
     * @param perPage 指定每页显示多少条数据
     * @return 总页数
     */
    public int getTotalPages(int perPage) {
        int totalRows = getTotalRows();
        // 计算出多少页，采用ceil函数，返回不小于该数的最小整数 如 :6.1 返回7.0
        int totalPages = (int) Math.ceil(totalRows * 1.0 / perPage);
        return totalPages;
    }

    /**
     * 查询黑名单号码
     *
     * @return 返回所有黑名单数据
     */
    public List<BlackBean> getAllDatas() {
        List<BlackBean> datas = new ArrayList<BlackBean>();//创建容器
        SQLiteDatabase database = blackDB.getReadableDatabase();
                     /*获取blacktb的所有数据游标*/
        Cursor cursor = database.rawQuery("select " + BlackTable.PHONE + ","
                + BlackTable.MODE + " from " + BlackTable.BLACKTABLE, null);
        while (cursor.moveToNext()) {
            BlackBean blackBean = new BlackBean();//有数据，数据封装
            blackBean.setPhone(cursor.getString(0));//封装黑名单号码
            blackBean.setMode(cursor.getInt(1));//封装拦截模式
            datas.add(blackBean);//添加数据到集合
        }
        cursor.close();//关闭游标
        database.close();//关闭数据库
        return datas;
    }

    /**
     * 删除黑名单号码
     *
     * @param phone 要删除的黑名单号码
     */
    public void delete(String phone) {
        SQLiteDatabase db = this.blackDB.getWritableDatabase();

        db.delete(BlackTable.BLACKTABLE, BlackTable.PHONE + "=?", new String[]{phone});//根据黑名单号码来删除黑名单数据
        db.close();
    }

    /**
     * 更新
     *
     * @param phone 修改黑名单号码
     * @param mode  修改新的拦截模式
     */
    public void update(String phone, int mode) {
        SQLiteDatabase db = this.blackDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BlackTable.MODE, mode);//设置黑名单拦截模式
        db.update(BlackTable.BLACKTABLE, values, BlackTable.PHONE + "==?", new String[]{phone});//根据号码更新拦截模式
        db.close();
    }

    /**
     * 添加黑名单号码
     *
     * @param blackBean 黑名单信息的封装bean
     */
    public void add(BlackBean blackBean) {
        add(blackBean.getPhone(), blackBean.getMode());
    }

    /**
     * 添加黑名单号码
     *
     * @param phone 黑名单号码
     * @param mode  拦截模式
     */
    private void add(String phone, Integer mode) {
        SQLiteDatabase db = blackDB.getWritableDatabase();
        ContentValues values = new ContentValues();//对添加的数据进行组装
        values.put(BlackTable.PHONE, phone);//设置黑名单号码
        values.put(BlackTable.MODE, mode);//设置黑名单拦截模式
        db.insert(BlackTable.BLACKTABLE, null, values);//添加黑名单
        db.close();//关闭数据库

    }
}
