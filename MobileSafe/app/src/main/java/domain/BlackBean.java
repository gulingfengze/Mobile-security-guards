package domain;

/**
 * Created by Lenovo on 2016/6/22.
 * 黑名单数据封装类
 */
public class BlackBean {
    private String phone;
    private int mode;
    @Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        if (o instanceof BlackBean) {
            BlackBean bean = (BlackBean)o;
            return phone.equals(bean.getPhone());
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {

        return phone.hashCode();
    }
    @Override
    public String toString() {
        return "BlackBean{" +
                "phone='" + phone + '\'' +
                ", mode=" + mode +
                '}';
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
