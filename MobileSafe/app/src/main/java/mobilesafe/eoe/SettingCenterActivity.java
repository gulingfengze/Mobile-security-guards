package mobilesafe.eoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import service.ComingPhoneService;
import service.TelSmsBlackService;
import service.WatchDogService;
import utils.MyConstants;
import utils.ServiceUtils;
import utils.SpTools;
import view.SettingCenterItemView;

/**
 * 设置中心界面
 */
public class SettingCenterActivity extends AppCompatActivity {

    private SettingCenterItemView sciv_autoupdate;
    private SettingCenterItemView sciv_blackservice;
    private SettingCenterItemView sciv_phoneLocationService;
    private TextView tv_locationStyle_content;
    private RelativeLayout rl_style_root;
    private AlertDialog dialog;
    private ImageView iv_locationSytle_click;
    private SettingCenterItemView sciv_watchdog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();//初始化界面
        initEvent();//初始化组件事件
        initData();//初始化组件的数据
    }

    private void initData() {
        // 判断看门狗服务，来设置复选框的初始值
        sciv_watchdog.setChecked(ServiceUtils.isServiceRunning(getApplicationContext(), "service.WatchDogService"));
        //初始化自动更新复选框的初始值
        sciv_autoupdate.setChecked(SpTools.getBoolean(getApplicationContext(), MyConstants.AUTOUPDATE, false));
        //判断黑名单服务，设置复选框初始值
        sciv_blackservice.setChecked(ServiceUtils.isServiceRunning(getApplicationContext(), "service.TelSmsBlackService"));
        //判断来电归属地服务，设置复选框初始值
        sciv_phoneLocationService.setChecked(ServiceUtils.isServiceRunning(getApplicationContext(), "service.ComingPhoneService"));


    }

    private String[] styleNames = new String[]{"卫士蓝", "金属灰", "苹果绿", "活力橙",
            "半透明"};


    /**
     * 来电显示归属地背景样式对话框
     */
    private void showStyleDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(SettingCenterActivity.this);
        ab.setTitle("选择归属地样式");
        ab.setSingleChoiceItems(styleNames,
                Integer.parseInt(SpTools.getString(getApplicationContext(),
                        MyConstants.STYLEBGINDEX, "0")),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {// which 点击的位置

                        // 保存sp中 字符串的方式保存归属地样式
                        SpTools.putString(getApplicationContext(),
                                MyConstants.STYLEBGINDEX, which + "");
                        tv_locationStyle_content.setText(styleNames[which]);
                        dialog.dismiss();
                    }
                });
        dialog = ab.create();
        dialog.show();

    }

    private void initEvent() {
      /* 看门狗服务事件*/
        sciv_watchdog.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断看门狗服务是否运行
                if (ServiceUtils.isServiceRunning(getApplicationContext(),
                        "service.WatchDogService")) {
                    // 服务在运行,关闭服务
                    Intent watchDogService = new Intent(
                            SettingCenterActivity.this, WatchDogService.class);
                    stopService(watchDogService);
                    System.out.println("watchdog service.....stop");
                    // 设置复选框的状态
                    sciv_watchdog.setChecked(false);
                } else {
                    // 服务停止,打开服务
                    Intent comingPhoneService = new Intent(
                            SettingCenterActivity.this, WatchDogService.class);
                    startService(comingPhoneService);
                    System.out.println("watchdog service.....start");
                    // 设置复选框的状态
                    sciv_watchdog.setChecked(true);
                }
            }
        });
       /*自动更新的事件处理*/
        sciv_autoupdate.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //原来的复选框状态的功能不变
                sciv_autoupdate.setChecked(!sciv_autoupdate.isChecked());
                //添加新的功能
                //如果复选框选中，自动更新已经开启，否则不开启
                //记录复选框的状态
                SpTools.putBoolean(getApplicationContext(), MyConstants.AUTOUPDATE, sciv_autoupdate.isChecked());
            }
        });

        /*黑名单服务启动或关闭*/
        sciv_blackservice.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果服务正在运行，则关闭服务
                if (ServiceUtils.isServiceRunning(getApplicationContext(), "service.TelSmsBlackService")) {
                    Intent blackService = new Intent(SettingCenterActivity.this, TelSmsBlackService.class);
                    stopService(blackService);

                    sciv_blackservice.setChecked(false);//设置复选框状态为关闭

                } else {
                    //打开服务
                    Intent blackService = new Intent(SettingCenterActivity.this, TelSmsBlackService.class);
                    startService(blackService);

                    sciv_blackservice.setChecked(true);//设置复选框状态为打开
                }
                SpTools.putBoolean(getApplicationContext(), MyConstants.BLACK, sciv_blackservice.isChecked()); //记录复选框的状态
            }
        });
        /*来电显示归属地服务启动或关闭*/
        sciv_phoneLocationService.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果服务正在运行，则关闭服务
                if (ServiceUtils.isServiceRunning(getApplicationContext(), "service.ComingPhoneService")) {
                    Intent comingService = new Intent(SettingCenterActivity.this, ComingPhoneService.class);
                    stopService(comingService);

                    sciv_phoneLocationService.setChecked(false);//设置复选框状态为关闭
                } else {
                    //打开服务
                    Intent comingService = new Intent(SettingCenterActivity.this, ComingPhoneService.class);
                    startService(comingService);

                    sciv_phoneLocationService.setChecked(true);//设置复选框状态为打开
                }
                SpTools.putBoolean(getApplicationContext(), MyConstants.PHONELOCATION, sciv_phoneLocationService.isChecked());
            }
        });

//        /*归属地跟布局点击事件*/
//        rl_style_root.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showStyleDialog();//弹出对话框让用户选择来电显示归属地背景的样式
//            }
//        });
        //归属地根布局点击事件
        rl_style_root.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://按下
                        iv_locationSytle_click.setImageResource(R.drawable.jiantou1_pressed);
                        break;
                    case MotionEvent.ACTION_UP://松开
                        iv_locationSytle_click.setImageResource(R.drawable.jiantou1_disable);
                        //显示选择归属地样式的对话框
                        showStyleDialog();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        /*箭头点击事件*/
        iv_locationSytle_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStyleDialog();//弹出对话框让用户选择来电显示归属地背景的样式
            }
        });
    }


    private void initView() {
        setContentView(R.layout.activity_setting_center);
        //获取自动更新自定义view
        sciv_autoupdate = (SettingCenterItemView) findViewById(R.id.sciv_setting_center_autoupdate);
        //获取黑名单拦截自定义view
        sciv_blackservice = (SettingCenterItemView) findViewById(R.id.sciv_setting_center_blackservice);
        //获取来电显示服务的设置
        sciv_phoneLocationService = (SettingCenterItemView) findViewById(R.id.sciv_setting_center_phonelocationservice);
        //获取归属地样式跟布局
        rl_style_root = (RelativeLayout) findViewById(R.id.rl_settingcenter_locationsytle_root);
        //获取归属地样式名字
        tv_locationStyle_content = (TextView) findViewById(R.id.tv_settingcenter_locationsytle_content);
        //点击图片按钮来显示样式选择对话框
        iv_locationSytle_click = (ImageView) findViewById(R.id.iv_settingcenter_locationsytle_select);
        // 看门狗服务的设置
        sciv_watchdog = (SettingCenterItemView) findViewById(R.id.sciv_setting_center_watchdogservice);
    }
}
