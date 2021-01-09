package com.x1aolata.youchat.client;

import android.os.Handler;
import android.os.Message;

import com.x1aolata.youchat.bean.MessageStatus;

import org.json.simple.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author x1aolata
 * @date 2020/11/22 13:14
 * @script ...
 */

/**
 * 接收消息
 */
public class ClientListener implements Runnable {

    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private static Handler handler;

    // 切换Activity时切换Handler
    public static void setHandler(Handler handler) {
        ClientListener.handler = handler;
    }

    public ClientListener(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, Handler handler) {
        this.socket = socket;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
        ClientListener.handler = handler;
    }


    @Override
    public void run() {
        try {
            while (true) {
                JSONObject jsonObject = (JSONObject) objectInputStream.readObject();

                if (jsonObject.get("type").equals("text")) {
                    Message message = handler.obtainMessage();
                    message.what = MessageStatus.receiveText;
                    message.obj = jsonObject;
                    handler.sendMessage(message);
                }
                if (jsonObject.get("type").equals("image")) {
                    Message message = handler.obtainMessage();
                    message.what = MessageStatus.receiveImage;
                    message.obj = jsonObject;
                    handler.sendMessage(message);
                }
                if (jsonObject.get("type").equals("audio")) {
                    Message message = handler.obtainMessage();
                    message.what = MessageStatus.receiveAudio;
                    message.obj = jsonObject;
                    handler.sendMessage(message);
                }

                if (jsonObject.get("type").equals("heart")) {
                    Message message = handler.obtainMessage();
                    message.what = MessageStatus.receiveHeart;
                    message.obj = jsonObject;
                    handler.sendMessage(message);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
