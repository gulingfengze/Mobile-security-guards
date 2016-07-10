package mobilesafe.eoe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import service.ClearTaskService;
import utils.MyConstants;
import utils.ServiceUtils;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/7/2.
 * 进程管理的设置界面
 */
public class TaskManagerSettingActivity extends Activity {
    private CheckBox cb_showsystemapp;
    private CheckBox cb_lockscreenClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        /*设置锁屏清理进程(通过广播服务来完成)*/
        cb_lockscreenClear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {//如果选中“设置锁屏清理进程”复选框，则启动服务/广播
                    Intent service = new Intent(TaskManagerSettingActivity.this, ClearTaskService.class);
                    startService(service);
                } else {//关闭服务/广播
                    Intent service = new Intent(TaskManagerSettingActivity.this, ClearTaskService.class);
                    stopService(service);
                }

            }
        });
        /*设置显示系统进程*/
        cb_showsystemapp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SpTools.putBoolean(getApplicationContext(), MyConstants.SHOWSYSTEM, isChecked);//保存是否显示系统进程的标记
            }
        });
    }

    private void initData() {
        /*通过锁屏清理进程服务来判断复选框状态/是否开启服务*/
        cb_lockscreenClear.setChecked(ServiceUtils.isServiceRunning(getApplicationContext(), "service.ClearTaskService"));

        /*通过标记来初始化是否显示系统进程的状态*/
        cb_showsystemapp.setChecked(SpTools.getBoolean(this, MyConstants.SHOWSYSTEM, false));
    }

    private void initView() {
        setContentView(R.layout.activity_taskmanagersettingcenter);
        cb_lockscreenClear = (CheckBox) findViewById(R.id.cb_taskmanager_settingcenter_lockscree_clear);
        cb_showsystemapp = (CheckBox) findViewById(R.id.cb_taskmanager_settingcenter_lockscree_showsystemapp);

    }

    @Override
    protected void onResume() {
        initData();
        super.onResume();
    }
}
