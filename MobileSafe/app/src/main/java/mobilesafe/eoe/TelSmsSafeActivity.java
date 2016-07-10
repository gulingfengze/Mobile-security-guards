package mobilesafe.eoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dao.BlackDao;
import domain.BlackBean;
import domain.BlackTable;
import utils.MyConstants;

/**
 * Created by Lenovo on 2016/6/22.
 * 通讯卫士的数据处理,短信和电话(Android中使用)
 */
public class TelSmsSafeActivity extends Activity {

    protected static final int LOADING = 1;
    protected static final int FINISH = 2;
    private ListView lv_safenumbers;
    private Button bt_addSafeNumber;

    private final int MOREDATASCOUNTS = 20;// 分批加载的数据个数

    // 存放黑名单数据的容器
    private List<BlackBean> datas = new ArrayList<BlackBean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        initView();// 初始化界面

        initData();// 初始化数据

        initEvent();// 初始化事件

        initPopupWindow();// 弹出窗体，功能：让用户可以从联系人，电话记录，短信记录中添加黑名单数据

    }

    private void showPopupWindow() {
        if (pw != null && pw.isShowing()) {
            pw.dismiss();// 关闭
        } else {
            int[] location = new int[2];
            // 获取添加按钮的坐标
            bt_addSafeNumber.getLocationInWindow(location);

            // 显示动画
            contentView.startAnimation(sa);
            // 设置右上角对齐
            pw.showAtLocation(bt_addSafeNumber, Gravity.RIGHT | Gravity.TOP,
                    location[0] - (getWindowManager().getDefaultDisplay().getWidth() - bt_addSafeNumber.getWidth()),
                    location[1] + bt_addSafeNumber.getHeight());
        }
    }

    private void closePopupWindow() {
        if (pw != null && pw.isShowing()) {
            pw.dismiss();// 关闭
        }
    }

    private void initPopupWindow() {
        // 弹出窗体显示的布局

        contentView = View.inflate(getApplicationContext(),
                R.layout.popup_blacknumber_item, null);

        // 手动添加
        TextView tv_shoudong = (TextView) contentView
                .findViewById(R.id.tv_popup_black_shoudong);
        // 联系人添加
        TextView tv_contact = (TextView) contentView
                .findViewById(R.id.tv_popup_black_contacts);
        // 电话添加
        TextView tv_phonelog = (TextView) contentView
                .findViewById(R.id.tv_popup_black_phonelog);
        // 短信添加
        TextView tv_smslog = (TextView) contentView
                .findViewById(R.id.tv_popup_black_smslog);

        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_popup_black_contacts:// 从联系人导入
                        System.out.println("从联系人导入");
                        Intent intent1 = new Intent(TelSmsSafeActivity.this,
                                FriendsActivity.class);
                        // 启动联系人的界面，并获取结果
                        startActivityForResult(intent1, 1);
                        break;
                    case R.id.tv_popup_black_phonelog:// 从电话日志导入
                        System.out.println("从电话日志导入");
                        Intent intent2 = new Intent(TelSmsSafeActivity.this,
                                CalllogsActivity.class);
                        // 启动联系人的界面，并获取结果
                        startActivityForResult(intent2, 1);
                        break;
                    case R.id.tv_popup_black_shoudong:// 手动导入
                        System.out.println("手动导入");
                        showInputBlacknumberDialog("");
                        break;
                    case R.id.tv_popup_black_smslog:// 从短信导入
                        System.out.println("从短信导入");
                        Intent intent3 = new
                                Intent(TelSmsSafeActivity.this, SmslogsActivity.class);
                        //启动联系人的界面，并获取结果
                        startActivityForResult(intent3, 1);
                        break;

                    default:
                        break;
                }

                // 关闭PopupWindow
                closePopupWindow();
            }
        };
        // 给四个组件添加事件
        tv_smslog.setOnClickListener(listener);
        tv_contact.setOnClickListener(listener);
        tv_phonelog.setOnClickListener(listener);
        tv_shoudong.setOnClickListener(listener);

        // 弹出窗体
        pw = new PopupWindow(contentView, -2, -2);

        // 显示动画要有背景
        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 窗体显示的动画
        sa = new ScaleAnimation(1, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0f);
        sa.setDuration(1000);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取联系人，电话记录，短信记录等的电话
        if (data != null) {
            // 用户点击条目获取结果
            String phone = data.getStringExtra(MyConstants.SAFENUMBER);
            // 显示输入黑名单的对话框
            showInputBlacknumberDialog(phone);
        } else {
            // 用户点击返回键 事件处理
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 给每个组件设置事件
     */
    private void initEvent() {
        // 给ListView设置滑动事件
        lv_safenumbers.setOnScrollListener(new AbsListView.OnScrollListener() {

            /*
             * (non-Javadoc) onScrollStateChanged 状态改变调用此方法
             * SCROLL_STATE_FLING:
             * 惯性滑动
             * SCROLL_STATE_IDLE:
             * 滑动停止
             * SCROLL_STATE_TOUCH_SCROLL:
             * 按住滑动
             * 三种状态，每种状态改变都会触发此方法
             *
             * @see
             * android.widget.AbsListView.OnScrollListener#onScrollStateChanged
             * (android.widget.AbsListView, int)
             */
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 监控静止状态SCROLL_STATE_IDLE
                // 当出现SCROLL_STATE_IDLE的状态时候，判断是否显示最后一条数据，如果显示最后一条数据，那就加载更多的数据
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // 当出现SCROLL_STATE_IDLE的状态时候

                    // 判断是否显示最后一条数据，如果显示最后一条数据，那就加载更多的数据
                    // 获取最后显示的数据位置
                    int lastVisiblePosition = lv_safenumbers
                            .getLastVisiblePosition();
                    if (lastVisiblePosition == datas.size() - 1) {// 最后显示的位置是最后一条数据
                        // 加载更多的数据
                        initData();
                    }

                }
            }

            /*
             * (non-Javadoc) onScroll 按住滑动触发事件
             *
             * @see
             * android.widget.AbsListView.OnScrollListener#onScroll(android.
             * widget.AbsListView, int, int, int)
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub

            }
        });
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case LOADING:// 正在加载数据
                    // 显示加载数据的进度
                    pb_loading.setVisibility(View.VISIBLE);

                    // 隐藏listView
                    lv_safenumbers.setVisibility(View.GONE);

                    // 隐藏没有数据
                    tv_nodata.setVisibility(View.GONE);
                    break;

                case FINISH:// 数据加载完成
                    // 判断是否有数据
                    // 有数据
                    if (moreDatas.size() != 0) {
                        // 显示listView
                        lv_safenumbers.setVisibility(View.VISIBLE);

                        // 隐藏没有数据
                        tv_nodata.setVisibility(View.GONE);

                        // 隐藏加载数据的进度
                        pb_loading.setVisibility(View.GONE);

                        // 更新数据
                        adapter.notifyDataSetChanged();// 通知listView重新去adapter中的数据
                    } else {// 没有取到数据

                        if (datas.size() != 0) {// 分批加载数据，没有更多数据
                            Toast.makeText(getApplicationContext(), "没有更多数据", Toast.LENGTH_SHORT)
                                    .show();
                            // 显示listView
                            lv_safenumbers.setVisibility(View.VISIBLE);

                            // 隐藏没有数据
                            tv_nodata.setVisibility(View.GONE);

                            // 隐藏加载数据的进度
                            pb_loading.setVisibility(View.GONE);

                            // 更新数据
                            adapter.notifyDataSetChanged();// 通知listView重新去adapter中的数据
                            return;
                        }
                        // 没有数据
                        // 隐藏listview
                        lv_safenumbers.setVisibility(View.GONE);

                        // 显示没有数据
                        tv_nodata.setVisibility(View.VISIBLE);

                        // 隐藏加载数据的进度
                        pb_loading.setVisibility(View.GONE);
                    }
                    break;

                default:
                    break;
            }
        }

        ;
    };
    private TextView tv_nodata;
    private ProgressBar pb_loading;
    private BlackDao dao;
    private MyAdapter adapter;
    private List<BlackBean> moreDatas;// 动态加载数据的临时容器
    private AlertDialog dialog;
    private View contentView;
    private PopupWindow pw;
    private ScaleAnimation sa;

    private void initData() {
        // 从db中取黑名单数据，子线程取
        new Thread() {

            public void run() {

                // 取数据之前，发个消息显示正在加载数据的进度条
                handler.obtainMessage(LOADING).sendToTarget();

                // 加载更多数据
                moreDatas = dao.getMoreDatas(MOREDATASCOUNTS, datas.size());

                datas.addAll(moreDatas);// 把一个容器的所有数据加进来
                // 取数据完成，发消息通知取数据完成
                handler.obtainMessage(FINISH).sendToTarget();

            }
        }.start();
    }

    private void initView() {
        setContentView(R.layout.activity_telsmssafe);
        // 显示安全号码listview
        lv_safenumbers = (ListView) findViewById(R.id.lv_telsms_safenumbers);

        // 添加黑名单数据的按钮
        bt_addSafeNumber = (Button) findViewById(R.id.bt_telsms_addsafenumber);

        // 没有数据显示的文本
        tv_nodata = (TextView) findViewById(R.id.tv_telsms_nodata);

        // 正在加载数据的进度
        pb_loading = (ProgressBar) findViewById(R.id.pb_telsms_loading);

        // 黑名单业务对象
        dao = new BlackDao(getApplicationContext());
        // 黑名单的适配器
        adapter = new MyAdapter();

        // 给ListView设置适配器,取适配器的数据显示
        // 首先 调用adapter的getCount方法来获取多少条数据，如果为0，不显示任何数据，否则调用getView方法依次取出显示位置的数据
        lv_safenumbers.setAdapter(adapter);

    }

    private class ItemView {
        // 显示黑名单号码
        TextView tv_phone;

        // 显示黑名单号码拦截模式
        TextView tv_mode;

        // 删除黑名单数据的 按钮
        ImageView iv_delete;
    }

    /**
     * 黑名单数据的适配器
     */
    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            int size = datas.size();
            /*如果列表中没有数据，为空*/
            if (size == 0) {
                // 隐藏listView
                lv_safenumbers.setVisibility(View.GONE);
                // 显示没有数据
                tv_nodata.setVisibility(View.VISIBLE);
                // 隐藏加载数据的进度
                pb_loading.setVisibility(View.GONE);
            }
            return size;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            // TODO Auto-generated method stub

            ItemView itemView = null;// 声明组件封装对象 初始为null
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                        R.layout.item_telsmssafe_listview, null);

                itemView = new ItemView();
                itemView.iv_delete = (ImageView) convertView
                        .findViewById(R.id.iv_telsmssafe_listview_item_delete);
                // 显示黑名单号码
                itemView.tv_phone = (TextView) convertView
                        .findViewById(R.id.tv_telsmssafe_listview_item_number);

                // 显示黑名单号码拦截模式
                itemView.tv_mode = (TextView) convertView
                        .findViewById(R.id.tv_telsmssafe_listview_item_mode);

                // 设置标记给convertView
                convertView.setTag(itemView);
            } else {
                // 存在缓存
                itemView = (ItemView) convertView.getTag();
            }
            // 获取当前位置的数据
            final BlackBean bean = datas.get(position);

            itemView.tv_phone.setText(bean.getPhone());// 显示黑名单号码

            // 设置黑名单的模式
            switch (bean.getMode()) {
                case BlackTable.SMS:// 短信拦截
                    itemView.tv_mode.setText("短信拦截");
                    break;
                case BlackTable.TEL:// 电话拦截
                    itemView.tv_mode.setText("电话拦截");
                    break;
                case BlackTable.ALL:// 全部拦截
                    itemView.tv_mode.setText("全部拦截");
                    break;

                default:
                    break;
            }

            // 设置删除数据的事件
            itemView.iv_delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(
                            TelSmsSafeActivity.this);
                    ab.setTitle("提示");
                    ab.setMessage("是否删除该数据？");
                    ab.setPositiveButton("删除",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // 从数据库中删除当前数据
                                    dao.delete(bean.getPhone());// 取出当前行数据里的黑名单号码

                                    // 删除容器中对应的数据
                                    datas.remove(position);


                                    //剩余数据如果少于10条或者用户删除的是最后一条数据 ，再加载更多的数据
                                    if (datas.size() < 9 || position == datas.size()) {
                                        initData();//分批取数据
                                    } else {
                                        adapter.notifyDataSetChanged();//通知界面更新数据
                                    }
                                }
                            });
                    ab.setNegativeButton("取消", null);// 自动关闭对话框
                    ab.show();

                }
            });

            return convertView;
        }


    }

    /**
     * 添加黑名单号码
     *
     * @param v
     */
    public void addBlackNumber(View v) {

        showPopupWindow();
    }

    private void showInputBlacknumberDialog(String phone) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        View view = View.inflate(getApplicationContext(),
                R.layout.dialog_addblacknumber, null);

        // 黑名单号码编辑框
        final EditText et_blackNumber = (EditText) view
                .findViewById(R.id.et_telsmssafe_blacknumber);
        //设置初始的黑名单号码
        et_blackNumber.setText(phone);
        // 短信拦截复选框
        final CheckBox cb_sms = (CheckBox) view
                .findViewById(R.id.cb_telsmssafe_smsmode);

        // 短信拦截复选框
        final CheckBox cb_phone = (CheckBox) view
                .findViewById(R.id.cb_telsmssafe_phonemode);

        // 添加黑名单号码按钮
        Button bt_add = (Button) view.findViewById(R.id.bt_telsmssafe_add);

        // 取消添加黑名单号码按钮
        Button bt_cancel = (Button) view
                .findViewById(R.id.bt_telsmssafe_cancel);

        bt_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        bt_add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 添加黑名单数据
                String phone = et_blackNumber.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(getApplicationContext(), "黑名单号码不能为空", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (!cb_phone.isChecked() && !cb_sms.isChecked()) {
                    // 两个拦截都没选
                    Toast.makeText(getApplicationContext(), "至少选择一种拦截模式", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                int mode = 0;
                if (cb_phone.isChecked()) {
                    mode |= BlackTable.TEL;// 设置电话拦截模式
                }
                if (cb_sms.isChecked()) {
                    mode |= BlackTable.SMS;// 设置电话拦截模式
                }

                // 界面显示新增的数据
                BlackBean bean = new BlackBean();
                bean.setMode(mode);
                bean.setPhone(phone);

                dao.add(bean);// 添加数据到黑名单表中
                // 如果新增的数据已经存在
                datas.remove(bean);// 该删除方法要靠equals和hashCode两个方法共同判断数据是否一致
                datas.add(0, bean);// 添加数据到List中

                // 让listview显示第一条数据
                // lv_safenumbers.setSelection(0);
                adapter = new MyAdapter();
                lv_safenumbers.setAdapter(adapter);
                dialog.dismiss();

                // 显示listView
                lv_safenumbers.setVisibility(View.VISIBLE);
                // 隐藏没有数据
                tv_nodata.setVisibility(View.GONE);
                // 隐藏加载数据的进度
                pb_loading.setVisibility(View.GONE);

            }
        });
        ab.setView(view);

        // 创建对话框
        dialog = ab.create();
        dialog.show();// 显示对话框
    }
}


//protected static final int LOADING = 1;
//    protected static final int FINISH = 2;
//    private ListView lv_safeNumbers;
//    private Button bt_addSafeNumber;
//    private TextView tv_nodata;
//    private ProgressBar pb_loading;
//    private MyAdapter adapter;
//    private List<BlackBean> datas = new ArrayList<BlackBean>();//存放黑名单的数据容器
//    private BlackDao dao;
//    private final int MOREDATASCOUNTS = 20;// 分批加载的数据个数
//    private AlertDialog dialog;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();//初始化界面
//        initData();//初始化数据
//        initEvent();// 初始化事件
//        initPopupWindow();// 弹出窗体，功能：让用户可以从联系人，电话记录，短信记录中添加黑名单数据
//    }
//
//    private void initPopupWindow() {
//
//    }
//
//    /**
//     * 给每个组件设置事件
//     */
//    private void initEvent() {
//        //给ListView设置滑动事件
//        lv_safeNumbers.setOnScrollListener(new AbsListView.OnScrollListener() {
//
//            /*
//             * (non-Javadoc)
//             * onScrollStateChanged---状态改变调用此方法
//             * SCROLL_STATE_FLING:惯性滑动
//             * SCROLL_STATE_IDLE:滑动停止
//             * SCROLL_STATE_TOUCH_SCROLL: 按住滑动
//             * 三种状态，每种状态改变都会触发此方法
//             * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
//             */
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                   /*
//                   *监控静止状态SCROLL_STATE_IDLE，当出现此状态时，判断是否显示最后一条数据，
//                   * 如果显示最后一条数据，那就加载更多数据
//                   */
//                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//                    int lastVisiblePosition = lv_safeNumbers.getLastVisiblePosition();//获取最后显示的数据位置
//                    if (lastVisiblePosition == datas.size() - 1) {
//                        initData();//加载更多数据
//                    }
//                }
//            }
//
//            /*
//             * (non-Javadoc)
//             * onScroll---按住滑动触发事件
//             * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
//             */
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//            }
//        });
//
//    }
//
//    /**
//     * 对UI操作
//     */
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case LOADING://正在加载数据时
//                    pb_loading.setVisibility(View.VISIBLE);//显示数据加载进度条
//                    tv_nodata.setVisibility(View.GONE);//隐藏没有数据文本
//                    lv_safeNumbers.setVisibility(View.GONE);//隐藏安全号码列表
//                    break;
//                case FINISH://加载数据完成时，判断是否有数据
//                /* 1.有数据*/
//                    if (moreDatas.size() != 0) {
//                        lv_safeNumbers.setVisibility(View.VISIBLE);//显示安全号码列表
//                        tv_nodata.setVisibility(View.GONE);//隐藏没有数据文本
//                        pb_loading.setVisibility(View.GONE);//隐藏数据加载进度条
//
//                        adapter.notifyDataSetChanged();//通知listView重新取adapter中的数据（更新数据）
//                    }
//                /* 2.没有数据*/
//                    else {
//                        if (datas.size() != 0) {// 分批加载数据，没有更多数据
//                            Toast.makeText(getApplicationContext(), "没有更多数据", Toast.LENGTH_SHORT)
//                                    .show();
//                            return;
//                        }
//                        tv_nodata.setVisibility(View.VISIBLE);//显示没有数据文本
//                        lv_safeNumbers.setVisibility(View.GONE);//隐藏安全号码列表
//                        pb_loading.setVisibility(View.GONE);//隐藏加载数据的进度条
//                    }
//                default:
//                    break;
//            }
//        }
//    };
//    private List<BlackBean> moreDatas;// 动态加载数据的临时容器
//
//    /**
//     * 加载黑名单（耗时操作），在子线程中进行操作
//     */
//    private void initData() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                handler.obtainMessage(LOADING).sendToTarget();//取数据之前，发个消息显示在加载数据的进度条
//                // 加载更多数据
//                moreDatas = dao.getMoreDatas(MOREDATASCOUNTS, datas.size());
//                datas.addAll(moreDatas);// 把一个容器的所有数据加进来
//                handler.obtainMessage(FINISH).sendToTarget();//取数据完成，发消息通知数据完成
//            }
//        }).start();
//    }
//
//    private void initView() {
//        setContentView(R.layout.activity_telsmssafe);
//        bt_addSafeNumber = (Button) findViewById(R.id.bt_telsms_addsafenumber);//添加黑名单数据按钮
//        lv_safeNumbers = (ListView) findViewById(R.id.lv_telsms_safenumbers);//安全号码列表
//        tv_nodata = (TextView) findViewById(R.id.tv_telsms_nodata);//没有数据文本
//        pb_loading = (ProgressBar) findViewById(R.id.pb_telsms_loading);//加载进度条
//        dao = new BlackDao(getApplicationContext());//黑名单业务对象
///*首先 调用adapter的getCount方法来获取多少条数据，如果为0，不显示任何数据，否则调用getView方法依次取出显示位置的数据*/
//        adapter = new MyAdapter();//黑名单适配器
//        lv_safeNumbers.setAdapter(adapter);//给ListView设置适配器,取适配器的数据显示
//
//    }
//
//    /*用于对控件的实例进行缓存*/
//    class ViewHolder {
//        TextView tv_phone;//显示黑名单号码
//        TextView tv_mode;//显示黑名单号码拦截模式
//        ImageView iv_delete;//删除黑名单数据的 按钮
//    }
//    /**
//     * 黑名单数据适配器
//     */
//    private class MyAdapter extends BaseAdapter {
//        @Override
//        public int getCount() {
//            return datas.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//            View view;
//            ViewHolder viewHolder=null;//创建ViewHolder对象
//            if (convertView == null) {
//                view = View.inflate(getApplicationContext(), R.layout.item_telsmssafe_listview, null);
//                viewHolder = new ViewHolder();
//                viewHolder.tv_phone = (TextView) findViewById(R.id.tv_telsmssafe_listview_item_number);
//                viewHolder.tv_mode = (TextView) findViewById(R.id.tv_telsmssafe_listview_item_mode);
//                viewHolder.iv_delete = (ImageView) findViewById(R.id.iv_telsmssafe_listview_item_delete);
//                view.setTag(viewHolder);//将ViewHolder对象存储在View中
//            } else {
//                view = convertView;
//                viewHolder = (ViewHolder) view.getTag();//重新获取ViewHolder
//            }
//
//            final BlackBean blackBean = datas.get(position);//获取当前行显示的数据
//            viewHolder.tv_phone.setText(blackBean.getPhone());//显示黑名单号码
//            /*设置黑名单的模式*/
//            switch (blackBean.getMode()) {
//                case BlackTable.SMS:
//                    viewHolder.tv_mode.setText("短信拦截");
//                    break;
//                case BlackTable.TEL:
//                    viewHolder.tv_mode.setText("电话拦截");
//                    break;
//                case BlackTable.ALL:
//                    viewHolder.tv_mode.setText("全部拦截");
//                    break;
//                default:
//                    break;
//            }
//            /*设置删除数据的事件*/
//            viewHolder.iv_delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    AlertDialog.Builder ab = new AlertDialog.Builder(TelSmsSafeActivity.this);
//                    ab.setTitle("提示")
//                            .setMessage("是否删除该数据")
//                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // 1.从数据库中删除当前数据
//                                    dao.delete(blackBean.getPhone());// 取出当前行数据里的黑名单号码
//                                    // 2.删除容器中对应的数据
//                                    datas.remove(position);
//                                    //3. 通知界面更新数据，让用户看到删除数据不存在
//                                    adapter.notifyDataSetChanged();// 只是让listView重新取当前显示位置的数据
//
//                                }
//                            }).setNegativeButton("取消", null)
//                            .show();
//                }
//            });
//
//            return view;
//        }
//    }
//
//    /**
//     * 添加黑名单号码按钮点击事件
//     *
//     * @param v
//     */
//    public void addBlackNumber(View v) {
//        AlertDialog.Builder ab = new AlertDialog.Builder(this);
//        View view = View.inflate(getApplicationContext(),
//                R.layout.dialog_addblacknumber, null);
//
//        // 黑名单号码编辑框
//        final EditText et_blackNumber = (EditText) view
//                .findViewById(R.id.et_telsmssafe_blacknumber);
//
//        // 短信拦截复选框
//        final CheckBox cb_sms = (CheckBox) view
//                .findViewById(R.id.cb_telsmssafe_smsmode);
//
//        // 短信拦截复选框
//        final CheckBox cb_phone = (CheckBox) view
//                .findViewById(R.id.cb_telsmssafe_phonemode);
//
//        // 添加黑名单号码按钮
//        Button bt_add = (Button) view.findViewById(R.id.bt_telsmssafe_add);
//
//        // 取消添加黑名单号码按钮
//        Button bt_cancel = (Button) view
//                .findViewById(R.id.bt_telsmssafe_cancel);
//        ab.setView(view);
//
//        bt_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        bt_add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String phone = et_blackNumber.getText().toString().trim();//获取黑名单数据
//                if (TextUtils.isEmpty(phone)) {
//                    Toast.makeText(getApplicationContext(), "黑名单号码不能为空", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (!cb_phone.isChecked() && !cb_sms.isChecked()) {
//                    Toast.makeText(getApplicationContext(), "至少选择一种拦截模式", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                int mode = 0;
//                if (cb_phone.isChecked()) {
//                    mode |= BlackTable.TEL;//设置电话拦截模式
//                }
//                if (cb_sms.isChecked()) {
//                    mode |= BlackTable.SMS;//设置短信拦截模式
//                }
//                /*界面显示用户新增的数据*/
//                BlackBean blackBean = new BlackBean();
//                blackBean.setMode(mode);
//                blackBean.setPhone(phone);
//
//                dao.add(blackBean);// 添加数据到黑名单表中
//                datas.remove(blackBean);// 如果新增的数据已经存在,先删除(该删除方法要靠equals和hashCode两个方法共同判断数据是否一致)
//                datas.add(0, blackBean);// 再添加数据到列表中
//
//                adapter = new MyAdapter();
//                lv_safeNumbers.setAdapter(adapter);
//                dialog.dismiss();
//            }
//        });
//
//        ab.setView(view);
//        dialog = ab.create();
//        dialog.show();
//    }


//}
