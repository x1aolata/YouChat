import org.json.simple.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

/**
 * @author x1aolata
 * @date 2020/11/6 18:52
 * @script ...
 */


public class Server {

    // 默认端口6666
    private static int port = 6666;

    public static void main(String[] args) {
        System.out.println("服务已启动.......");

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println(socket.getRemoteSocketAddress().toString());

                // 清除之前连接过的IP
                ChatManager.deleteSocket(Utils.getIPFromSocketAddress(socket.getRemoteSocketAddress().toString()));
                // 获取到socket
                System.out.println("有人连进来了");
                ChatSocket chatSocket = new ChatSocket(socket);
                chatSocket.start();

                ChatManager.getChatManager().add(chatSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}


class ChatSocket extends Thread {

    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public Socket getSocket() {
        return socket;
    }

    public ChatSocket(Socket socket) {
        this.socket = socket;
        // 获取输入输出流
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(Object object) {
        try {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        } catch (IOException e) {
            //  e.printStackTrace();
        }

    }

    public void showOtherInfo(JSONObject jsonObject) {
        if (jsonObject.get("name") != null)
            System.out.println("name: " + jsonObject.get("name").toString());
        if (jsonObject.get("source") != null)
            System.out.println("source: " + jsonObject.get("source").toString());
        if (jsonObject.get("destination") != null)
            System.out.println("destination: " + jsonObject.get("destination").toString());
        if (jsonObject.get("time") != null)
            System.out.println("time: " + jsonObject.get("time").toString());
        System.out.println("\n");
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object object = objectInputStream.readObject();
                JSONObject jsonObject = (JSONObject) object;

                // 文字消息
                if (jsonObject.get("type").equals("text")) {
                    System.out.println(jsonObject.get("data").toString());
                    showOtherInfo(jsonObject);
                }
                // 图片消息
                if (jsonObject.get("type").equals("image")) {
                    System.out.println("转发图片");
                    showOtherInfo(jsonObject);
                }
                // 音频消息
                if (jsonObject.get("type").equals("audio")) {
                    System.out.println("转发音频");
                    showOtherInfo(jsonObject);
                }
                // 心跳消息
                if (jsonObject.get("type").equals("heart")) {
                    System.out.println("心跳");
                    showOtherInfo(jsonObject);
                }
                // 转发除了心跳包的消息
                if (!((JSONObject) object).get("type").equals("hearts")) {
                    ChatManager.getChatManager().sendMessageToOthers(this, object);
                }
                System.out.println("当前共计连接数：" + "" + ChatManager.getSize());

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}

class ChatManager {
    //单例类
    private static ChatManager chatManager = new ChatManager();
    public static Vector<ChatSocket> vector = new Vector<ChatSocket>();

    public static void deleteSocket(String ip) {
        for (int i = 0; i < vector.size(); i++) {
            if (Utils.getIPFromSocketAddress(vector.get(i).getSocket().getRemoteSocketAddress().toString()).equals(ip)) {
                vector.remove(i);
                i--;
            }
        }
    }

    private ChatManager() {
    }

    public static int getSize() {
        return vector.size();
    }

    public static ChatManager getChatManager() {
        return chatManager;
    }

    public void add(ChatSocket chatSocket) {
        vector.add(chatSocket);
    }

    public void sendMessageToOthers(ChatSocket chatSocket, Object object) {
        for (ChatSocket chatSocketorder : vector) {
            if (!chatSocket.equals(chatSocketorder)) {
                chatSocketorder.sendMessage(object);
            }
        }
    }


}