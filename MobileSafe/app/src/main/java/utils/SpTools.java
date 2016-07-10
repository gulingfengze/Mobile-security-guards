package utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Lenovo on 2016/6/12.
 * 存储相关数据（密码等信息）
 */
public class SpTools {
    public static void putString(Context context,String key,String value){
        SharedPreferences sp=context.getSharedPreferences(MyConstants.SPFILE,context.MODE_PRIVATE);
        sp.edit().putString(key,value).commit();//保存数据
    }
    public static String getString(Context context, String key, String defvalue){
        SharedPreferences sp=context.getSharedPreferences(MyConstants.SPFILE,context.MODE_PRIVATE);
        return sp.getString(key,defvalue);//获取数据
    }
    public static void putBoolean(Context context,String key,Boolean value){
        SharedPreferences sp=context.getSharedPreferences(MyConstants.SPFILE,context.MODE_PRIVATE);
        sp.edit().putBoolean(key,value).commit();//保存数据
    }
    public static Boolean getBoolean(Context context, String key, Boolean defvalue){
        SharedPreferences sp=context.getSharedPreferences(MyConstants.SPFILE,context.MODE_PRIVATE);
        return sp.getBoolean(key,defvalue);//获取数据
    }
}
