package utils;

/**
 * Created by Lenovo on 2016/6/19.
 */
public class EncryptTools {
    /**
     *
     * 加密的种子
     * @param str
     * 要加密的字符串
     * @return
     */
    public static String encrypt(String str){
        byte[] bytes = str.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] += 1;//对字节加密
        }
        return new String(bytes);
    }
    /**
     *
     * 解密的种子
     * @param str
     * @return
     */
    public static String decryption(String str) {
        byte[] bytes = str.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] -= 1;//对字节加密
        }
        return new String(bytes);
    }
}
