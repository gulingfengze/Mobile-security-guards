package mobilesafe.eoe;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dao.BlackDao;
import domain.BlackBean;
import domain.BlackTable;

/**
 * Created by Lenovo on 2016/6/22.
 * 通讯卫士的数据处理,短信和电话(Web中使用)
 */
public class TelSmsSafeActivityPage extends Activity {
    protected static final int LOADING = 1;
    protected static final int FINISH = 2;
    private ListView lv_safeNumbers;
    private Button bt_addSafeNumber;
    private TextView tv_nodata;
    private ProgressBar pb_loading;
    private MyAdapter adapter;
    private List<BlackBean> datas = new ArrayList<BlackBean>();//存放黑名单的数据容器
    private BlackDao dao;
    private EditText et_gotoPage;
    private int totalPages;// 总页数
    private int currentPage = 1;// 当前页的数据,默认1
    private final int perPage = 20;// 每页显示20条数据
    private TextView tv_totalPages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();//初始化界面

        initData();//初始化数据
    }

    /**
     * 对UI操作
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADING://正在加载数据时
                    pb_loading.setVisibility(View.VISIBLE);//显示数据加载进度条
                    tv_nodata.setVisibility(View.GONE);//隐藏没有数据文本
                    lv_safeNumbers.setVisibility(View.GONE);//隐藏安全号码列表
                    break;
                case FINISH://加载数据完成时，判断是否有数据
                /* 1.有数据*/
                    if (datas.size() != 0) {
                        lv_safeNumbers.setVisibility(View.VISIBLE);//显示安全号码列表
                        tv_nodata.setVisibility(View.GONE);//隐藏没有数据文本
                        pb_loading.setVisibility(View.GONE);//隐藏数据加载进度条

                        adapter.notifyDataSetChanged();//通知listView重新取adapter中的数据（更新数据）

                        tv_totalPages.setText(currentPage + "/" + totalPages);//初始化总页数和当前页的值
                    }
                /* 2.没有数据*/
                    else {
                        tv_nodata.setVisibility(View.VISIBLE);//显示没有数据文本
                        lv_safeNumbers.setVisibility(View.GONE);//隐藏安全号码列表
                        pb_loading.setVisibility(View.GONE);//隐藏加载数据的进度条
                    }
                default:
                    break;
            }
        }
    };

    /**
     * 加载黑名单（耗时操作），在子线程中进行操作
     */
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                handler.obtainMessage(LOADING).sendToTarget();//取数据之前，发个消息显示在加载数据的进度条
//                SystemClock.sleep(2000);//休眠2秒
//                datas = dao.getAllDatas();//获取所有数据

                datas = dao.getPageDatas(currentPage, perPage); // 获取当前页的数据currentPage当前页，perPage每页数据个数
                totalPages = dao.getTotalPages(perPage);// 获取总页数
                handler.obtainMessage(FINISH).sendToTarget();//取数据完成，发消息通知数据完成
            }
        }).start();
    }

    private void initView() {
        setContentView(R.layout.activity_telsmssafe);
        bt_addSafeNumber = (Button) findViewById(R.id.bt_telsms_addsafenumber);//添加黑名单数据按钮
        lv_safeNumbers = (ListView) findViewById(R.id.lv_telsms_safenumbers);//安全号码列表
        tv_nodata = (TextView) findViewById(R.id.tv_telsms_nodata);//没有数据文本
        pb_loading = (ProgressBar) findViewById(R.id.pb_telsms_loading);//加载进度条
        et_gotoPage = (EditText) findViewById(R.id.et_telsms_gotopage);// 输入的跳转页
        tv_totalPages = (TextView) findViewById(R.id.tv_telsms_totalpages); // 总页数
        dao = new BlackDao(getApplicationContext());//黑名单业务对象
/*首先 调用adapter的getCount方法来获取多少条数据，如果为0，不显示任何数据，否则调用getView方法依次取出显示位置的数据*/
        adapter = new MyAdapter();//黑名单适配器
        lv_safeNumbers.setAdapter(adapter);//给Listview设置适配器,取适配器的数据显示

    }

    /*用于对控件的实例进行缓存*/
    class ViewHolder {
        TextView tv_phone;//显示黑名单号码
        TextView tv_mode;//显示黑名单号码拦截模式
        ImageView iv_delete;//删除黑名单数据的 按钮
    }

    /**
     * 黑名单数据适配器
     */
    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder viewHolder;//创建ViewHolder对象
            if (convertView == null) {
                view = View.inflate(getApplicationContext(), R.layout.item_telsmssafe_listview, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_phone = (TextView) findViewById(R.id.tv_telsmssafe_listview_item_number);
                viewHolder.tv_mode = (TextView) findViewById(R.id.tv_telsmssafe_listview_item_mode);
                viewHolder.iv_delete = (ImageView) findViewById(R.id.iv_telsmssafe_listview_item_delete);
                view.setTag(viewHolder);//将ViewHolder对象存储在View中
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();//重新获取ViewHolder
            }

            BlackBean blackBean = datas.get(position);//获取当前行显示的数据
            viewHolder.tv_phone.setText(blackBean.getPhone());//显示黑名单号码
            /*设置黑名单的模式*/
            switch (blackBean.getMode()) {
                case BlackTable.SMS:
                    viewHolder.tv_mode.setText("短信拦截");
                    break;
                case BlackTable.TEL:
                    viewHolder.tv_mode.setText("电话拦截");
                    break;
                case BlackTable.ALL:
                    viewHolder.tv_mode.setText("全部拦截");
                    break;
                default:
                    break;
            }
            return view;
        }
    }

    /**
     * 下一页
     *
     * @param v
     */                          /*点击后程序崩溃*/
    public void nextPage(View v) {
        // 10页,最后一页，再点击下页： 1，给用户提醒最后一页，2，回到第一页
        currentPage++;// 下一页

        // 处理越界
        currentPage = currentPage % totalPages;

        // 取当前页的数据
        initData();

    }

    /**
     * 上一页
     *
     * @param v
     */
    public void prevPage(View v) {
        // 10页,第一页，再点击上页： 1，给用户提醒第一页，2，回到尾页
        currentPage--;// 下一页

        if (currentPage == 0) {
            currentPage = totalPages;//显示最后一页
        }

        // 取当前页的数据
        initData();
    }

    /**
     * 尾页
     *
     * @param v
     */
    public void endPage(View v) {
        //设置当前页为尾页
        currentPage = totalPages;
        //取数据
        initData();
    }

    /**
     * 跳转
     *
     * @param v
     */
    public void jumpPage(View v) {
        //获取跳转页的页码
        String jumpPageStr = et_gotoPage.getText().toString().trim();
        if (TextUtils.isEmpty(jumpPageStr)) {
            Toast.makeText(getApplicationContext(), "跳转页不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        //把字符串的页码转成整数
        int jumpPage = Integer.parseInt(jumpPageStr);

        if (jumpPage >= 1 && jumpPage <= totalPages) {
            //把跳转页设置给当前页
            currentPage = jumpPage;
            initData();//初始数据
        } else {
            Toast.makeText(getApplicationContext(), "请输入正确页码", Toast.LENGTH_SHORT).show();
            return;
        }
    }

}
