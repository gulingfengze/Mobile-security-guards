package mobilesafe.eoe;

/**
 * Created by Lenovo on 2016/6/13.
 * 设置向导界面1
 */
public class Setup1Activity extends BaseSetupActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();
//    }

    /**
     * 子类覆盖父类方法完成界面显示
     */
    public void initView() {
        setContentView(R.layout.activity_setup1);
    }
    /**
     * 子类覆盖父类方法完成向下一个界面切换
     */
    @Override
    public void nextActivity() {
        startActivity(Setup2Activity.class);//跳转到第二个向导界面
    }
    /**
     * 子类覆盖父类方法完成向上一个界面切换
     */
    @Override
    public void preActivity() {

    }
}
