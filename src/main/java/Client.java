import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.util.Scanner;

public class Client {

    private static Socket socket;
    public static boolean connectionState = false;
    private static ObjectOutputStream oos;

    public static void main(String[] args) {
        connect();
//        if (connectionState){
//            try {
//                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//                new Thread(new ClientListen(socket)).start();
//                new Thread(new ClientSend(socket, oos)).start();
//                new Thread(new ClientHeart(socket, oos)).start();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }

    }

    private static void connect(){
        try {
            socket = new Socket("127.0.0.1", 9901);
            connectionState = true;
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            new Thread(new ClientListen(socket)).start();
            new Thread(new ClientSend(socket, oos)).start();
            new Thread(new ClientHeart(socket, oos)).start();
        } catch (Exception e) {
            e.printStackTrace();
            connectionState = false;
        }
    }

    public static void reconnect() throws InterruptedException {
        while (!connectionState){
            connect();
            Thread.sleep(3000);
        }
    }
}

class ClientListen implements Runnable{
    private Socket socket;

    ClientListen(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            while (true){
                System.out.println(ois.readObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

class ClientSend implements Runnable{
    private Socket socket;
    private ObjectOutputStream oos;

    ClientSend(Socket socket, ObjectOutputStream oos){
        this.socket = socket;
        this.oos = oos;

    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true){
                System.out.print("client input your message:");
                String string = scanner.nextLine();
                JSONObject object = new JSONObject();
                object.put("type","chat");
                object.put("msg",string);
                oos.writeObject(object);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class ClientHeart implements Runnable{
    private Socket socket;
    private ObjectOutputStream oos;

    ClientHeart(Socket socket, ObjectOutputStream oos){
        this.socket = socket;
        this.oos = oos;
    }

    @Override
    public void run() {
        try {
            System.out.println("心跳包线程以启动");
            while (true){
                Thread.sleep(5000);
                JSONObject object = new JSONObject();
                object.put("type","heart");
                object.put("msg","heart");
                oos.writeObject(object);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
                Client.connectionState = false;
                Client.reconnect();
            } catch (Exception ex) {
                e.printStackTrace();
            }
        }

    }
}