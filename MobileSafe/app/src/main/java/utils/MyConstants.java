package utils;

/**
 * Created by Lenovo on 2016/6/12.
 * 存储常量
 */
public interface MyConstants {
    String VIRUSVERSIONURL = "http://10.0.2.2:8080/VirusServer/servlet/getversion";//获取病毒库版本的url
    String GETVIRUSVDATASURL = "http://10.0.2.2:8080/VirusServer/servlet/getviruses";//获取病毒库数据的url
    String SPFILE="config";//sp的配置文件名
    String PASSWORD="password";//设置手机防盗密码
    String ISSETUP="issetup";//是否进入过向导界面
    String SIM = "sim";//sim卡信息
    String SAFENUMBER = "safenumber";//安全号码
    int ENCRYPT = 66;//加密
    String LOSTFIND = "lostfind";//开机是否开启手机防盗
    String LOSTFINDNAME = "lostfindname";//手机防盗名
    String AUTOUPDATE = "autoupdate";//自定更新设置
    String TOASTX = "toastx";//自定义土司x坐标
    String TOASTY = "toasty";//自定义土司y坐标
    String STYLEBGINDEX = "styleindex";//归属地背景样式
    String BLACK="black";//黑名单拦截设置
    String PHONELOCATION="phonelocation";//来电归属地设置
    String SHOWSYSTEM = "showsystem";//显示系统进程
}
