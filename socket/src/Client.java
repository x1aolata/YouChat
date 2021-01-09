import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author x1aolata
 * @date 2020/11/6 19:07
 * @script ...
 */
public class Client {
    private static String ServerIP = "127.0.0.1";
    private static int port = 6666;


    private static Socket socket;
    public static boolean connectionstate = false;

    public static void main(String[] args) {
        System.out.println("客户端已启动.....");

        while (!connectionstate) {
            System.out.println("正在连接....");
            connect();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }


    }


    private static void connect() {
        try {

            System.out.println("连接成功 " + ServerIP);
            socket = new Socket(ServerIP, port);
            connectionstate = true;

            // 连接成功初始化
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            new Thread(new ClientListener(socket, ois)).start();
            new Thread(new ClientSend(socket, oos)).start();
            new Thread(new ClientHeart(socket, oos)).start();

        } catch (IOException e) {
            e.printStackTrace();
            connectionstate = false;
        }
    }

    public static void reconnect() {
        while (!connectionstate) {
            System.out.println("正在重新连接.....");
            connect();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


class ClientListener implements Runnable {
    private Socket socket;
    ObjectInputStream ois;


    public ClientListener(Socket socket, ObjectInputStream ois) {
        this.socket = socket;
        this.ois = ois;
    }


    @Override
    public void run() {
        try {
            while (true) {

                JSONObject jsonObject = (JSONObject) ois.readObject();
                // 文字消息
                if (jsonObject.get("type").equals("text")) {
                    System.out.println(jsonObject.get("data").toString());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
                Client.connectionstate = false;
                Client.reconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

class ClientSend implements Runnable {


    private Socket socket;

    private ObjectOutputStream oos;

    public ClientSend(Socket socket, ObjectOutputStream oos) {
        this.socket = socket;
        this.oos = oos;
    }


    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print(">>>");
                String s = scanner.nextLine();

                JSONObject object = new JSONObject();
                object.put("type", "text");
                object.put("data", s);

                oos.writeObject(object);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class ClientHeart implements Runnable {

    private Socket socket;
    private ObjectOutputStream oos;

    public ClientHeart(Socket socket, ObjectOutputStream oos) {
        this.socket = socket;
        this.oos = oos;
    }


    @Override
    public void run() {
        System.out.println("心跳包线程已启动");
        try {
            while (true) {

                Thread.sleep(5000);
                JSONObject object = new JSONObject();
                object.put("type", "heart");
                object.put("data", "心跳包");

                oos.writeObject(object);
                oos.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
                Client.connectionstate = false;
                Client.reconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}


