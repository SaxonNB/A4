package cn.edu.sustech.cs209.chatting.common;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Message {

    private Long timestamp;

    private String sentFrom;
    private String password;

    private String sendTo;

    private String data;

    private MessageType type;
    private ChatRoom chatRoom;
    private CopyOnWriteArrayList<String> nowUserList;
    private CopyOnWriteArrayList<PieceMessage> allHistoryMessage;
    private CopyOnWriteArrayList<ChatRoom> roomlist;
    private String filename;
    private String filedata;

    public String getFiledata() {
        return filedata;
    }

    public void setFiledata(String filedata) {
        this.filedata = filedata;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public Message(Long timestamp, String sentFrom, String password, String sendTo, String data, MessageType type) {
        this.timestamp = timestamp;
        this.sentFrom = sentFrom;
        this.password = password;
        this.sendTo = sendTo;
        this.data = data;
        this.type = type;
    }

    public CopyOnWriteArrayList<ChatRoom> getRoomlist() {
        return roomlist;
    }

    public void setRoomlist(CopyOnWriteArrayList<ChatRoom> roomlist) {
        this.roomlist = roomlist;
    }

    public void setAllHistoryMessage(CopyOnWriteArrayList<PieceMessage> allHistoryMessage) {
        this.allHistoryMessage = allHistoryMessage;
    }

    public CopyOnWriteArrayList<PieceMessage> getAllHistoryMessage() {
        return allHistoryMessage;
    }

    public CopyOnWriteArrayList<String> getNowUserList() {
        return nowUserList;
    }

    public void setNowUserList(CopyOnWriteArrayList<String> nowUserList) {
        this.nowUserList = nowUserList;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public MessageType getType() {
        return type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentFrom(){return sentFrom;}

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public String getPassword(){return password;}
    public static String toJson(Message message1) {
        Gson gson = new Gson();
        return gson.toJson(message1);
    }

    public static Message fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }

}
