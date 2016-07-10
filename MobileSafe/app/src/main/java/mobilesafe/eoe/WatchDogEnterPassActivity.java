package mobilesafe.eoe;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Lenovo on 2016/7/5.
 * 看门狗密码输入界面
 */
public class WatchDogEnterPassActivity extends Activity {

    private ImageView iv_icon;
    private EditText et_pass;
    private Button bt_enter;
    private HomeReceiver receiver;
    private String packName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        bt_enter.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                String pass = et_pass.getText().toString().trim();//获取输入的密码
                if (TextUtils.isEmpty(pass)) {//判断输入的密码是否为空
                    Toast.makeText(WatchDogEnterPassActivity.this, "输入的密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (pass.equals("369258")) {//如果密码正确（369258），关闭自己
                    //这里发送一个自定义广播，告诉看门狗服务，是熟人
                    Intent intent = new Intent();
                    intent.setAction("WatchDog");
                    intent.putExtra("packname", packName);
                    sendBroadcast(intent);//发送广播
                    finish();//关闭自己
                } else {
                    Toast.makeText(WatchDogEnterPassActivity.this, "输入的密码不正确", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

    }

    /**
     * 创建Home键广播
     */
    private class HomeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) { //如果是home键回到主界面 关闭自己
                //如果是home键回到主界面 关闭自己

                goToHome();
            }
        }
    }

    private void initData() {
               /*注册Home键的广播*/
        receiver = new HomeReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(receiver, filter);
        Intent intent = getIntent();
        //获取APP的包名
        packName = intent.getStringExtra("packname");
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packName, 0);
            iv_icon.setImageDrawable(applicationInfo.loadIcon(pm));


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_watchdog_enterpass);
        iv_icon = (ImageView) findViewById(R.id.iv_watchingDong_enterPass_icon);
        et_pass = (EditText) findViewById(R.id.et_watchingDong_enterPass_passWord);
        bt_enter = (Button) findViewById(R.id.bt_watchingDong_enterPass_enter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //回到手机主界面
            /*
             *  <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.MONKEY"/>
            </intent-filter>
			 */
            goToHome();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goToHome() {
         /*
             *  <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.MONKEY"/>
            </intent-filter>
			 */
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.MONKEY");
        startActivity(intent);
        finish();//关闭自己
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);//注销广播
        super.onDestroy();
    }
}
