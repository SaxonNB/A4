package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientThread implements Runnable {
    //用户名
    private String userName;
    //用户密码
    private String userPassword;
    //主机地址
    private String hostName;
    //端口号
    private int port;
    //套接字
    private Socket s;
    private BufferedReader br = null;
    // 该线程所处理的Socket所对应的输出流
    private PrintStream ps = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Scanner in;
    private PrintWriter out;
    private Controller controller;
    private static int currentRoom;

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public int getPort() {
        return port;
    }

    public String getHostName() {
        return hostName;
    }

    public Socket getS() {
        return s;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public boolean exit = false;


    public ClientThread(String hostname, int port, String username, String userPassword, Controller controller) {
        this.hostName = hostname;
        this.port = port;
        this.userName = username;
        this.userPassword = userPassword;
        this.controller = controller;
        //将本线程对象赋给ChatController控制器
        // controller.setClientThread(this);
    }


    @Override
    public void run() {
        try {
            boolean iscon = true;
            s = new Socket();
            SocketAddress endpoint = new InetSocketAddress(hostName, port);
            //设置连接超时时间
            s.connect(endpoint, 5 * 1000);
            br = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf-8"));
            ps = new PrintStream(s.getOutputStream());
            if (s.isConnected()) {
                System.out.println("本机已成功连接服务器！下一步用户登录..");
            }
            connect();

            while (!s.isClosed() && s.isConnected() && !exit) {
                //System.out.println(!s.isClosed()+" "+ s.isConnected() +" "+!exit);
                String serverMessage = br.readLine();
                // System.out.println(serverMessage);
                if (serverMessage != null) {
                    System.out.println("收到服务器消息.." + serverMessage + " 对数据进行解析并做响应中..");
                    Message message = Message.fromJson(serverMessage);
                    switch (message.getType()) {
                        case SUCCESS:
                            controller.setClientThread(this);
                            //  System.out.println(message.getNowUserList());
                            controller.setOnlineUserList(message.getNowUserList());
                            System.out.println("用户登录成功！");
                            break;
                        case FAIL:
                            System.out.println("用户登陆失败！");
                            controller.failLogin();
                            s.close();
                            break;
                        case NEWCLIENTJION:
                            System.out.println("新用户加入，更新在线用户列表");
                            controller.setOnlineUserList(message.getNowUserList());
                            break;
                        case SOMEONELEAVE:
                            System.out.println("有用户退出，更新在线列表");
                            controller.setOnlineUserList(message.getNowUserList());
                            break;
                        case REFRESHCHATTHINGWINDOWS:
                            controller.setCurrentRoom(message.getChatRoom().getRoomId());
                            System.out.println("更新聊天界面");
                            controller.initializeChatWindows(message);
                            break;
                        case REFRESHCHATLIST:
                            System.out.println("更新聊天列表");
                            CopyOnWriteArrayList<ChatRoom> rooms = message.getRoomlist();
                            ArrayList<String> roomnameByNumbers = new ArrayList<>();
                            if (rooms != null) {
                                for (int i = 0; i < rooms.size(); i++) {
                                    roomnameByNumbers.add(list_to_string(rooms.get(i).getUserList()));
                                }
                                controller.refreshChatList(roomnameByNumbers);
                            }
                            break;
                        case RECIEVE:
                            CopyOnWriteArrayList<ChatRoom> rooms1 = message.getRoomlist();
                            ArrayList<String> roomnameByNumbers1 = new ArrayList<>();
                            if (rooms1 != null) {
                                System.out.println("收到消息，更新聊天列表");
                                for (int i = 0; i < rooms1.size(); i++) {
                                    roomnameByNumbers1.add(list_to_string(rooms1.get(i).getUserList()));
                                }
                                controller.refreshChatList(roomnameByNumbers1);
                            }
                            if (message.getChatRoom().getRoomId() == controller.getCurrentRoom()) {
                                System.out.println("收到消息，更新聊天界面");
                                controller.initializeChatWindows(message);
                            }
                            break;
                        case RECIEVEFILE:
                            System.out.println("成功接收文件");
                            String encodedFile = message.getFiledata();
                            String fileName = message.getFilename();
                            String path = "D:\\Java2\\A4\\chatting-client\\src\\main\\java\\AllFile\\" + fileName;
                            byte[] fileContent = Base64.getDecoder().decode(encodedFile);
                            Files.write(Paths.get(path), fileContent);
                            break;
                        default:
                            break;
                    }
                }
            }

        } catch (SocketTimeoutException ex) {
            System.out.println(111);
            ex.printStackTrace();
        } catch (Exception e) {
            System.out.println("服务器宕机了！！！！！！！！");
            controller.serverClose();
        } finally {
            try {
                s.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 用户连接请求类型消息
     *
     * @throws IOException
     */
    public void connect() {
        //System.out.println(userName+"00000");
        //创新连接消息
        Message message = new Message(System.currentTimeMillis(), userName, userPassword, null, null, MessageType.CONNECT);
        //发送
        send(message);
    }

    /**
     * 将message转化为字符串后发送
     *
     * @param message
     */
    public void send(Message message) {
        //转化为gson的字符串
        String JSON = Message.toJson(message);
        System.out.println("发送的消息内容：{} " + JSON);
        ps.println(JSON);
    }

    public static String list_to_string(CopyOnWriteArrayList<String> list) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < list.size() - 1; i++) {
            s.append(list.get(i)).append(";");
        }
        s.append(list.get(list.size() - 1));
        return s.toString();
    }

}
