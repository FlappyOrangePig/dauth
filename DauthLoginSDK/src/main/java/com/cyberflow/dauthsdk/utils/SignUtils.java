package com.cyberflow.dauthsdk.utils;



import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class SignUtils {
    private static final String SECRET_KEY = "123&*abc";
    private static final String TAG = "ApiClient";
    //对参数升序排序
    public static String sign(final Map<String, String> data) {
        data.put("security_key",SECRET_KEY);
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[0]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            if ("sign".equals(keyArray[i])) {
                continue;
            }
            // 参数值为空，则不参与签名
            if (String.valueOf(data.get(keyArray[i])).length() > 0) {
                sb.append(keyArray[i]).append("=").append(data.get(keyArray[i]));
                if (i < keyArray.length - 1) {
                    sb.append("&");
                }
            }
        }
        Log.e("DAuthLogger","签名前参数："+sb.toString());
        String signParam = md5(sb.toString());
        Log.e("DAuthLogger","签名后md5值："+signParam);
        data.remove("security_key");
        return signParam;
    }



    private static String md5(String key) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            String upCaseStr = new String(str);
            String lowCase = upCaseStr.toLowerCase();
            return lowCase;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> objToMap(Object obj) {


        Map<String, Object> map = new HashMap<String, Object>();

        Field[] fields = obj.getClass().getDeclaredFields(); // 获取f对象对应类中的所有属性域

        for (int i = 0, len = fields.length; i < len; i++) {

            String varName = fields[i].getName();

            varName = varName.toLowerCase(); // 将key置为小写，默认为对象的属性

            try {

                boolean accessFlag = fields[i].isAccessible(); // 获取原来的访问控制权限

                fields[i].setAccessible(true); // 修改访问控制权限

                Object o = fields[i].get(obj); // 获取在对象f中属性fields[i]对应的对象中的变量

                if (o != null) {

                    map.put(varName, o.toString());

                }

                fields[i].setAccessible(accessFlag); // 恢复访问控制权限

            } catch (IllegalArgumentException ex) {

                ex.printStackTrace();

            } catch (IllegalAccessException ex) {

                ex.printStackTrace();

            }

        }
        return map;
    }

}
