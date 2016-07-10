package receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import service.LostFindService;
import utils.EncryptTools;
import utils.MyConstants;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/6/19.
 * 开机启动的广播接收者（sim卡变更报警）
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
           /*手机启动完成，检测SIM卡是否变化*/
        //取出原来保存的sim卡信息
        String oldsim = SpTools.getString(context, MyConstants.SIM, "");

        //取出当前手机的sim卡信息
        TelephonyManager tm= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simSeriaNumber=tm.getSimSerialNumber();

                    /*判断sim卡*/
        if (!oldsim.equals(simSeriaNumber)){
            //sim卡变化，发送报警短信，取出安全号码
            String safeNumber=SpTools.getString(context,MyConstants.SAFENUMBER,"");
            safeNumber = EncryptTools.decryption(safeNumber);
            //发送短信给安全号码
            SmsManager sm = SmsManager.getDefault();
            sm.sendTextMessage(safeNumber, "", "警告：SIM卡被更改！ ", null, null);
        }
                       /*自动启动防盗服务*/
        if (SpTools.getBoolean(context,MyConstants.LOSTFIND,false)){
            // true  开机自动启动防盗服务
            Intent service = new Intent(context, LostFindService.class);
            context.startService(service);//启动防盗保护的服务
        }
    }
}
