package engine;

import android.content.Context;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Lenovo on 2016/7/6.
 * 流量统计的业务类
 */
public class ConnectivityEngine {
    /**
     * @return 接收的流量信息（格式化的如：33MB）
     */
    public static String getReceive(int uid, Context context) {
        String res = null;
        //读取流量信息文件 /proc/uid_stat/uid/tcp_rcv
        String path = "/proc/uid_stat/" + uid + "/tcp_rcv";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = reader.readLine();
            long size = Long.parseLong(line);
            res = Formatter.formatFileSize(context, size);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * @return 发送的流量信息（格式化的如：22MB）
     */
    public static String getSend(int uid, Context context) {
        String res = null;
        //读取流量信息文件 /proc/uid_stat/uid/tcp_snd
        String path = "/proc/uid_stat/" + uid + "/tcp_snd";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = reader.readLine();
            long size = Long.parseLong(line);
            res = Formatter.formatFileSize(context, size);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
