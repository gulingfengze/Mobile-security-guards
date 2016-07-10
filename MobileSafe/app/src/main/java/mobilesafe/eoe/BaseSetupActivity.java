package mobilesafe.eoe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Lenovo on 2016/6/13.
 * “手机防盗”向导4个界面封装
 */
public abstract class BaseSetupActivity extends Activity {

    private GestureDetector gd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initGesture();//初始化手势识别器
        initData();//初始数据
        initEvent();//初始化组件的事件
    }

    public void initData() {
        
    }

    public void initEvent() {

    }

    /**
     * 手势识别器（界面滑动）
     */
    private void initGesture() {
        //初始化手势识别器,要想手势识别器生效，绑定onTouch事件
        gd = new GestureDetector(new GestureDetector.OnGestureListener() {

            /**
             * e1,按下的点
             * e2 松开屏幕的点
             * velocityX x轴方向的速度
             * velocityY y轴方向的速度
             */
            @Override //覆盖此方法完成手势的切换效果
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //x轴方向的速度是否满足横向滑动的条件 pix/s
                if (velocityX > 200) { //速度大于400像素每秒
                    //可以完成滑动
                    float dx = e2.getX() - e1.getX();//x轴方向滑动的间距
                    if (Math.abs(dx) < 100) {
                        return true;//如果间距不符合直接无效
                    }
                    if (dx < 0 ){//从右往左滑动
                        next(null);//不是组件的事件调用
                    }  else {//从左往右滑动
                        prev(null);
                    }
                }
                return true;


            }
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }


        });
    }

    /**
     * 将initView()方法定义为抽象方法
     */
    public abstract void initView();


    /**
     * @param v
     * 下一个页面事件处理
     * 要完成的任务：1.完成界面的切换（1.1和1.2）  2.界面切换时的动画播放
     */
    public void next(View v){
        nextActivity();//1.1封装切换的下一个界面

        nextAnimation();//2.界面想下一个切换时动画播放
    }

    /**
     * @param type
     * 向导界面切换功能实现方法封装
     */
    public void startActivity(Class type){
        Intent next=new Intent(this,type);
        startActivity(next);
        finish();//关闭当前设置向导界面，不然点击返回键时又回到该设置向导界面。
    }

    /**
     * 封装切换的下一个界面的抽象方法
     */
    public abstract void nextActivity();
    /**
     * 封装切换的上一个界面的抽象方法
     */
    public abstract void preActivity();

    /**
     * 界面向下一个切换时的动画播放
     */
    private void nextAnimation() {
        //第一个参数是进来的动画，第二个是出去的动画
       overridePendingTransition(R.anim.next_in,R.anim.next_out);
    }

    /**
     * @param v
     * 上一个页面事件处理
     */
    public void prev(View v){
        preActivity();//1.2封装切换的上一个界面

        preAnimation();//2.界面切换时动画播放
    }
    /**
     * 界面向上一个切换时的动画播放
     */
    private void preAnimation() {
        //第一个参数是进来的动画，第二个是出去的动画
        overridePendingTransition(R.anim.prev_in,R.anim.prev_out);
    }

}
