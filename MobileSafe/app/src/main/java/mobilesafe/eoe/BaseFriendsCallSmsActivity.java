package mobilesafe.eoe;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import domain.ContantBean;
import utils.MyConstants;

/**
 * Created by Lenovo on 2016/6/19.
 * 显示所有好友信息界面（当界面只显示一个ListView时候可以继承ListActivity）
 */
public abstract class BaseFriendsCallSmsActivity extends ListActivity {
    protected static final int LOADING = 1;
    protected static final int FINISH = 2;

    //获取联系人的数据
    private List<ContantBean> datas = new ArrayList<ContantBean>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lv_datas=getListView();//获取组件
        adapter = new MyAdapter();

        //设置适配器，读取适配器的数据来显示
        lv_datas.setAdapter(adapter);
        //填充数据
        initData();
        //初始化事件
        initEvent();
    }

    /**
     * 初始化联系人列表条目的点击事件(返回数据给上一个活动)
     */
    private void initEvent() {
       lv_datas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               /*1.获取当前条目数据*/
               ContantBean contantBean=datas.get(position);
               String phone=contantBean.getPhone();
               /*2.向上一个活动传回数据*/
               Intent datas=new Intent();
               datas.putExtra(MyConstants.SAFENUMBER,phone);//保存安全号码
               setResult(1, datas);//设置数据(setResult()方法专门用于向上一个活动返回数据)
               finish();//关闭当前活动
           }
       });
    }

    /**
     * 填充加载数据（涉及到数据加载，数据可以是本地数据和网络数据，
     * 存在耗时操作，所以此处使用子线程访问数据）
     */
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*显示获取数据时候的进度*/
              Message msg=Message.obtain();
                msg.what=LOADING;
                handler.sendMessage(msg);

                SystemClock.sleep(2000);//为了展示进度条，休眠2秒



//                /*获取数据(核心代码)*/
//                datas = ReadContantsEngine.readContants(getApplicationContext());
                datas = getDatas();



                /*数据获取完成时发送加载完成的消息*/
                msg=Message.obtain();
                msg.what=FINISH;
                handler.sendMessage(msg);
            }
        }).start();
    }
        /*获取联系人界面更新*/
    private Handler handler=new Handler(){
        private ProgressDialog pd;
        @Override
        public void handleMessage(Message msg) {
          switch (msg.what){
              case LOADING://正在加载数据
                  pd = new ProgressDialog(BaseFriendsCallSmsActivity.this);
                  pd.setTitle("提示");
                  pd.setMessage("正在努力加载数据...");
                  pd.show();//显示对话框
                  break;
              case FINISH://数据加载完成
                  if (pd != null) {
                      pd.dismiss();//关闭对话框
                      pd = null;//垃圾回收释放内存
                  }
                  if (datas.size()==0){
                      Intent datas = new Intent();
                      datas.putExtra(MyConstants.SAFENUMBER, "");//保存安全号码
                      setResult(1, datas);//设置数据
                      finish();//关闭自己
                  }
            /*通过适配器通知ListView并将数据显示在ListView中*/
                  adapter.notifyDataSetChanged();
                  break;
              default:
                  break;
          }
        }
    };
    private MyAdapter adapter;
    private ListView lv_datas;
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
            ViewHolder viewHolder;
            if (convertView==null){
                view=View.inflate(getApplicationContext(),R.layout.item_friend_listview,null);
                viewHolder=new ViewHolder();//创建ViewHolder对象
                viewHolder.tv_name= (TextView) view.findViewById(R.id.tv_friends_item_name);
                viewHolder.tv_phone= (TextView) view.findViewById(R.id.tv_friends_item_phone);
                view.setTag(viewHolder);//将ViewHolder对象存储在View中
            }else {
                view=convertView;
                viewHolder= (ViewHolder) view.getTag();//重新获取ViewHolder
            }
            /*获取当前行显示的数据*/
            ContantBean bean=datas.get(position);
            viewHolder.tv_name.setText(bean.getName());
            viewHolder.tv_phone.setText(bean.getPhone());
            return view;
        }
    }
    /*用于对控件的实例进行缓存*/
    class ViewHolder{
        TextView tv_name;
        TextView tv_phone;
    }
    public abstract List<ContantBean> getDatas();
}
