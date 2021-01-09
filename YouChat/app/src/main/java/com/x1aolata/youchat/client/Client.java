package com.x1aolata.youchat.client;

import android.graphics.Bitmap;
import android.os.Message;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

import com.x1aolata.youchat.bean.MessageStatus;
import com.x1aolata.youchat.util.Utils;

/**
 * @author x1aolata
 * @date 2020/11/22 11:34
 * @script ...
 */
public class Client {
    private static final Client ourInstance = new Client();
    private static ObjectInputStream objectInputStream;
    private static ObjectOutputStream objectOutputStream;
    private static JSONObject object;
    private static String name;
    private static String friendIP;
    private static Handler handler;
    private static String serverIP = "127.0.0.1";
    private static int port = 6666;
    private Socket socket;

    private Client() {
    }


    public static void setFriendIP(String friendIP) {
        Client.friendIP = friendIP;
    }

    public static void setHandler(Handler handler) {
        Client.handler = handler;
    }

    //获取实例
    public static Client getInstance() {
        return ourInstance;
    }

    //初始化配置，必须
    public static void setConfig(Handler handler, String serverIP, int port, String name) {
        Client.handler = handler;
        Client.serverIP = serverIP;
        Client.port = port;
        Client.name = name;
    }


    boolean connection_status = false;

    public boolean getConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(serverIP, port);
                    connection_status = true;
                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    Log.d("YouChatinfo", "服务器连接成功");
                    //向主线程发送消息
                    Message message = handler.obtainMessage();
                    message.what = MessageStatus.connection_successful;
                    handler.sendMessage(message);

                    // 启动接收消息线程
                    new Thread(new ClientListener(socket, objectInputStream, objectOutputStream, handler)).start();

                    // 启动心跳
                    startHeart();
                } catch (Exception e) {
                    // e.printStackTrace();
                    Log.d("YouChatinfo", "服务器连接失败");
                    connection_status = false;
                    Message message = handler.obtainMessage();
                    message.what = MessageStatus.connection_fail;
                    handler.sendMessage(message);

                }

            }
        }).start();

        return connection_status;
    }

    public static void sendText(String s) {
        object = new JSONObject();
        object.put("type", "text");
        object.put("data", s);
        object.put("name", name);
        object.put("source", Utils.getIPAddress());
        object.put("destination", friendIP);
        object.put("time", Utils.getCurrentTime());
        sendObject(object);
    }

    public static void sendHeart() {
        object = new JSONObject();
        object.put("type", "heart");
        object.put("name", name);
        object.put("source", Utils.getIPAddress());
        object.put("time", Utils.getCurrentTime());
        sendObject(object);
    }

    public static void sendImage(Bitmap bitmap) {
        object = new JSONObject();
        object.put("type", "image");
        object.put("data", Utils.image2Base64(bitmap));
        object.put("name", name);
        object.put("source", Utils.getIPAddress());
        object.put("destination", friendIP);
        object.put("time", Utils.getCurrentTime());
        sendObject(object);
    }

    public static void sendAudio(String audioBase64Code, int audiotime) {
        object = new JSONObject();
        object.put("type", "audio");
        object.put("data", audioBase64Code);
        object.put("audiotime", audiotime);
        object.put("name", name);
        object.put("source", Utils.getIPAddress());
        object.put("destination", friendIP);
        object.put("time", Utils.getCurrentTime());
        sendObject(object);
    }

    private static void sendObject(Object object) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (objectOutputStream != null) {
                        objectOutputStream.writeObject(object);
                        objectOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private static void startHeart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Client.sendHeart();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

}
