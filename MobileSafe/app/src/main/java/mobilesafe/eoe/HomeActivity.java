package mobilesafe.eoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import utils.Md5Utils;
import utils.MyConstants;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/6/9.
 * 主界面
 */
public class HomeActivity extends Activity {
    private StartAppAd startAppAd = new StartAppAd(this);

    private GridView gv_menus;//主界面按钮
    private int icons[] = {R.drawable.safe,R.drawable.callmsgsafe,R.drawable.item_gv_selector_app
            ,R.drawable.taskmanager,R.drawable.netmanager,R.drawable.trojan
            ,R.drawable.sysoptimize,R.drawable.atools,R.drawable.settings};

    private String names[]={"手机防盗","通讯卫士","软件管家","进程管理","流量统计","病毒查杀","缓存清理","高级工具","设置中心"};
    private MyAdapter adapter;//GridView的适配器
    private AlertDialog dialog;//设置对话框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* 注册开发者和app的信息*/
        StartAppSDK.init(this, "102205247", "202885025", true);


        initView();//初始化界面
        initData();//为GridView设置数据
        initEvent();//初始化GridView事件

    }

    /**
     * 初始化GridView组件事件
     */
    private void initEvent() {
        gv_menus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //判断点击位置
                switch (position) {
                    case 0://手机防盗
                        /*先判断是否设置过密码，没有设置密码则弹出设置密码的对话框，否则弹出输入密码登录对话框*/
                        if (TextUtils.isEmpty(SpTools.getString(getApplicationContext(), MyConstants.PASSWORD, ""))) {

                            showSettingPassDialog();//弹出对话框进行密码设置（自定义对话框）
                        } else {
                            showEnterPassDialog();//弹出输入密码登陆的对话框
                        }
                        break;
                    case 1://通讯卫士
                        Intent i=new Intent(HomeActivity.this,TelSmsSafeActivity.class);
                        startActivity(i);
                        break;
                    case 2://软件管家
                        Intent app=new Intent(HomeActivity.this,AppManagerActivity.class);
                        startActivity(app);
                        break;
                    case 3://进程管理
                        Intent process=new Intent(HomeActivity.this,TaskManagerActivity.class);
                        startActivity(process);
                        break;
                    case 4://流量统计
                    {
                        Intent flow = new Intent(HomeActivity.this,ConnectivityActivity.class);
                        startActivity(flow);
                        break;
                    }
                    case 5://扫描病毒
                    {
                        Intent antivirus = new Intent(HomeActivity.this,AntivirusActivity.class);
                        startActivity(antivirus);
                        break;

                    }
                    case 6://缓存清理
                    {
                        Intent cache = new Intent(HomeActivity.this,CacheActivity.class);
                        startActivity(cache);
                        break;

                    }
                    case 7://高级工具
                        Intent tool=new Intent(HomeActivity.this,AtoolActivity.class);
                        startActivity(tool);
                        break;
                    case 8://设置中心
                            Intent setting=new Intent(HomeActivity.this,SettingCenterActivity.class);
                            startActivity(setting);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * 设置“手机防盗”功能的密码登陆对话框
     */
    private void showEnterPassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(getApplicationContext(), R.layout.dialog_enter_password, null);
        final EditText et_passone = (EditText) view.findViewById(R.id.et_dialog_enter_password_passone);

        Button bt_setpass = (Button) view.findViewById(R.id.bt_dialog_enter_password_login);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_dialog_enter_password_cancel);


        builder.setView(view);



        bt_setpass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //设置密码
                String passone = et_passone.getText().toString().trim();

                if (TextUtils.isEmpty(passone)) {
                    Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // 密码判断,md5 2次加密
                    passone = Md5Utils.md5(Md5Utils.md5(passone));
                    //读取sp中保存的密文，进行判断
                    if (passone.equals(SpTools.getString(getApplicationContext(), MyConstants.PASSWORD, ""))) {
                        //一致
                        //进入手机防盗界面
                        Intent intent = new Intent(HomeActivity.this, LostFindActivity.class);
                        startActivity(intent);
                    } else {
                        //不一致
                        Toast.makeText(getApplicationContext(), "密码不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //关闭对话框
                    dialog.dismiss();
                }

            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();//关闭对话框
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 设置“手机防盗”功能的密码设置对话框
     */
    private void showSettingPassDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=View.inflate(getApplicationContext(),R.layout.dialog_setting_password,null);
        final EditText et_passone= (EditText) view.findViewById(R.id.et_dialog_setting_password_passone);
        final EditText et_passtwo= (EditText) view.findViewById(R.id.et_dialog_setting_password_passtwo);
        Button bt_setpass= (Button) view.findViewById(R.id.bt_dialog_setting_password_setpass);
        Button bt_cancel= (Button) view.findViewById(R.id.bt_dialog_setting_password_cancel);

        builder.setView(view);

        bt_setpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passone = et_passone.getText().toString().trim();//获取密码并去掉两边空格（使用trim（））
                String passtwo = et_passtwo.getText().toString().trim();
                /*对设置的密码进行判断,即密码一和密码二是否都填写完整和填写是否一致*/
                if (TextUtils.isEmpty(passone) || TextUtils.isEmpty(passtwo)) {
                    Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!passone.equals(passtwo)) {
                    Toast.makeText(getApplicationContext(), "密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                } else
                    System.out.println("保存密码");
               /*将密码保存到SharedPreferences中(SpTools.java类)*/

                passone = Md5Utils.md5(Md5Utils.md5(passone)); //对密码加密处理 ，md5 2次加密 保存密文
                SpTools.putString(getApplicationContext(), MyConstants.PASSWORD, passone);
                dialog.dismiss();
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();//关闭对话框
            }
        });
        dialog = builder.create();
        dialog.show();
     }

    /**
     * 初始化组件数据
     */
    private void initData() {
        adapter = new MyAdapter();
        gv_menus.setAdapter(adapter);//将适配器给GridView(设置GridView的适配器数据)
    }
    @Override
    protected void onResume() {

        adapter.notifyDataSetChanged(); //通知Gridview重新取数据
        startAppAd.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        startAppAd.onPause();
    }

    /**
     * 创建适配器（主界面中每一个功能都是：上图标，下文字。所以需要创建组合控件）
     */
    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return icons.length;//图标个数
        }
        @Override   //【未进行viewHolder优化】
        public View getView(int position, View convertView, ViewGroup parent) {
            //加载组合控件
            View view=View.inflate(getApplicationContext(),R.layout.item_home_gridview,null);
            //获取组件
            TextView tv_name= (TextView) view.findViewById(R.id.tv_item_home_gv_name);
            ImageView iv_icon= (ImageView) view.findViewById(R.id.iv_item_home_gv_icon);
            //设置数据
            iv_icon.setImageResource(icons[position]);//设置图片数据
            tv_name.setText(names[position]);//设置文字数据
            if (position==0){//只判断手机防盗的位置
                /*如果有新的手机防盗名，则设置新的防盗名*/
                if (!TextUtils.isEmpty(SpTools.getString(getApplicationContext(),MyConstants.LOSTFINDNAME,""))){
                    tv_name.setText(SpTools.getString(getApplicationContext(),MyConstants.LOSTFINDNAME,""));
                }
            }
            return view;
        }
        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

    }

    private void initView() {
        setContentView(R.layout.activity_home);
        gv_menus = (GridView) findViewById(R.id.gv_home_menus);
    }
}
