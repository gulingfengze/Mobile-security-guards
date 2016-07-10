package view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import mobilesafe.eoe.R;

/**
 * Created by Lenovo on 2016/6/21.
 * 自定义组合控件
 * 自定义“设置中心界面”View
 */
public class SettingCenterItemView extends LinearLayout {


    private TextView tv_title;
    private TextView tv_content;

    private String[] contents;
    private View item;
    private CheckBox cb_check;
    private View blackItem;


    /**
     * @param context 在代码实例化调用该构造函数
     */
    public SettingCenterItemView(Context context) {
        super(context);
        initView();

    }

    /**
     * @param context
     * @param attrs   在配置文件中，反射实例化设置属性参数
     */
    public SettingCenterItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        initEvent();
        String content = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "content");
        String title = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "title");
        System.out.println("title:" + title);
        System.out.println("content:" + content);
        tv_title.setText(title);
//        tv_content.setText(content);
        contents = content.split("-");
        //初始化设置未选中的颜色为红色
        tv_content.setTextColor(Color.RED);
        tv_content.setText(contents[0]);

    }

    /**
     * item跟布局设置点击事件
     *
     * @param listener
     */
    public void setItemClickListener(OnClickListener listener) {
        //通过自定义组合控制，把时间传递给子组件
        item.setOnClickListener(listener);
    }

    /**
     * 设置item里的checkbox的状态
     *
     * @param isChecked
     */
    public void setChecked(boolean isChecked) {
        cb_check.setChecked(isChecked);
    }

    /**
     * @return item里的checkbox的状态
     */
    public boolean isChecked() {
        return cb_check.isChecked();
    }

    /**
     * 初始化复选框事件
     */
    private void initEvent() {
        item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//子布局点击事件
                cb_check.setChecked(!cb_check.isChecked());
            }
        });
        cb_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //设置选中的颜色为绿色
                    tv_content.setTextColor(Color.GREEN);
                    tv_content.setText(contents[1]);
                } else {
                    //设置未选中的颜色为红色
                    tv_content.setTextColor(Color.RED);
                    tv_content.setText(contents[0]);
                }
            }
        });
    }

    /**
     * 初始化LinearLayout的子组件
     */
    private void initView() {
        /*1.添加“设置中心”的自定义子组件*/
        item = View.inflate(getContext(), R.layout.item_settingcenter_view, null);
        //显示标题
        tv_title = (TextView) item.findViewById(R.id.tv_settingcenter_autoupdate_title);
        //显示内容
        tv_content = (TextView) item.findViewById(R.id.tv_settingcenter_autoupdate_content);
        //设置复选框
        cb_check = (CheckBox) item.findViewById(R.id.cb_settingcenter_autoupdate_checked);
        addView(item,0);//设置中心的自定义控件item
//        /*2.添加“通信卫士”的自定义子组件*/
//        blackItem = View.inflate(getContext(), R.layout.item_telsmssafe_listview, null);
//        addView(blackItem);//通讯卫士的自定义控件blackItem（黑名单item）
    }

    public SettingCenterItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
