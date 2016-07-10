package mobilesafe.eoe;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import engine.PhoneLocationEngine;

/**
 * 手机归属地查询界面（高级工具）
 * Created by Lenovo on 2016/6/24.
 */
public class PhoneLocationActivity extends Activity {

    private EditText et_phone;
    private Button bt_query;
    private TextView tv_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initEvent();
    }

    private void initEvent() {

        /*给EditText加文件变化事件*/
        et_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                locationQuery();//文本改变
            }
        });

        bt_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationQuery();
            }
        });
    }

    /**
     * 归属地查询事件封装
     */
    private void locationQuery() {
        String phone = et_phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
//            Toast.makeText(getApplicationContext(), "号码不能为空", Toast.LENGTH_SHORT).show();
            //抖动的效果
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            et_phone.startAnimation(shake);

//            //震动的效果
//            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            //震动的参数设置
//            vibrator.vibrate(new long[]{200, 300, 300, 200, 500, 100}, 3);

            tv_location.setText("归属地：");
            return;
        } else {
            String location = PhoneLocationEngine.locationQuery(phone, getApplicationContext());
            tv_location.setText("归属地:" + location);
        }
    }

    private void initView() {
        setContentView(R.layout.activity_phonelocation);
        et_phone = (EditText) findViewById(R.id.et_phonelocation_number);
        bt_query = (Button) findViewById(R.id.bt_phonelocation_query);
        tv_location = (TextView) findViewById(R.id.tv_phonelocation_address);
    }
}
