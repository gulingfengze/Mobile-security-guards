package mobilesafe.eoe;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import utils.EncryptTools;
import utils.MyConstants;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/6/13.
 * 设置向导界面3
 */
public class Setup3Activity extends BaseSetupActivity {
    private EditText et_safeNumber;//安全号码的编辑框
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();
//    }


    /* (non-Javadoc)
     * 子类覆盖此方法来完成组件数据的初始化
     * @see mobile.safe.eoe.BaseSetupActivity#initData()
     */
    @Override
    public void initData() {
        String safenumber = SpTools.getString(getApplicationContext(), MyConstants.SAFENUMBER, "");
        et_safeNumber.setText(EncryptTools.decryption(safenumber));
        super.initData();
    }

    /**
     * 子类覆盖父类方法完成界面显示
     */
    public void initView() {
        setContentView(R.layout.activity_setup3);
        //安全号码的编辑框
        et_safeNumber = (EditText) findViewById(R.id.et_setup3_safenumber);
    }

    /**
     * @param v
     * 选择安全号码点击事件
     */
    public  void selectSafeNumber(View v){
        /*启动显示好友界面*/
        Intent intent = new Intent(this,FriendsActivity.class);
        startActivityForResult(intent,1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {//用户选择数据来关闭联系人界面,而不是直接点击返回按钮
            //取数据
            String phone = data.getStringExtra(MyConstants.SAFENUMBER);
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(Setup3Activity.this, "联系人数据为空", Toast.LENGTH_SHORT).show();
            }
            //显示安全号码
            et_safeNumber.setText(phone);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* (non-Javadoc)
         * 覆盖父类的方法，完成业务
         * @see mobilesafe.eoe.BaseSetupActivity#next(android.view.View)
         */
    @Override
    public void next(View v) {
        /*保存安全号码*/
        //获取安全号码
        String safeNumber = et_safeNumber.getText().toString().trim();
        //如果安全号码，下一步不进行页面的跳转
        if (TextUtils.isEmpty(safeNumber)){
            //为空
            Toast.makeText(getApplicationContext(), "安全号码不能为空",Toast.LENGTH_SHORT).show();
            //不调用父类的功能来进行页面的切换
            return;
        } else {
            //对安全号码加密
            safeNumber = EncryptTools.encrypt(safeNumber);
            //保存安全号码
            SpTools.putString(getApplicationContext(), MyConstants.SAFENUMBER, safeNumber);
        }

        //调用父类功能完成页面的切换
        super.next(v);
    }
    /**
     * 子类覆盖父类方法完成向下一个界面切换
     */
    @Override
    public void nextActivity() {
        startActivity(Setup4Activity.class);//跳转到第四个向导界面
    }
    /**
     * 子类覆盖父类方法完成向上一个界面切换
     */
    @Override
    public void preActivity() {
        startActivity(Setup2Activity.class);//跳转到第二个向导界面
    }
}
