package cn.edu.sustech.cs209.chatting.common;

import com.google.gson.Gson;

public class Message {

    private Long timestamp;

    private String sentFrom;
    private String password;

    private String sendTo;

    private String data;

    private MessageType type;



    public Message(Long timestamp, String sentFrom, String password, String sendTo, String data, MessageType type) {
        this.timestamp = timestamp;
        this.sentFrom = sentFrom;
        this.password = password;
        this.sendTo = sendTo;
        this.data = data;
        this.type = type;
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
