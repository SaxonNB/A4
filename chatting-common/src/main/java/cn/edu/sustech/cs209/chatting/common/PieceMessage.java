package cn.edu.sustech.cs209.chatting.common;

public class PieceMessage {
    private int roomid;
    private String alluser;
    private String content;
    private long sendtime;
    private String from;

    public PieceMessage(int roomid, String alluser, String content, long sendtime, String from) {
        this.roomid = roomid;
        this.alluser = alluser;
        this.content = content;
        this.sendtime = sendtime;
        this.from = from;
    }

    public int getRoomid() {
        return roomid;
    }

    public long getSendtime() {
        return sendtime;
    }

    public String getAlluser() {
        return alluser;
    }

    public String getContent() {
        return content;
    }

    public String getFrom() {
        return from;
    }
}
