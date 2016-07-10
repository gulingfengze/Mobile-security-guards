package mobilesafe.eoe;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import domain.TaskBean;
import engine.TaskManagerEngine;
import utils.MyConstants;
import utils.SpTools;

/**
 * Created by Lenovo on 2016/7/2.
 * 进程管理界面
 */
public class TaskManagerActivity extends Activity {
    protected static final int LOADING = 1;
    protected static final int FINISH = 2;
    private TextView tv_tasknumber;
    private TextView tv_meminfo;
    private ListView lv_taskdatas;
    private TextView tv_list_tag;
    private ProgressBar pb_loading;
    private List<TaskBean> sysTasks = new CopyOnWriteArrayList<TaskBean>();//系统进程数据
    private List<TaskBean> userTasks = new CopyOnWriteArrayList<TaskBean>();//用户进程数据
    private long availMemSize = 0;//可用内存大小
    private long totalMemSize = 0;//总内存大小
    private MyAdapter adapter;
    private ActivityManager am;
    private Object obj = new Object();
    private InitDataClass initData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化数据封装对象
        initData = new InitDataClass();
        initView();//初始化界面
        initData();//设置数据
        initEvent();//初始化事件
    }

    private class ViewHolder {
        ImageView iv_icon;//图标
        TextView tv_title; //名字
        TextView tv_memsize;//占用内存大小
        CheckBox cb_checked;//是否选择
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            /*“显示系统进程”*/
            setTileMessage();
            if (!SpTools.getBoolean(getApplicationContext(), MyConstants.SHOWSYSTEM, false)) {
                return userTasks.size() + 1;  //不显示系统进程
            }

            return sysTasks.size() + 1 + userTasks.size() + 1;
        }

        @Override
        public TaskBean getItem(int position) {
            TaskBean bean = null;
            if (position == 0 || position == userTasks.size() + 1) {
                return bean;
            }
            //判断position 如果是用户apk
            if (position <= userTasks.size()) {
                bean = userTasks.get(position - 1);
            } else {
                //系统apk
                bean = sysTasks.get(position - userTasks.size() - 2);
            }

            return bean;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TaskBean bean = getItem(position);

            if (position == 0) {// 用户apk的标签
                TextView tv_userTable = new TextView(getApplicationContext());
                tv_userTable.setText("个人软件(" + userTasks.size() + ")");
                tv_userTable.setTextColor(Color.WHITE);// 文字为白色
                tv_userTable.setBackgroundColor(Color.GRAY);// 文字背景为灰色
                return tv_userTable;
            } else if (position == userTasks.size() + 1) {// 系统apk标签
                TextView tv_userTable = new TextView(getApplicationContext());
                tv_userTable.setText("系统软件(" + sysTasks.size() + ")");
                tv_userTable.setTextColor(Color.WHITE);// 文字为白色
                tv_userTable.setBackgroundColor(Color.GRAY);// 文字背景为灰色
                return tv_userTable;
            } else {
                ViewHolder holder = new ViewHolder();//界面缓存
                if (convertView != null && convertView instanceof RelativeLayout) {
                    holder = (ViewHolder) convertView.getTag();
                } else {
                    convertView = View.inflate(getApplicationContext(), R.layout.item_taskmanager_listview_item, null);
                    holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_taskmanager_listview_item_icon);
                    holder.tv_title = (TextView) convertView.findViewById(R.id.tv_taskmanager_listview_item_title);
                    holder.tv_memsize = (TextView) convertView.findViewById(R.id.tv_taskmanager_listview_item_memsize);
                    holder.cb_checked = (CheckBox) convertView.findViewById(R.id.tv_taskmanager_listview_item_checked);
                    convertView.setTag(holder);//绑定Tag
                }


                /*点击列表条目进行复选框的设置*/
                final ViewHolder mHolder = holder;
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {//如果是软件本身，则取消复选框选中状态
                        if (bean.getPackName().equals(getPackageName())) {
                            mHolder.cb_checked.setChecked(false);
                        } else {
                            mHolder.cb_checked.setChecked(!mHolder.cb_checked.isChecked());//设置复选框的反选操作
                        }
                    }
                });


                /*设置数据*/
                holder.iv_icon.setImageDrawable(bean.getIcon());// 设置图标
                holder.tv_memsize.setText(Formatter.formatFileSize(getApplicationContext(), bean.getMemSize()));//设置占用的内存大小
                holder.tv_title.setText(bean.getName());// 设置名字

                /*记录复选框的状态,记录bean中*/
                //给复选框加事件，记录复选框的状态
                holder.cb_checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        bean.setIsChecked(isChecked);//记录复选框的状态
                        System.out.println("选中复选框");
                    }
                });
                holder.cb_checked.setChecked(bean.isChecked());//从bean取出复选框的状态显示

                 /*判断是不是软件（手机卫士）本身,如果是本身，则让checkbox隐藏*/
                if (bean.getPackName().equals(getPackageName())) {
                    holder.cb_checked.setVisibility(View.GONE);//隐藏复选框
                }else {
                    holder.cb_checked.setVisibility(View.VISIBLE);//显示复选框
                }

                return convertView;
            }

        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADING://加载数据进程显示
                    pb_loading.setVisibility(View.VISIBLE);
                    lv_taskdatas.setVisibility(View.GONE);
                    tv_list_tag.setVisibility(View.GONE);
                    break;
                case FINISH://数据加载完成
                    pb_loading.setVisibility(View.GONE);
                    lv_taskdatas.setVisibility(View.VISIBLE);
                    tv_list_tag.setVisibility(View.VISIBLE);

                    setTileMessage();//设置运行中进程的个数

                    adapter.notifyDataSetChanged();//数据的通知
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 设置运行进程个数(标题信息)
     */
    private void setTileMessage() {
        if (SpTools.getBoolean(getApplicationContext(), MyConstants.SHOWSYSTEM, false)) {
            tv_tasknumber.setText("运行中的进程:" + (sysTasks.size() + userTasks.size()));
        } else {
            tv_tasknumber.setText("运行中的进程:" + (userTasks.size()));
        }
                                          /*设置内存的使用信息*/
        // 格式化显示可用内存
        String availMemFormatter = Formatter.formatFileSize(getApplicationContext(), availMemSize);
        // 格式化显示可用内存
        String totalMemFormatter = Formatter.formatFileSize(getApplicationContext(), totalMemSize);
        // 设置内存的使用信息
        tv_meminfo.setText("可用/总内存:" + availMemFormatter + "/" + totalMemFormatter);
    }
    private void initEvent() {
      /*给ListView添加滚动事件*/
        lv_taskdatas.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            /**
             * @param view
             * @param firstVisibleItem
             * @param visibleItemCount
             * @param totalItemCount
             * 按住滑动
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //如果显示用户进程，标签要显示用户进程的Tag
                if (firstVisibleItem <= userTasks.size()) {
                    tv_list_tag.setText("用户进程(" + userTasks.size() + ")");
                } else {
                    tv_list_tag.setText("系统进程(" + sysTasks.size() + ")");
                }
            }
        });
    }
    private class InitDataClass {
        public synchronized void initData(){
            // 1.发送加载数据进度的消息
            handler.obtainMessage(LOADING).sendToTarget();

           /*获取所有运行中的进程数据*/
            List<TaskBean> allTaskDatas = TaskManagerEngine
                    .getAllRunningTaskInfos(getApplicationContext());
            availMemSize = TaskManagerEngine
                    .getAvailMemSize(getApplicationContext());
            totalMemSize = TaskManagerEngine
                    .getTotalMemSize(getApplicationContext());
            SystemClock.sleep(500);
            sysTasks.clear();
            userTasks.clear();
             /* 分发数据*/
            for (TaskBean taskBean : allTaskDatas) {
                if (taskBean.isSystem()) {
                    // 系统进程
                    sysTasks.add(taskBean);
                } else {
                    // 用户进程
                    userTasks.add(taskBean);
                }
            }
            System.out.println(allTaskDatas.size() + ":" + sysTasks.size()
                    + ":" + userTasks.size());

            //2.发送加载数据完成消息
            handler.obtainMessage(FINISH).sendToTarget();
        }
    }
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //通过对象来初始化数据
                initData.initData();
            }
        }).start();
    }

    private void initView() {
        setContentView(R.layout.activity_taskmanager);
        //显示进程的个数
        tv_tasknumber = (TextView) findViewById(R.id.tv_taskmanager_tasknumber);
        //显示使用的内存信息
        tv_meminfo = (TextView) findViewById(R.id.tv_taskmanager_meminfo);
        //显示所有进程的信息
        lv_taskdatas = (ListView) findViewById(R.id.lv_taskmanager_appdatas);
        //进程数据的标签
        tv_list_tag = (TextView) findViewById(R.id.tv_taskmanager_listview_lable);
        //加载进程数据的 进度
        pb_loading = (ProgressBar) findViewById(R.id.pb_taskmanager_loading);
        /*设置并加载listView的适配器*/
        adapter = new MyAdapter();
        lv_taskdatas.setAdapter(adapter);
        //Activity管理器
        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    }



    /**
     * @param view 全选按钮事件
     */
    public void selectAll(View view) {
        /*遍历所有的APK*/
        //1.遍历用户APK,如果是软件本身，则
        for (TaskBean bean : userTasks) {
            if (bean.getPackName().equals(getPackageName())) {
                bean.setIsChecked(false); //如果是软件本身，则取消复选框选中状态
                continue;
            } else {
                bean.setIsChecked(true);
            }

        }
        //2.遍历系统APK
        for (TaskBean bean : sysTasks) {
            bean.setIsChecked(true);
        }
        adapter.notifyDataSetChanged();//更新列表界面
    }

    /**
     * @param view 反选按钮事件
     */
    public void fanSelect(View view) {
        /*遍历所有的APK*/
        //1.遍历用户APK,如果是软件本身，则
        for (TaskBean bean : userTasks) {
            if (bean.getPackName().equals(getPackageName())) {
                bean.setIsChecked(false); //如果是软件本身，则取消复选框选中状态
            } else {
                bean.setIsChecked(!bean.isChecked());
            }

        }
        //2.遍历系统APK
        for (TaskBean bean : sysTasks) {
            bean.setIsChecked(!bean.isChecked());
        }
        adapter.notifyDataSetChanged();//更新列表界面
    }

    /**
     * @param v 清理选中的进程
     */
    public void clearTask(View v) {
         /*有些进程删除不掉，增强用户体验除了自己都可以删掉
         让用户看到清理了几个进程，释放了多少内存
         每个用户选中的进程都要清理*/
        long clearMem = 0;// 记录内存的大小
        int clearNum = 0;// 记录清理多少个进程

        for (TaskBean bean : userTasks) {//清理用户
            if (bean.isChecked()) {
                clearNum++;//清理的个数累计
                clearMem += bean.getMemSize();//清理内存大小累计（byte）
                am.killBackgroundProcesses(bean.getPackName());//清理

                userTasks.remove(bean);//从容器中删除数据
            }
        }
        for (TaskBean bean : sysTasks) {//清理系统
            if (bean.isChecked()) {
                clearNum++;//清理的个数累计
                clearMem += bean.getMemSize();//清理内存大小累计（byte）
                am.killBackgroundProcesses(bean.getPackName());//清理

                sysTasks.remove(bean);//从容器中删除数据
            }
        }
        Toast.makeText(getApplicationContext(), "清理了" + clearNum + "个进程，" +
                        "释放了" + Formatter.formatFileSize(getApplicationContext(), clearMem),
                Toast.LENGTH_LONG).show();

        availMemSize += clearMem;//增加可用内存
        setTileMessage();//更新标题的信息
        adapter.notifyDataSetChanged();//通知界面listView的更新
    }


    /**
     * @param view
     * 打开进程设置界面
     */
    public void setting(View view) {
        Intent setting=new Intent(this,TaskManagerSettingActivity.class);
        startActivity(setting);
    }
}
