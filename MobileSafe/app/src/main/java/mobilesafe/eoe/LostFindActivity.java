package mobilesafe.eoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import utils.MyConstants;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/6/12.
 * 手机防盗界面
 */
public class LostFindActivity extends Activity {
    private AlertDialog dialog;//修改手机防盗名称
    private boolean isShowMenu = false;
    private View popupView;
    private ScaleAnimation sa;
    private PopupWindow pw;
    private RelativeLayout rl_root;
    private LinearLayout ll_bottom_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果第一次访问该界面，要先进入设置向导界面
        if (SpTools.getBoolean(getApplicationContext(), MyConstants.ISSETUP,false)){
            //进入过设置向导界面，直接显示手机防盗界面
             initView();//手机防盗界面
             initPopupView();// 初始化修改名的界面
             initPopupWindow();// 初始化弹出窗体
        }else {
           //进入设置向导界面
            Intent intent=new Intent(this,Setup1Activity.class);
            startActivity(intent);
            finish();//关闭设置向导界面，不然点击返回键时又回到该界面。
        }

    }

    private void initPopupWindow() {
        // 弹出窗体
        pw = new PopupWindow(popupView, -2, -2);
        pw.setFocusable(true);// 获取焦点
        // 窗体显示的动画
         sa = new ScaleAnimation(1, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0f);
        sa.setDuration(1000);
    }

    private void initPopupView() {
        popupView = View.inflate(getApplicationContext(),
                R.layout.dialog_modify_name, null);
        final EditText et_name= (EditText) popupView.findViewById(R.id.et_dialog_lostfind_modify_name);
        Button bt_modify= (Button) popupView.findViewById(R.id.bt_dialog_lostfind_modify);
        Button bt_cancel= (Button) popupView.findViewById(R.id.bt_dialog_lostfind_cancel);
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
            }
        });
        bt_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString().trim();//获取修改名
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(LostFindActivity.this, "修改名不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                //将新名称保存到sp中
                SpTools.putString(getApplicationContext(), MyConstants.LOSTFINDNAME, name);
                pw.dismiss();
                Toast.makeText(LostFindActivity.this, "名称修改成功", Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * @param v
     * 重新进入设置向导界面点击事件
     */
    public void enterSetup(View v){
     Intent intent=new Intent(this,Setup1Activity.class);
        startActivity(intent);
        finish();
    }
    /**
     *手机防盗界面
     */
    private void initView() {
        setContentView(R.layout.activity_lostfind);
        ll_bottom_menu = (LinearLayout) findViewById(R.id.ll_lostfind_menu_bottom);
        rl_root = (RelativeLayout) findViewById(R.id.rl_lostfind_root);// 根布局
    }
    /*
     * (non-Javadoc)
     * 创建菜单
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);创建自定义菜单，将系统默认菜单注销
        return true;
    }
    /*
     * (non-Javadoc)
     * 处理菜单事件
     * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()){
            case R.id.mn_modify_name:
                Toast.makeText(LostFindActivity.this, "修改手机防盗名", Toast.LENGTH_SHORT).show();
                showModifyNameDialog();//弹出对话框，让用户输入新的手机防盗名
                break;
            case R.id.mn_test_menu:
                Toast.makeText(LostFindActivity.this, "测试手机防盗名", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * 修改手机防盗名称的对话框
     */
    private void showModifyNameDialog() {
        AlertDialog.Builder ab=new AlertDialog.Builder(this);
        dialog=ab.create();//创建对话框
        dialog.show();//显示对话框
    }


    /**
     * @param keyCode
     * @param event
     * @return
     * 处理menu键的事件
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_MENU){
//           if (isShowMenu){
//               ll_bottom_menu.setVisibility(View.VISIBLE);//显示菜单
//           }else {
//               ll_bottom_menu.setVisibility(View.GONE);//不显示菜单
//           }
//            isShowMenu=!isShowMenu;
//        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (pw != null && pw.isShowing()) {
                pw.dismiss();
            } else {

                // 设置弹出窗体的背景
                pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popupView.startAnimation(sa);
                // 设置弹出窗体显示的位置
                int height = getWindowManager().getDefaultDisplay().getHeight();
                int width = getWindowManager().getDefaultDisplay().getWidth();
                pw.showAtLocation(rl_root, Gravity.LEFT | Gravity.TOP,
                        width / 4, height / 4);

            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
