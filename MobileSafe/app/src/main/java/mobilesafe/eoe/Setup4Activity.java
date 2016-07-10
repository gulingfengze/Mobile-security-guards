package mobilesafe.eoe;

import android.content.Intent;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import service.LostFindService;
import utils.MyConstants;
import utils.ServiceUtils;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/6/13.
 * 设置向导界面4
 */
public class Setup4Activity extends BaseSetupActivity {
    private CheckBox cb_isprotected;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();
//    }

    /**
     * 子类覆盖父类方法完成界面显示
     */
    public void initView() {
        setContentView(R.layout.activity_setup4);
        //打钩开启防盗保护的复选框
        cb_isprotected= (CheckBox) findViewById(R.id.cb_setup4_isprotected);
    }
    /* (non-Javadoc)
     * 初始化复选框的值
     * @see mobilesafe.eoe.BaseSetupActivity#initData()
     */
    @Override
    public void initData() {
        //初始化复选框的值 看服务是否开启
        //如果服务开启，打钩，否则不打钩
        if (ServiceUtils.isServiceRunning(getApplicationContext(), "service.LostFindService")){
            cb_isprotected.setChecked(true);
        } else {
            cb_isprotected.setChecked(false);//初始化复选框的状态
        }
        super.initData();
    }

    /* (non-Javadoc)
     * 初始化复选框的事件
     * @see mobilesafe.eoe.BaseSetupActivity#initData()
     */
    @Override
    public void initEvent() {
       cb_isprotected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if (isChecked){
                     SpTools.putBoolean(getApplicationContext(), MyConstants.LOSTFIND, true);
                     System.out.println("check true");
                     Intent service = new Intent(Setup4Activity.this, LostFindService.class);
                     startService(service);//启动防盗保护服务
                 }else {
                     SpTools.putBoolean(getApplicationContext(), MyConstants.LOSTFIND, false);
                     System.out.println("check false");
                     Intent service=new Intent(Setup4Activity.this, LostFindService.class);
                     stopService(service);//关闭防盗保护服务
                 }
           }
       });
        super.initEvent();
    }

    /**
     * 子类覆盖父类方法完成向下一个界面切换
     */
    @Override
    public void nextActivity() {
        //保存设置完成的状态
        SpTools.putBoolean(getApplicationContext(), MyConstants.ISSETUP, true);
        startActivity(LostFindActivity.class);//跳转到手机防盗界面
    }
    /**
     * 子类覆盖父类方法完成向上一个界面切换
     */
    @Override
    public void preActivity() {
        startActivity(Setup3Activity.class);//跳转到第三个向导界面
    }
}
