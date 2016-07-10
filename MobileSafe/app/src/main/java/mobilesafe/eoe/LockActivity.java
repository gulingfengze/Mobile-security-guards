package mobilesafe.eoe;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import dao.LockedDao;
import domain.LockedTable;
import fragment.LockedFragment;
import fragment.UnlockedFragment;

/**
 * Created by Lenovo on 2016/7/4.
 * "高级工具"功能的‘程序锁’
 * 程序锁界面Fragment
 */
public class LockActivity extends FragmentActivity {

    private TextView tv_locked;
    private TextView tv_unlock;
    private FrameLayout fl_content;
    private UnlockedFragment unlockedFragment;
    private LockedFragment lockedFragment;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initView();// 初始化界面

        initData();// 初始化数据

        initEvent();// 初始化事件
    }

    private void initEvent() {
        /*注册内容观察者*/
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LockedDao dao = new LockedDao(getApplicationContext());
                        //读取dao层读取数据
                        List<String> allLockedDatas = dao.getAllLockedDatas();

                        lockedFragment.setAllLockedPacks(allLockedDatas);
                        unlockedFragment.setAllLockedPacks(allLockedDatas);
                    }
                }).start();
                super.onChange(selfChange);
            }
        };
        getContentResolver().registerContentObserver(LockedTable.uri, true, observer);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = fm.beginTransaction();//开启事务
                /*替换,默认显示未加锁的界面,把未加锁的fragment替换掉FrameLayout*/
                if (v.getId() == R.id.tv_lockedactivity_locked) {  //已加锁
                    transaction.replace(R.id.fl_lockedactivity_content, lockedFragment);
                    tv_locked.setBackgroundResource(R.drawable.tab_right_pressed);//按下
                    tv_unlock.setBackgroundResource(R.drawable.tab_left_default);//默认 不按下
                } else {//未加锁
                    transaction.replace(R.id.fl_lockedactivity_content, unlockedFragment);
                    tv_locked.setBackgroundResource(R.drawable.tab_right_default);//不按下
                    tv_unlock.setBackgroundResource(R.drawable.tab_left_pressed);//按下
                }
                transaction.commit();// 3,提交事物
            }
        };
        tv_unlock.setOnClickListener(listener);
        tv_locked.setOnClickListener(listener);
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LockedDao dao = new LockedDao(getApplicationContext());
                //读取dao层读取数据
                List<String> allLockedDatas = dao.getAllLockedDatas();

                lockedFragment.setAllLockedPacks(allLockedDatas);
                unlockedFragment.setAllLockedPacks(allLockedDatas);
            }
        }).start();
        /*动态添加碎片主要分为5步
        *  1.创建待添加的碎片实例
        *  2.获取FragmentManager，在活动中直接调用getFragmentManager()方法得到
        *  3.开启一个事务，通过调用beginTransaction()方法开启
        *  4.想容器加入碎片，一般使用replace()方法实现，需要传入容器的id和待添加的碎片实例
        *  5.提交事务，调用commit()方法来完成
        * */
        fm = getSupportFragmentManager();//获取FragmentManager
        FragmentTransaction transaction = fm.beginTransaction();//获取事务
        transaction.replace(R.id.fl_lockedactivity_content, unlockedFragment);//替换,默认显示未加锁的界面,把未加锁的fragment替换掉FrameLayout
        transaction.commit();//提交事务
        System.out.println("initData");
    }

    private void initView() {
        setContentView(R.layout.activity_lock);
        // 加锁的textView
        tv_locked = (TextView) findViewById(R.id.tv_lockedactivity_locked);
        // 加锁的textView
        tv_unlock = (TextView) findViewById(R.id.tv_lockedactivity_unlock);
        // 要替换成fragment的组件
        fl_content = (FrameLayout) findViewById(R.id.fl_lockedactivity_content);
        // 未加锁的fragment实例
        unlockedFragment = new UnlockedFragment();
        // 已加锁的fragment实例
        lockedFragment = new LockedFragment();
    }
}
