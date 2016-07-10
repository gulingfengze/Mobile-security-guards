package mobilesafe.eoe;

import java.util.List;

import domain.ContantBean;
import engine.ReadContantsEngine;

/**
 * Created by Lenovo on 2016/6/23.
 * 显示所有好友信息的界面
 */
public class SmslogsActivity extends BaseFriendsCallSmsActivity {
    @Override
    public List<ContantBean> getDatas() {
        return ReadContantsEngine.readSmslog(getApplicationContext());
    }
}
