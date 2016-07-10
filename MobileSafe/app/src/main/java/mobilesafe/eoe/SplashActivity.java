package mobilesafe.eoe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import domain.UrlBean;
import utils.MyConstants;
import utils.SpTools;

/**
 * 手机卫士splash界面
 */

public class SplashActivity extends AppCompatActivity {

    private static final int LOADMAIN = 1;//加载主界面
    private static final int SHOWUPDATEDIALOG = 2;//显示是否更新的对话框
    private static final int ERROR = 3;//错误统一代号
    private RelativeLayout rl_root;//界面的根布局组件
    private UrlBean parseJson;//url信息封装
    private int versionCode;//版本号
    private String versionName;//版本名
    private TextView tv_versionName;//显示版本名组件
    private long startTimeMillis;//执行开始访问网络的时间
    private ProgressBar pd_download;//下载最新版本apk进度条
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();//初始化界面
        initAnimation();//初始化动画
        initData();//初始化数据
        //timeInitialization();耗时的功能封装，只要耗时的处理，都放到此方法
        //拷贝数据库(“高级设置”功能中号码归属地数据库)
        copyDB("address.db");
        //拷贝病毒数据库(“病毒查杀”功能)
        copyDB("antivirus.db");
    }


    /**
     * 把assert目录下文件拷贝到本地(/data/data/包名/files)
     * @param dbName
     *      assert目录下的文件名
     * @throws IOException
     */
    private void copyDB(final String dbName)  {
        new Thread(){
            public void run() {

                //判断文件是否存在，如果存在不需要拷贝
                File file = new File("/data/data/"+getPackageName() + "/files/" + dbName);
                if (file.exists()) {//文件存在
                    return;
                }
                // 文件的拷贝
                try {
                    filecopy(dbName);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            };
        }.start();

    }

    private void filecopy(String dbName) throws IOException,
            FileNotFoundException {
        //io流来拷贝
        //输入流
      /*
      （外部数据库放在assets下出现问题，所以将其放在raw文件夹下进行读取）
        AssetManager assets = getAssets();
        //读取assert的文件，转换成InputStream
        InputStream is = assets.open(dbName);*/


        //读取raw文件,外部数据放在了raw文件夹下
        InputStream is = getApplicationContext().getResources().openRawResource(
                R.raw.address); //欲导入的数据库

        //输出流
        FileOutputStream fos = openFileOutput(dbName, MODE_PRIVATE );

        //流的拷贝
        //定义缓冲区大小10k
        byte[] buffer = new byte[10240];

        //读取的长度
        int len = is.read(buffer);
        int counts = 1;
        //循环读取,如果读到文件尾部，返回-1
        while (len != -1) {
            //把缓冲区的数据写到输出流
            fos.write(buffer,0,len);
            //每次100k的时候刷新缓冲区的数据到文件中
            if (counts % 10 == 0) {
                fos.flush();//刷新缓冲区
            }
            //继续读取
            len = is.read(buffer);
            counts++;
        }
        fos.flush();
        fos.close();
        is.close();
    }

    /**
     * 耗时的功能封装，只要耗时的处理，都放到此方法
     */
    private void timeInitialization() {
        //一开始动画，就应该干耗时的业务（网络，本地数据初始化，数据的拷贝等）
        if (SpTools.getBoolean(getApplicationContext(), MyConstants.AUTOUPDATE, false)) {
            //true 自动更新
            // 检测服务器的版本
            checkVersion();
        }
// else{
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    SystemClock.sleep(3000);
//                    //进入主界面
//                    handler.obtainMessage(LOADMAIN).sendToTarget();
//                }
//            }).start();
//        }
    }

    /**
     * 获取自己当前版本信息
     */
    private void initData() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;//版本号
            versionName = packageInfo.versionName;//版本名
            tv_versionName.setText(versionName);//设置textView
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 访问服务器获取最新版本信息
     */
    private void checkVersion() {
        //访问服务数据，获取数据url，在子线程中访问服务
        new Thread(new Runnable() {//使用匿名内部类创建子线程对象
            @Override
            public void run() {
                BufferedReader bfr = null;
                HttpURLConnection conn = null;
                int errorCode = -1;//正常，没有错误
                try {
                    startTimeMillis = System.currentTimeMillis();//毫秒显示当前时间
                    URL url = new URL("http://10.0.2.2:8080/guardversion.json");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");//设置请求方式
                    conn.setConnectTimeout(5000);//设置网络连接超时时间
                    conn.setReadTimeout(5000);//设置读取数据超时时间
                    int responseCode = conn.getResponseCode();//500？ 404？ 200？
                    if (responseCode == 200) {//读取成功
                        InputStream is = conn.getInputStream();//获取读取的字节流
//                        InputStreamReader isr=new InputStreamReader(is);
//                        BufferedReader bfr=new BufferedReader(isr);//将字节流转换成字符流
                        bfr = new BufferedReader(new InputStreamReader(is));
                        StringBuilder json = new StringBuilder();//json字符串数据的封装
                        String line;
                        while ((line = bfr.readLine()) != null) {
                            json.append(line);
//
                        }
                        parseJson = parseJson(json);//开始解析json数据,定义一个 parseJson()方法执行解析
                    } else {
                        errorCode = 404;
                    }
                } catch (MalformedURLException e) {
                    errorCode = 4002;
                    e.printStackTrace();
                } catch (IOException e) {//网络连接异常
                    errorCode = 4001;
                    e.printStackTrace();
                } catch (JSONException e) {//json解析异常
                    errorCode = 4003;
                    e.printStackTrace();
                } finally {
                    Message msg = Message.obtain();
                    if (errorCode == -1) {
                        msg.what = isNewVersion(parseJson);//检测是否有新版本
                    } else {
                        msg.what = ERROR;
                        msg.arg1 = errorCode;
                    }
                    long endTime = System.currentTimeMillis();
                    if (endTime - startTimeMillis < 3000) {
                        SystemClock.sleep(3000 - (endTime - startTimeMillis));//时间不超过3秒，补足3秒
                    }
                    handler.sendMessage(msg);//发送消息
                    try {
                        //关闭连接资源
                        if (bfr == null || conn == null) {
                            return;
                        }
                        bfr.close();
                        conn.disconnect();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            }
        }).start();

    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            //处理消息
            switch (msg.what) {
                case LOADMAIN://加载主界面
                    loadMain();
                    break;
                case ERROR://有异常
                    switch (msg.arg1) {
                        case 404://资源找不到
                            Toast.makeText(getApplicationContext(), "404资源找不到", Toast.LENGTH_SHORT).show();
                            break;
                        case 4001://找不到网络
                            Toast.makeText(getApplicationContext(), "4001没有网络", Toast.LENGTH_SHORT).show();
                            break;
                        case 4003://json格式错误
                            Toast.makeText(getApplicationContext(), "4003json格式错误", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                    loadMain();//进入主界面
                    break;
                case SHOWUPDATEDIALOG://显示更新版本的对话框
                    showUpdateDialog();
                    break;
                default:
                    break;
            }
        }

    };

    /**
     * 显示是否更新新版本对话框选择
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//对话框的上下文是Activity的class，AlertDialog是Activity的一部分。
        //builder.setCancelable(false);//1.禁止用户进行取消操作（在弹出是否更新版本对话框时候）
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {//2.添加自定义取消点击事件
            @Override
            public void onCancel(DialogInterface dialog) {
                loadMain();//取消事件处理，直接进入主界面
            }
        })
                .setTitle("提示")
                .setMessage("是否更新到新的版本？新版本具有如下特性：" + parseJson.getDesc())
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("更新apk");//更新apk
                        downLoadNewApk();//访问网络并下载新的apk
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadMain();//进入主界面
            }
        }).show();//显示对话框（builder.show();）
    }

    /**
     * 新版本apk更新（使用了xUtils-2.6.8.jar包）
     */
    private void downLoadNewApk() {
        HttpUtils utils = new HttpUtils();
        System.out.println(parseJson.getUrl());//parseJson.getUrl()下载的url
        //更新时先删除存在的apk
        File file = new File("/mnt/sdcard/xx.apk");
        file.delete();

        utils.download(parseJson.getUrl(), "/mnt/sdcard/xx.apk", new RequestCallBack<File>() {
            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                pd_download.setVisibility(View.VISIBLE);//设置进度条的显示
                pd_download.setMax((int) total);//设置进度条最大值
                pd_download.setProgress((int) current);//设置当前进度
                super.onLoading(total, current, isUploading);
            }

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {//下载成功
                //在主线程中执行
                Toast.makeText(getApplicationContext(), "下载新版本成功", Toast.LENGTH_SHORT).show();
                //安装apk
                installApk();
                pd_download.setVisibility(View.GONE);//隐藏进度条
            }

            @Override
            public void onFailure(HttpException e, String s) {//下载失败
                //在主线程中执行
                Toast.makeText(getApplicationContext(), "下载新版本失败", Toast.LENGTH_SHORT).show();
                pd_download.setVisibility(View.GONE);//隐藏进度条
            }
        });
    }

    /**
     * 安装下载新的apk
     */
    private void installApk() {
        /*<intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:scheme="content" />
        <data android:scheme="file" />
        <data android:mimeType="application/vnd.android.package-archive" />
        </intent-filter>*/
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        Uri data = Uri.fromFile(new File("/mnt/sdcard/xx.apk"));
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(data, type);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //安装下载新的apk时，用户取消安装则直接进入主界面
        loadMain();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 进入主界面
     */
    private void loadMain() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);//进入主界面
        finish();
    }

    /**
     * @param parseJson 在子线程中进行(判断是否有新版本)
     *                  这里不能执行对UI的操作
     */
    protected int isNewVersion(UrlBean parseJson) {
        int serverCode = parseJson.getVersionCode();//获取服务版本
//      long endTimeMillis=System.currentTimeMillis();//执行结束的时间
//        if (endTimeMillis-startTimeMillis<3000){//因为加载splash动画需要3秒
//            //设置休眠时间至少大于3秒，保证spalsh动画加载完成
//            SystemClock.sleep(3000 - (endTimeMillis - startTimeMillis));
//        }
        if (serverCode == versionCode) {//版本一致
            //进入主界面（获取message有两种方法）
//         Message msg=new Message();//这是第一种方法
//          Message msg=Message.obtain();//这是第二种方法
//           msg.what=LOADMAIN;
//           handler.sendMessage(msg);
            return LOADMAIN;
        } else {//有新的版本
//           //弹出对话框，显示新版本的描述信息，用户选择是否更新
//           Message msg=Message.obtain();
//           msg.what=SHOWUPDATEDIALOG;
//           handler.sendMessage(msg);
            return SHOWUPDATEDIALOG;
        }

    }

    /**
     * @param jsonString 从服务器获取的json数据
     * @return url信息封装对象
     */
    protected UrlBean parseJson(StringBuilder jsonString) throws JSONException {
        UrlBean bean = new UrlBean();
        JSONObject jsonObject;

        // {"version":"2","url":"http://10.0.2.2:8080/xxx.apk","desc":"增加了防病毒功能"}
        jsonObject = new JSONObject(jsonString + "");//把json字符串数据封装成json对象
        int versionCode = jsonObject.getInt("version");//版本号
        String url = jsonObject.getString("url");//版本信息地址
        String desc = jsonObject.getString("desc");//描述信息
            /*封装数据*/
        bean.setDesc(desc);//描述信息
        bean.setUrl(url);//新apk下载路径
        bean.setVersionCode(versionCode);//新版本号
        return bean;
    }

    /**
     * 界面组建的初始化
     */
    private void initView() {
        setContentView(R.layout.activity_splash);
        rl_root = (RelativeLayout) findViewById(R.id.rl_splash_root);
        tv_versionName = (TextView) findViewById(R.id.tv_splash_version_name);
        pd_download = (ProgressBar) findViewById(R.id.pb_splash_download_progress);
    }

    /**
     * Splash动画显示
     */
    private void initAnimation() {
        /*定义Alpha动画*/
        //创建动画，Alpha动画,0.0表示完全透明，1.0表示完全不透明（从透明到不透明）
        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setDuration(3000);//设置动画播放时间3S
        aa.setFillAfter(true);//设置界面停留在动画结束状态
        /*定义旋转动画*/
        //顺时针旋转360度，如果逆时针改为-360
        RotateAnimation ra = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);//设置锚点信息（以哪里为中心旋转多少度）
        ra.setDuration(3000);
        ra.setFillAfter(true);
       /*比例动画，从小到大*/
        ScaleAnimation sa = new ScaleAnimation(0.0f, 1.0f,
                0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(3000);
        sa.setFillAfter(true);
        /*创建动画集合*/
        AnimationSet as = new AnimationSet(true);
        as.addAnimation(sa);//加载比例动画
        as.addAnimation(ra);//加载旋转动画
        as.addAnimation(aa);//加载渐变动画
        /*设置动画监听*/
        as.setAnimationListener(new Animation.AnimationListener() {
            /*
             * 开始动画做版本检测
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationStart(android.view.animation.Animation)
			 */
            @Override
            public void onAnimationStart(Animation animation) {
                //耗时的功能统一处理封装
                timeInitialization();
            }

            /**
             * @param animation
             * 动画结束，进行事件处理
             */
            @Override
            public void onAnimationEnd(Animation animation) {
                //判断是否进行服务器版本的检测
                if (!SpTools.getBoolean(getApplicationContext(), MyConstants.AUTOUPDATE, false)) {
                    //不做版本检测，直接进入主界面
                    loadMain();
                } else {
                    //界面的衔接是有自动更新来完成，在此不做处理
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //显示动画，将布局设置动画
        rl_root.startAnimation(as);//同时播放三种动画

    }
//    /**
//     * Alpha显示
//     */
//    private void showAlpha() {
//        // Alpha动画0.0完全透明
//        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
//        // 设置动画播放的时间毫秒为单位
//        aa.setDuration(3000);
//        // 界面停留在动画结束状态
//        aa.setFillAfter(true);
//        // 给组件设置动态
//        rl_root.startAnimation(aa);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
}
