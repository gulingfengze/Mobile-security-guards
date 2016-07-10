package service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;

import java.util.List;

import utils.EncryptTools;
import utils.MyConstants;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/6/20.
 * 定位服务管理器，获取定位信息
 */
public class LocationService extends Service {

    private LocationManager lm;
    private LocationListener listener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //获取位置管理器
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        //获取监听对象
        listener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                float accuracy = location.getAccuracy();//精确度，以米为单位
                double altitude = location.getAltitude();// 获取海拔高度
                double longitude = location.getLongitude();// 获取经度
                double latitude = location.getLatitude();// 获取纬度
                float speed = location.getSpeed();// 速度
                             /* 定位信息*/
                StringBuilder tv_mess = new StringBuilder();
                tv_mess.append("accuracy:" + accuracy + "\n");
                tv_mess.append("altitude:" + altitude + "\n");
                tv_mess.append("longitude:" + longitude + "\n");
                tv_mess.append("latitude:" + latitude + "\n");
                tv_mess.append("speed:" + speed + "\n");
                           /*发送短信*/
                String safeNumber = SpTools.getString(LocationService.this, MyConstants.SAFENUMBER, "");
                safeNumber = EncryptTools.decryption(safeNumber);
                         /*发送短信给安全号码*/
                SmsManager sm = SmsManager.getDefault();
                sm.sendTextMessage(safeNumber, "", tv_mess + "", null, null);
                stopSelf();// 关闭gps

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //获取所有的提供的定位方式
        List<String> allProviders = lm.getAllProviders();
        for (String string : allProviders) {
            System.out.println(string + ">>>定位方式");
        }

        Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);//产生费用
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //动态获取手机的最佳定位方式
        String bestProvider = lm.getBestProvider(criteria , true);
        //注册监听回调
        lm.requestLocationUpdates(bestProvider, 0, 0, listener);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.removeUpdates(listener);//取消定位监听
        lm = null;
        super.onDestroy();
    }
}
