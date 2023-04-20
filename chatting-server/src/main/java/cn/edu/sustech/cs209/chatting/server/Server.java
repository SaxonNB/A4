package cn.edu.sustech.cs209.chatting.server;

import JDBC.Utils;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.Room;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static ArrayList<PrintWriter> outputStreams = new ArrayList<>();
    private static CopyOnWriteArrayList<Room> roomList = new CopyOnWriteArrayList<>();
    private static ConcurrentHashMap<String,Room> map = new ConcurrentHashMap<>();
    //用户名与client的hashmap
    private static ConcurrentHashMap<String,PrintWriter> name_To_printer = new ConcurrentHashMap<>();
    //连接服务器的客户端列表
    private static CopyOnWriteArrayList<String> clientList = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8588);
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println(socket.getInetAddress() + "连接上了本服务器");
            new Thread(new ServerThread(socket/*,this,roomList,map,name_To_printer,clientList,*/)).start();
        }
    }

    private static class ServerThread implements Runnable {
        private Server server ;
        private CopyOnWriteArrayList<Room> roomList = new CopyOnWriteArrayList<>();
        private ConcurrentHashMap<String,Room> map = new ConcurrentHashMap<>();
        //用户名与client的hashmap
        private ConcurrentHashMap<String,PrintWriter> name_To_printer = new ConcurrentHashMap<>();
        //连接服务器的客户端列表
        private CopyOnWriteArrayList<String> clientList = new CopyOnWriteArrayList<>();
        private Socket socket = null;
        private static Scanner in;
        private static PrintWriter out;
        private String User;
        private static Utils jdbc = new Utils();

        public ServerThread(Socket socket/*,Server server/*,CopyOnWriteArrayList<Room> roomList,ConcurrentHashMap<String,Room> map,ConcurrentHashMap<String,PrintWriter> name_To_printer,CopyOnWriteArrayList<String> clientList*/) {
            this.socket = socket;
           /* this.clientList = clientList;
            this.map = map;
            this.name_To_printer = name_To_printer;
            this.roomList = roomList;*/
        }

        @Override
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream());
                outputStreams.add(out);
                doService();
            } catch (SocketException se) { /*处理用户断开的异常*/
                System.out.println("处理用户断开的异常");

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        public static void doService() {
            while (true) {
                if (in.hasNext()) {
                    String line = in.nextLine();
                    Message message = Message.fromJson(line);
                    String username = message.getSentFrom();
                    String password = message.getPassword();
                    switch (message.getType()) {
                        case CONNECT:
                            Message tempMessage;
                            String JSON;
                            if (jdbc.hasSignIn(username) && jdbc.isMember(username, password)) {
                                tempMessage = new Message(System.currentTimeMillis(), username, password, username, "", MessageType.SUCCESS);
                                JSON = Message.toJson(tempMessage);


                            } else if (!jdbc.hasSignIn(username)) {
                                tempMessage = new Message(System.currentTimeMillis(), username, password, username, "", MessageType.SUCCESS);
                                JSON = Message.toJson(tempMessage);
                                jdbc.signIn(username, password);
                            }else {
                                tempMessage = new Message(System.currentTimeMillis(),username,password,username,"",MessageType.FAIL);
                                JSON = Message.toJson(tempMessage);
                            }
                            out.println(JSON);
                            out.flush();
                            break;
                        case DISCONNECT:

                            break;
                        default:
                            break;
                    }


                } else {
                    return;
                }
            }
        }

        /**
         * 获得
         */
    }



}

