package engine;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import domain.ContantBean;

/**
 * Created by Lenovo on 2016/6/19.
 * 读取手机联系人的功能类
 */
public class ReadContantsEngine {

    /**
     * @return
     *     短信日志的记录
     */
    public static List<ContantBean> readSmslog(Context context){
        //1，电话日志的数据库
        //2,通过分析，db不能直接访问，需要内容提供者访问该数据库
        //3,看上层源码 找到uri content://sms
        Uri uri = Uri.parse("content://sms");
        //获取电话记录的联系人游标
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"address"}, null, null, " _id desc");
        List<ContantBean> datas = new ArrayList<ContantBean>();

        while (cursor.moveToNext()) {
            ContantBean bean = new ContantBean();

            String phone = cursor.getString(0);//获取号码
            //String name = cursor.getString(1);//获取名字

            //bean.setName(name);
            bean.setPhone(phone);

            //添加数据
            datas.add(bean);

        }
        return datas;

    }

    /**
     * @return
     *     电话日志的记录
     */
public static List<ContantBean>readCalllog(Context context){
    //1，电话日志的数据库
    //2,通过分析，db不能直接访问，需要内容提供者访问该数据库
    //3,看上层源码 找到uri为   content://call_log/calls
    List<ContantBean>datas=new ArrayList<ContantBean>();
    Uri uri=Uri.parse("content://call_log/calls");
    Cursor cursor = context.getContentResolver().query(uri, new String[]{"number", "name"}, null, null, " _id desc");
    while (cursor.moveToNext()){
        ContantBean bean=new ContantBean();
        String phone=cursor.getString(0);//获取号码
        String name=cursor.getString(1);//获取名字
        bean.setName(name);
        bean.setPhone(phone);
        datas.add(bean);//添加数据
    }
    return datas;
}


           /*读取联系人*/
    public static List<ContantBean>readContants(Context context){
        List<ContantBean>datas=new ArrayList<ContantBean>();
        //将URI字符串解析成Uri对象作为参数传入
        Uri uriContants=Uri.parse("content://com.android.contacts/contacts");
        Uri uriDatas = Uri.parse("content://com.android.contacts/data");
        /*1.查询联系人id*/
        Cursor cursor1=context.getContentResolver().query(uriContants, new String[]{"_id"}, null, null, null);
        while(cursor1.moveToNext()){
            ContantBean bean = new ContantBean();//实例化联系人信息的封装bean
            String id = cursor1.getString(0);//获取到联系人的id
        /*2.查询联系人手机号码*/
        Cursor cursor2 = context.getContentResolver().query(uriDatas,new String[]{"data1","mimetype"}, " raw_contact_id = ? ", new String[]{id}, null);
        while(cursor2.moveToNext()) {
             String data  = cursor2.getString(0);
             String mimeType = cursor2.getString(1);
                if (mimeType.equals("vnd.android.cursor.item/name")) {
                    System.out.println("第" +id + "个用户：名字：" + data);
                    bean.setName(data);
                } else if (mimeType.equals("vnd.android.cursor.item/phone_v2")) {
                    System.out.println("第" +id + "个用户：电话：" + data);
                    bean.setPhone(data);
                }
            }
            cursor2.close();//关闭游标释放资源
            datas.add(bean);//加一条好友信息
        }
        cursor1.close();
        return datas;
    }
}