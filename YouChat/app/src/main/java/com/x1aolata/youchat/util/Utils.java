package com.x1aolata.youchat.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Base64;
import android.util.Log;
import android.util.TimeUtils;

import com.blankj.utilcode.util.NetworkUtils;
import com.x1aolata.youchat.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author x1aolata
 * @date 2020/11/22 13:20
 * @script ...
 */
public class Utils {

    static List<Integer> Images = new ArrayList<Integer>(Arrays.asList(
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3,
            R.drawable.image4,
            R.drawable.image5,
            R.drawable.image6,
            R.drawable.image7,
            R.drawable.image8,
            R.drawable.image9,
            R.drawable.image10,
            R.drawable.image11,
            R.drawable.image12));

    static List<Integer> choseImages;

    public static int getRandomImage() {
        if (choseImages == null || choseImages.size() == 0)
            choseImages = new ArrayList<Integer>(Images);
        Random rand = new Random();
        int n = rand.nextInt(choseImages.size());
        int image = choseImages.get(n);
        choseImages.remove(n);
        return image;
    }


    public static String audio2Base64(String path) {
        String str = "";
        try {
            File file = new File(path);
            FileInputStream inputFile = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            inputFile.read(buffer);
            inputFile.close();
            str = Base64.encodeToString(buffer, Base64.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 将base64字符解码保存文件
     *
     * @param base64Code
     * @throws Exception
     */

    public static String base642Audio(String base64Code) {
        String path = "/data/user/0/com.x1aolata.youchat/files/audio" + getRandomString(12) + ".mp3";
        try {
            byte[] buffer = Base64.decode(base64Code, Base64.DEFAULT);
            FileOutputStream out = new FileOutputStream(path);
            out.write(buffer);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }


    /**
     * 生成随机长度的字符串
     * 用于命名
     *
     * @param length
     * @return
     */
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }


    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentTime() {
        return new Date().toLocaleString();
    }

    /**
     * 获取本机IP地址
     *
     * @return
     */
    public static String getIPAddress() {
        return NetworkUtils.getIPAddress(true);
    }

    /**
     * Bitmap 转成 Base64
     *
     * @param bitmap
     * @return
     */
    public static String image2Base64(Bitmap bitmap) {
        //以防解析错误之后bitmap为null
        if (bitmap == null)
            return "解析异常";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //此步骤为将bitmap进行压缩，选择了原格式png，
        // 第二个参数为压缩质量，我选择了原画质，也就是100，
        // 第三个参数传入outputstream去写入压缩后的数据
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将获取到的outputstream转换成byte数组
        byte[] bytes = outputStream.toByteArray();
        //android.util包下有Base64工具类，直接调用，格式选择Base64.DEFAULT即可
        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        //打印数据，下面计算用
        return str;
    }


    /**
     * 将Base64转成Bitmap
     *
     * @param s
     * @return
     */
    public static Bitmap base642Image(String s) {
        //用base64.decode解析编码
        byte[] bytes = Base64.decode(s, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}



