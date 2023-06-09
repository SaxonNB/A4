package cn.edu.sustech.cs209.chatting.server;

import JDBC.Utils;
import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.PieceMessage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static ArrayList<PrintWriter> outputStreams = new ArrayList<>();
    private static CopyOnWriteArrayList<ChatRoom> roomList = new CopyOnWriteArrayList<>();
    private static ConcurrentHashMap<String, ChatRoom> map = new ConcurrentHashMap<>();
    //用户名与client的hashmap
    private static ConcurrentHashMap<String, PrintWriter> name_To_printer = new ConcurrentHashMap<>();
    //连接服务器的客户端列表
    private static CopyOnWriteArrayList<String> clientList = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8588);
        int a = 0;
        Scanner end = new Scanner(System.in);
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println(socket.getInetAddress() + "连接上了本服务器");
            new Thread(new ServerThread(socket/*,this,roomList,map,name_To_printer,clientList,*/)).start();
        }
    }

    private static class ServerThread implements Runnable {
        private Server server;
        private CopyOnWriteArrayList<ChatRoom> roomList = new CopyOnWriteArrayList<>();
        private static ConcurrentHashMap<String, ChatRoom> map = new ConcurrentHashMap<>();
        //用户名与client的hashmap
        private static ConcurrentHashMap<String, PrintWriter> name_To_printer = new ConcurrentHashMap<>();
        //连接服务器的客户端列表
        private static CopyOnWriteArrayList<String> clientList = new CopyOnWriteArrayList<>();
        private Socket socket = null;
        private Scanner in;
        private PrintWriter selfout;
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
                selfout = new PrintWriter(socket.getOutputStream());
                Scanner end = new Scanner(System.in);
                Service(in, selfout);
               // System.out.println("hasdhsjkdhjkshdjhdak");
            } catch (SocketException se) { /*处理用户断开的异常*/
                System.out.println("处理用户断开的异常");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    socket.close();
                    in.close();
                    selfout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        public static void Service(Scanner in, PrintWriter selfout) throws ParseException, IOException {
            exit:
            while (true) {
                //System.out.println(in.hasNext());
                if (in.hasNext()) {
                    String line = in.nextLine();
                    Message message = Message.fromJson(line);
                    String username = message.getSentFrom();
                    String password = message.getPassword();

                    CopyOnWriteArrayList<String> nowUserList = message.getNowUserList();
                    Message tempMessage;
                    // String JSON;
                    switch (message.getType()) {
                        /**
                         * 连接成功，添加到服务器在线用户列表
                         * 给所有在线用户发消息，更新在线用户列表
                         */
                        case CONNECT:
                            // System.out.println(clientList);
                            if (!clientList.contains(username)) {
                                if (jdbc.hasSignIn(username) && jdbc.isMember(username, password)) {
                                    clientList.add(username);
                                    tempMessage = new Message(System.currentTimeMillis(), username, password, username, "", MessageType.SUCCESS);
                                    tempMessage.setNowUserList(clientList);
                                    System.out.println(clientList);
                                    send(tempMessage, selfout);
                                    tempMessage = new Message(System.currentTimeMillis(), null, null, null, null, MessageType.REFRESHCHATLIST);
                                    tempMessage.setRoomlist(jdbc.getAllRoomByName(username));
                                    send(tempMessage, selfout);
                                    Message message1 = new Message(System.currentTimeMillis(), username, null, null, "", MessageType.NEWCLIENTJION);
                                    message1.setNowUserList(clientList);
                                    outputStreams.add(selfout);
                                    name_To_printer.put(username, selfout);
                                    // System.out.println(name_To_printer+"??????");
                                    sendAllExceptSelf(message1, selfout);
                                } else if (!jdbc.hasSignIn(username)) {
                                    clientList.add(username);
                                    tempMessage = new Message(System.currentTimeMillis(), username, password, username, "", MessageType.SUCCESS);
                                    tempMessage.setNowUserList(clientList);
                                    send(tempMessage, selfout);
                                    jdbc.signIn(username, password);
                                    Message message1 = new Message(System.currentTimeMillis(), username, null, null, "", MessageType.NEWCLIENTJION);
                                    message1.setNowUserList(clientList);
                                    outputStreams.add(selfout);
                                    name_To_printer.put(username, selfout);
                                    //  System.out.println(name_To_printer+"??????");
                                    sendAllExceptSelf(message1, selfout);
                                }
                            } else {
                                tempMessage = new Message(System.currentTimeMillis(), username, password, username, "", MessageType.FAIL);
                                send(tempMessage, selfout);
                            }
                            break;
                        /**
                         * 用户断开连接，给服务器发请求
                         * 服务器给(除自己)所有在线用户发送消息，更新在线用户列表
                         * 并在服务器移除该客户端的线程,map
                         */
                        case DISCONNECT:
                           /* System.out.println(name_To_printer);
                            System.out.println(clientList+"   666");
                            System.out.println(outputStreams);
                            System.out.println(username);*/
                            message = new Message(System.currentTimeMillis(), username, null, null, "", MessageType.SOMEONELEAVE);
                            name_To_printer.remove(username);
                            clientList.remove(username);
                            outputStreams.remove(selfout);
                            message.setNowUserList(clientList);
                            sendAllExceptSelf(message, selfout);
                            System.out.println("duankai");
                            break exit;

                        /**
                         * 将消息添加到数据库
                         * 将该聊天室的所有聊天记录从数据库读出
                         * 判断toname端是否在线，若在线，给toname端发送聊天室所有消息
                         */
                        case SENDMESSAGE:
                            message.getChatRoom().setUserList(jdbc.getAllUserByRoomId(message.getChatRoom().getRoomId()));
                            jdbc.addMessageToDatabase(message);
                            String allusers = jdbc.copyList_to_String(message.getChatRoom().getUserList());
                            CopyOnWriteArrayList<String> roomUsers = message.getChatRoom().getUserList();
                            for (int i = 0; i < roomUsers.size(); i++) {
                                if (clientList.contains(roomUsers.get(i))){
                                    CopyOnWriteArrayList<ChatRoom> rooms = jdbc.getAllRoomByName(roomUsers.get(i));
                                    Message message1 = new Message(message.getTimestamp(), message.getSentFrom(),null,null,null,MessageType.RECIEVE);
                                    message1.setRoomlist(rooms);
                                    ChatRoom room = new ChatRoom(message.getChatRoom().getRoomId(),null,null);
                                    CopyOnWriteArrayList<PieceMessage> allMessages = jdbc.getHistoryMessageByRoomId(message.getChatRoom().getRoomId());
                                    message1.setAllHistoryMessage(allMessages);
                                    message1.setChatRoom(room);
                                    send(message1,name_To_printer.get(roomUsers.get(i)));
                                }
                            }
                            break;
                        /**
                         * 创建新的聊天
                         * 将聊天室加到数据库
                         * 此时聊天室的所有人一定在线
                         * 给聊天室中在线用户发消息，添加一个room
                         */
                        case CREATNEWROOM:
                            //System.out.println(message.getChatRoom().getUserList()+"0000000");
                            String allusers2 = jdbc.copyList_to_String(message.getChatRoom().getUserList());
                            if (!jdbc.checkRoomExist(allusers2)) {
                                int newroomid = jdbc.getnewRoomId();
                                message.getChatRoom().setRoomId(newroomid);
                                jdbc.addMessageToDatabase(message);
                            }else {
                                message.getChatRoom().setRoomId(jdbc.getRoomIdByAlluser(allusers2));
                            }
                            CopyOnWriteArrayList<String> roomUsers1 = message.getChatRoom().getUserList();
                            for (int i = 0; i < roomUsers1.size(); i++) {
                                if (clientList.contains(roomUsers1.get(i))){
                                    CopyOnWriteArrayList<ChatRoom> rooms = jdbc.getAllRoomByName(roomUsers1.get(i));
                                    Message message1 = new Message(message.getTimestamp(), message.getSentFrom(),null,null,null,MessageType.REFRESHCHATLIST);
                                    message1.setRoomlist(rooms);
                                    send(message1,name_To_printer.get(roomUsers1.get(i)));
                                    Message message2 = new Message(message.getTimestamp(), message.getSentFrom(),null,roomUsers1.get(i),null,MessageType.REFRESHCHATTHINGWINDOWS);
                                    ChatRoom room = new ChatRoom(message.getChatRoom().getRoomId(),null,null);
                                    CopyOnWriteArrayList<PieceMessage> allMessages = jdbc.getHistoryMessageByRoomId(message.getChatRoom().getRoomId());
                                    message2.setAllHistoryMessage(allMessages);
                                    message2.setChatRoom(room);
                                    send(message2,name_To_printer.get(roomUsers1.get(i)));
                                }
                            }
                            break;
                        /**
                         * 将客户端发送的文件存在服务器中
                         */
                        case SENDFIEL:
                            String filename = message.getFilename();
                            String filedata = message.getFiledata();
                            byte[] filecontent = Base64.getDecoder().decode(filedata);
                            try {
                                String path = "D:\\Java2\\A4\\chatting-server\\src\\main\\java\\AllFile\\"+filename;
                                Files.write(Paths.get(path),filecontent);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            CopyOnWriteArrayList<String> alluse = jdbc.getAllUserByRoomId(message.getChatRoom().getRoomId());
                            message.getChatRoom().setUserList(alluse);
                            jdbc.addMessageToDatabase(message);
                            for (int i = 0; i < alluse.size(); i++) {
                                if (clientList.contains(alluse.get(i))){
                                    CopyOnWriteArrayList<ChatRoom> rooms = jdbc.getAllRoomByName(alluse.get(i));
                                    Message message1 = new Message(message.getTimestamp(), message.getSentFrom(),null,null,null,MessageType.RECIEVE);
                                    message1.setRoomlist(rooms);
                                    ChatRoom room = new ChatRoom(message.getChatRoom().getRoomId(),null,null);
                                    CopyOnWriteArrayList<PieceMessage> allMessages = jdbc.getHistoryMessageByRoomId(message.getChatRoom().getRoomId());
                                    message1.setAllHistoryMessage(allMessages);
                                    message1.setChatRoom(room);
                                    send(message1,name_To_printer.get(alluse.get(i)));
                                }
                            }
                            break;
                        case ASKFORFILE:
                            CopyOnWriteArrayList<String> alluse1 = jdbc.getAllUserByRoomId(message.getChatRoom().getRoomId());
                            message.getChatRoom().setUserList(alluse1);
                            jdbc.addMessageToDatabase(message);
                            for (int i = 0; i < alluse1.size(); i++) {
                                if (clientList.contains(alluse1.get(i))){
                                    CopyOnWriteArrayList<ChatRoom> rooms = jdbc.getAllRoomByName(alluse1.get(i));
                                    Message message1 = new Message(message.getTimestamp(), message.getSentFrom(),null,null,null,MessageType.RECIEVE);
                                    message1.setRoomlist(rooms);
                                    ChatRoom room = new ChatRoom(message.getChatRoom().getRoomId(),null,null);
                                    CopyOnWriteArrayList<PieceMessage> allMessages = jdbc.getHistoryMessageByRoomId(message.getChatRoom().getRoomId());
                                    message1.setAllHistoryMessage(allMessages);
                                    message1.setChatRoom(room);
                                    send(message1,name_To_printer.get(alluse1.get(i)));
                                }
                            }
                            String path = "D:\\Java2\\A4\\chatting-server\\src\\main\\java\\AllFile\\"+message.getFilename();
                            File file = new File(path);
                            byte[] fileContent1 = Files.readAllBytes(file.toPath());
                            String encodedFile1 = Base64.getEncoder().encodeToString(fileContent1);
                            Message m=new Message(System.currentTimeMillis(),null,null,null,null,MessageType.RECIEVEFILE);
                            m.setFilename(message.getFilename());
                            m.setFiledata(encodedFile1);
                            send(m,name_To_printer.get(message.getSentFrom()));


                            break ;
                        default:
                            break;
                    }

                } else {
                    // System.out.println("0000000000000");
                    return;
                }
            }
            System.out.println("jieshu");
        }


        /**
         * 将copylist《piece》 转为 copylist《String》
         */
        public static CopyOnWriteArrayList<String> pieceList_to_stringList(CopyOnWriteArrayList<PieceMessage> pieceList) throws ParseException {
            CopyOnWriteArrayList<String> result = new CopyOnWriteArrayList<>();
            for (PieceMessage temp : pieceList) {
                String a = longToDate(temp.getSendtime()).toString();
                a += "     " + temp.getFrom() + "说： ";
                a += temp.getContent();
                result.add(a);
            }
            return result;
        }


        public static Date longToDate(long lo) throws ParseException {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //long转Date
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sd.format(new Date(lo)));
            return date;
        }


        /**
         * 向指定一组用户发消息
         */
        public static void sendToMany(Message message, CopyOnWriteArrayList<String> list) {
            for (int i = 0; i < list.size(); i++) {
                String temp = list.get(i);
                if (clientList.contains(temp)) {
                    send(message, name_To_printer.get(temp));
                }
            }
        }


        /**
         * 发送消息
         */
        public static void send(Message message, PrintWriter out) {
            String JSONMessage = Message.toJson(message);
            out.println(JSONMessage);
            out.flush();
        }

        /**
         * 向除自己以外所有人发消息
         */

        public static void sendAllExceptSelf(Message message, PrintWriter selfout) {

            for (PrintWriter out : outputStreams) {
                if (!out.equals(selfout)) {
                    send(message, out);
                }
            }
        }

        /**
         * 向所有人发消息（包括自己）
         */
        public static void sendAll(Message message) {
            for (PrintWriter out : outputStreams) {
                send(message, out);
            }
        }
    }


}

