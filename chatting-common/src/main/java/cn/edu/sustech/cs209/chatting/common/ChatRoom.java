package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRoom {
    private int roomId;
    private CopyOnWriteArrayList<String> userList;
    private CopyOnWriteArrayList<String> allMessage;
    public ChatRoom (int roomId, CopyOnWriteArrayList<String> userList, CopyOnWriteArrayList<String> allMessage){
        this.roomId = roomId;
        this.allMessage = allMessage;
        this.userList = userList;
    }

    public int getRoomId() {
        return roomId;
    }

    public CopyOnWriteArrayList<String> getUserList() {
        return userList;
    }

    public CopyOnWriteArrayList<String> getAllMessage() {
        return allMessage;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setAllMessage(CopyOnWriteArrayList<String> allMessage) {
        this.allMessage = allMessage;
    }

    public void setUserList(CopyOnWriteArrayList<String> userList) {
        this.userList = userList;
    }
}
