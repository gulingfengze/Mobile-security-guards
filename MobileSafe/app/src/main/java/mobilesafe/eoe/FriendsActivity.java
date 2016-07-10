package mobilesafe.eoe;

import java.util.List;

import domain.ContantBean;
import engine.ReadContantsEngine;

/**
 * Created by Lenovo on 2016/6/19.
 * 显示所有好友信息界面
 */
public class FriendsActivity extends BaseFriendsCallSmsActivity {
    /* (non-Javadoc)
         * 提取数据的方法,需要覆盖此方法完成数据的显示
         * @see mobilesafe.eoe.BaseFriendsCallSmsActivity#getDatas()
         */
    @Override
    public List<ContantBean> getDatas() {
        return ReadContantsEngine.readContants(getApplicationContext());
    }

}