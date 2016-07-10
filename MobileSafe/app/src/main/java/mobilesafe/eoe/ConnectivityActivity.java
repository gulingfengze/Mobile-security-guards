package mobilesafe.eoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import domain.AppBean;
import engine.AppManagerEngine;
import engine.ConnectivityEngine;

/**
 * Created by Lenovo on 2016/7/6.
 * 流量统计界面
 */
public class ConnectivityActivity extends Activity {

    private ListView lv_datas;
    private MyAdapter adapter;
    private List<AppBean> datas = new ArrayList<AppBean>();
    private ConnectivityManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        //测试自定环形进度条
//        setContentView(R.layout.test_shape);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
        }
    };
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                datas = AppManagerEngine.getAllApks(getApplicationContext());
                /*加上之后显示不出APK*/
//                for (int i = 0; i < datas.size(); i++) {
//                    AppBean appBean = datas.get(i);
//                    if (ConnectivityEngine.getReceive(appBean.getUid(), getApplicationContext()) == null) {
//                        datas.remove(i);
//                        i--;
//                    }
//                }
                System.out.println("成功");
                handler.obtainMessage().sendToTarget();
            }
        }).start();
    }


    private class ViewHolder {
        ImageView iv_icon;
        TextView tv_title;
        ImageView iv_seedetail;
    }

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
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(getApplicationContext(), R.layout.item_liuliang_listview_item, null);
                viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_liuliang__listview_item_icon);
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_liuliang__listview_item_title);
                viewHolder.iv_seedetail = (ImageView) convertView.findViewById(R.id.iv_liuliang__listview_lock);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final AppBean appBean = datas.get(position);
            viewHolder.iv_icon.setImageDrawable(appBean.getIcon());
            viewHolder.tv_title.setText(appBean.getAppName());
            viewHolder.iv_seedetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //接收的流量
                    String rev = ConnectivityEngine.getReceive(appBean.getUid(), getApplicationContext());

                    String snd = ConnectivityEngine.getSend(appBean.getUid(), getApplicationContext());

                    showConnectivityMess(cm.getActiveNetworkInfo().getTypeName() + "\n" + "接收的流量：" + rev + "\n发送的流量:" + snd);
                }
            });
            return convertView;
        }
    }

    /**
     * 显示流量信息的对话框
     */
    private void showConnectivityMess(String mess) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("流量信息")
                .setMessage(mess)
                .setPositiveButton("确定 ", null);
        ab.show();
    }

    private void initView() {
        setContentView(R.layout.activity_liuliang);
        lv_datas = (ListView) findViewById(R.id.lv_liuliang_datas);
        adapter = new MyAdapter();
        lv_datas.setAdapter(adapter);
        //流量信息的管理类
        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }
}
