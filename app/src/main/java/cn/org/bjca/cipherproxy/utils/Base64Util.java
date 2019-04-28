package cn.org.bjca.cipherproxy.utils;

import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by 吴腾飞 on 2019/4/24.
 */

public class Base64Util {
    /**
     * String转base64String
     *
     * @param str
     * @return
     */
    public static String strConvertBase64(String str) {
        if (null != str) {
            String s = Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);
            return s;
        }
        return null;
    }

    /**
     * base64String转String
     *
     * @param str base64String
     * @return
     */
    public static String base64ConvertStr(String str) {
        if (null != str) {
            byte[] s = Base64.decode(str, Base64.NO_WRAP);
            return new String(s);
        }
        return null;
    }

    /**
     * 文件转base64字符串
     *
     * @param file
     * @return
     */
    public static String fileToBase64(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            int length = in.read(bytes);
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return base64;
    }

    /**
     * base64字符串转文件
     *
     * @param base64
     * @return
     */
    public static File base64ToFile(String base64) {
        File file = null;
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String time = simpleDateFormat.format(date.getTime());
        String fileName = time + ".xml";
        FileOutputStream out = null;
        try {
            // 解码，然后将字节转换为文件
            file = new File(Environment.getExternalStorageDirectory() + "/Cipher/xml", fileName);
            if (!file.exists())
                file.getParentFile().mkdirs();
            file.createNewFile();
            byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);// 将字符串转换为byte数组
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buffer = new byte[1024];
            out = new FileOutputStream(file);
            int byteread = 0;
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread); // 文件写操作
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
