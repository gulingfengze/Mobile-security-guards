package mobilesafe.eoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import dao.AntiVirusDao;
import domain.AppBean;
import engine.AppManagerEngine;
import utils.Md5Utils;
import utils.MyConstants;

/**
 * Created by Lenovo on 2016/7/7.
 * 扫描病毒的界面
 */
public class AntivirusActivity extends Activity {
    protected static final int SCANNING = 1;
    protected static final int FINISH = 2;
    protected static final int MESSAGE = 3;
    private ImageView iv_scan;
    private TextView tv_scanappname;
    private ProgressBar pb_scan;
    private LinearLayout ll_scancontent;
    private RotateAnimation ra;
    private List<AppBean> allApks;
    private AntiVirusDao dao;
    private int progress;//进度条的进度
    private boolean isRun = false;//控制线程运行

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAnimation();//扫描动画
        //startScan();//开始扫描(耗时操作)
        checkVersion();//检测病毒库

    }

    private boolean connecting = false;//正在联网

    /**
     * @param pointNumber 点的个数
     * @return pointNumber个点组成的字符串
     */
    private String getPoints(int pointNumber) {
        String res = "";
        for (int i = 0; i < pointNumber; i++) {
            res += ".";
        }
        return res;
    }

    private void checkVersion() {
//        final AlertDialog.Builder ab = new AlertDialog.Builder(this);
//        final AlertDialog dialog = ab.setTitle("注意")
//                .setMessage("正在联网")
//                .create();
//        dialog.show();
        tv_scanappname.setText("正在尝试联网......");
        new Thread(new Runnable() {
            @Override
            public void run() {
                connecting = true;
                int i = 0;
                while (connecting){
                    i++;//一直加
                    final int j=i%6+1;// i   从1到6
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //更新UI
                            tv_scanappname.setText("正在尝试联网"+getPoints(j));
                        }
                    });
                    SystemClock.sleep(500);//半秒
                }
            }
        }).start();
        HttpUtils utils = new HttpUtils();
        utils.configTimeout(5000);//设置超时时间
        utils.send(HttpRequest.HttpMethod.GET, MyConstants.VIRUSVERSIONURL, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                System.out.println(arg0);
                //dialog.dismiss();
                connecting = false;//关闭显示正在联网的线程
                Toast.makeText(getApplicationContext(), "联网失败", Toast.LENGTH_SHORT).show();
                //失败
                startScan();//扫描病毒

            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
//                dialog.dismiss();
                connecting = false;//关闭显示正在联网的线程
                //判断版本号是否一致
                String version = arg0.result;
                if (dao.isNewVirus(Integer.parseInt(version))) {
                    //病毒库最新的
                    Toast.makeText(getApplicationContext(), "病毒库最新", Toast.LENGTH_SHORT).show();
                    startScan();//开始扫描
                } else {
                    //有新病毒库
                    //对话框给用户选择 是否跟新病毒库
                    isUpdateVirusDialog();//
                }
            }
        });
    }

    protected void isUpdateVirusDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("有新病毒")
                .setMessage("是否更新病毒库？")
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //下载更新病毒库
                        HttpUtils utils = new HttpUtils();
                        utils.send(HttpRequest.HttpMethod.GET, MyConstants.GETVIRUSVDATASURL, new RequestCallBack<String>() {

                            @Override
                            public void onFailure(HttpException arg0, String arg1) {
                                startScan();//开始扫描病毒
                            }

                            @Override
                            public void onSuccess(ResponseInfo<String> arg0) {

                                String virusJson = arg0.result;
                                try {
                                    JSONObject jsonObj = new JSONObject(virusJson);
                                    String md5 = jsonObj.getString("md5");
                                    String desc = jsonObj.getString("desc");
                                    dao.addVirus(md5, desc);//添加新的病毒
                                    Toast.makeText(getApplicationContext(), "更新病毒库成功", Toast.LENGTH_SHORT).show();
                                    startScan();//开始扫描
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        startScan();//开始扫描病毒
                    }
                });
        ab.show();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCANNING://开始扫描
                    iv_scan.startAnimation(ra);//开始动画
                    break;
                case FINISH://扫描完成
                    iv_scan.clearAnimation();//清除动画
                    break;
                case MESSAGE://扫描一个APK
                    pb_scan.setMax(allApks.size());//设置进度条最大值
                    pb_scan.setProgress(progress);//设置当前进度
                    AntiVirusBean bean = (AntiVirusBean) msg.obj;
                    TextView tv = new TextView(AntivirusActivity.this);
                    if (bean.siVirus) {
                        tv.setTextColor(Color.RED);
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }
                    tv.setText(bean.packName);//包名
                    tv_scanappname.setText("正在扫描:" + bean.packName);
                    //加到线性布局
                    ll_scancontent.addView(tv, 0);//每次加到最前面
                    break;
                default:
                    break;
            }
        }
    };

    private class AntiVirusBean {
        String packName;
        boolean siVirus;
    }

    private void startScan() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.obtainMessage(SCANNING).sendToTarget();//开始扫描动画
                //扫描APK首先获取手机中所有APK
                allApks = AppManagerEngine.getAllApks(getApplicationContext());
                AntiVirusBean bean = new AntiVirusBean();//存放每个apk扫描的结果信息
                isRun = true;//线程可以正常运行的逻辑
                for (AppBean appBean : allApks) {
                    if (!isRun) {
                        //停止线程运行
                        break;
                    }
                    bean.packName = appBean.getPackName();
                    String md5 = Md5Utils.getFileMD5(appBean.getApkPath());
                    System.out.println(md5 + ":" + appBean.getApkPath());
                    if (dao.isVirus(md5)) {
                        System.out.println("是病毒");
                        bean.siVirus = true; //是病毒
                    } else {
                        bean.siVirus = false;//不是病毒
                    }
                    progress++;

                    Message msg = handler.obtainMessage(MESSAGE);
                    msg.obj = bean;
                    handler.sendMessage(msg);

                    SystemClock.sleep(50);
                }
                handler.obtainMessage(FINISH).sendToTarget();//发送扫描完成的消息
            }
        }).start();
    }

    private void initAnimation() {
        ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(500);
        ra.setRepeatCount(Animation.INFINITE);//无限次数扫描
        //修改旋转动画插入器(数学函数)
        ra.setInterpolator(new Interpolator() {

            @Override
            public float getInterpolation(float x) {
                // y
                return x;
            }
        });
    }


    private void initView() {
        setContentView(R.layout.activity_antivirus);
        iv_scan = (ImageView) findViewById(R.id.iv_antivirus_scan); //扫描病毒的扇形
        tv_scanappname = (TextView) findViewById(R.id.tv_antivirus_title);//扫描APP的名字的显示
        pb_scan = (ProgressBar) findViewById(R.id.pb_antivirus_scanprogress);//扫描的进度
        ll_scancontent = (LinearLayout) findViewById(R.id.ll_antivirus_results);  //扫描结果的显示
        dao = new AntiVirusDao();
    }

    @Override
    protected void onDestroy() {
        isRun = false;//activity退出的时候，关闭线程
        super.onDestroy();
    }
}
