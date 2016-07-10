package domain;

/**
 * Created by Lenovo on 2016/6/8.
 * url信息封装
 */
public class UrlBean {
    private String url;//apk下载的路径
    private int versionCode;//版本号
    private String desc;//描述信息

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "UrlBean{" +
                "url='" + url + '\'' +
                ", versionCode=" + versionCode +
                ", desc='" + desc + '\'' +
                '}';
    }
}
