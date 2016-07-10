package mobilesafe.eoe;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import utils.MyConstants;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/6/13.
 * 设置向导界面2
 */
public class Setup2Activity extends BaseSetupActivity {
    private Button bt_bind;
    private ImageView iv_isBind;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();
//    }

    /* (non-Javadoc)
	 * 初始化组件的数据
	 * @see mobilesafe.eoe.BaseSetupActivity#initData()
	 */
    @Override
    public void initData() {
        super.initData();
    }

    /**
     * 子类覆盖父类方法完成界面显示
     */
    public void initView() {
        setContentView(R.layout.activity_setup2);
        //获取bind sim卡按钮
        bt_bind = (Button) findViewById(R.id.bt_setup2_bindsim);
        //是否绑定sim卡的图标
        iv_isBind = (ImageView) findViewById(R.id.iv_setup2_isbind);
        if (TextUtils.isEmpty(SpTools.getString(getApplicationContext(), MyConstants.SIM, ""))){
            //未绑定
            {
                //切换是否绑定sim卡的图标
                iv_isBind.setImageResource(R.drawable.unlock);//设置未加锁的图片
            }
        } else {
            {
                //切换是否绑定sim卡的图标
                iv_isBind.setImageResource(R.drawable.lock);//设置加锁的图片
            }
        }
    }

    /* (non-Javadoc)
	 * 添加自己的事件
	 * @see mobilesafe.eoe.BaseSetupActivity#initEvent()
	 */
    @Override
    public void initEvent() {
        //添加自己的点击事件（bind sim卡按钮）
        bt_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*对sim卡进行绑定和解绑操作*/
                if (TextUtils.isEmpty(SpTools.getString(getApplicationContext(),MyConstants.SIM,""))){
                    //1.如果没有绑定,进行绑定

                    //获取sim卡信息
                    TelephonyManager tm= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    //sim卡信息
                    String  simSerialNumber = tm.getSimSerialNumber();
                    //将sim卡信息保存到sp中
                    SpTools.putString(getApplicationContext(),MyConstants.SIM,simSerialNumber);

                    //切换是否绑定sim卡的图标(设置加锁图片)
                    iv_isBind.setImageResource(R.drawable.lock);
                }else {
                    //2.已经绑定sim卡，解绑sim卡，这里保存空值来实现
                    SpTools.putString(getApplicationContext(), MyConstants.SIM, "");

                    //切换是否绑定sim卡的图标（设置解锁图片）
                    iv_isBind.setImageResource(R.drawable.unlock);//设置未加锁的图片

                }

            }
        });
        super.initEvent();
    }

    @Override
    public void next(View v) {
        if (TextUtils.isEmpty(SpTools.getString(getApplicationContext(), MyConstants.SIM, ""))){
            //没有绑定sim
            Toast.makeText(getApplicationContext(), "请先绑定sim卡", Toast.LENGTH_SHORT).show();
            return;
        }
        super.next(v);//调用父类的功能
    }

    /**
     * 子类覆盖父类方法完成向下一个界面切换
     */
    @Override
    public void nextActivity() {
        startActivity(Setup3Activity.class);//跳转到第三个向导界面
    }
    /**
     * 子类覆盖父类方法完成向上一个界面切换
     */
    @Override
    public void preActivity() {
        startActivity(Setup1Activity.class);//跳转到第一个向导界面
    }
}
