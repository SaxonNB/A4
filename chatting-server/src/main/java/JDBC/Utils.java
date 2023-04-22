package JDBC;


import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.PieceMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 对数据库的操作
 *
 * @author York
 * @date 2018-11-30 15:07:06
 */
public class Utils {
    private static Connections connection = new Connections();

    /**
     * 判断是否用户已注册
     */
    public boolean hasSignIn(String name) {
        Connection conn = connection.getCon();
        try {
            Statement statement = conn.createStatement();
            String sql = "select * from userinfo where userName = '" + name + "';";
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                return true;
            }
            statement.close();
            rs.close();
          //  conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 注册用户
     */
    public void signIn(String name, String password) {
        Connection conn = connection.getCon();
        try {
            Statement statement = conn.createStatement();
            String sql = "insert into userinfo values ('" + name + "','" + password + "');";
            statement.execute(sql);
            statement.close();
          //  conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否为正确的登陆用户
     * 未注册用户会自动注册，并自动登录
     * 已注册用户会判断账号密码是否正确
     */
    public boolean isMember(String name, String password) {
        Connection conn = connection.getCon();
        try {
            Statement statement = conn.createStatement();
            if (hasSignIn(name)) {
                String sql = "select * from userinfo where userName = '" + name + "' and userPWD = '" + password + "';";
                ResultSet rs = statement.executeQuery(sql);
                if (rs.next()) {
                    return true;
                }
                statement.close();
                rs.close();
              //  conn.close();
            }else {
                signIn(name,password);
                statement.close();
                //conn.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据room查询所有信息
     */
    public CopyOnWriteArrayList<String> getAllUserByRoomId(int roomId) {
        Connection conn = connection.getCon();
        String allUser = null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT alluser FROM chatroom WHERE roomid = ? limit 1;");
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                allUser = rs.getString(1);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
       return string_to_copylist(allUser);
    }

    /**
     * 根据房间号获取所有历史消息
     */
    public CopyOnWriteArrayList<PieceMessage> getHistoryMessageByAllUser(String alluser){
        Connection conn = connection.getCon();
        CopyOnWriteArrayList<PieceMessage> allMessage = new CopyOnWriteArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * from chatroom where alluser = ?;");
            stmt.setString(1,alluser);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PieceMessage message = new PieceMessage(rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getTimestamp(4).getTime(),
                        rs.getString(5));
                allMessage.add(message);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allMessage;
    }

    /**
     *根据用户名获取所有房间
     */
    public CopyOnWriteArrayList<ChatRoom> getAllRoomByName(String name){
        Connection conn = connection.getCon();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT roomid, alluser from chatroom where alluser like ? order by roomid;");
            stmt.setString(1,"%"+name+"%");
            ResultSet rs = stmt.executeQuery();
            CopyOnWriteArrayList<ChatRoom> roomlist = new CopyOnWriteArrayList<>();
            while (rs.next()) {
                String alluser = rs.getString(2);
                CopyOnWriteArrayList<String> list = string_to_copylist(alluser);
                ChatRoom temp = new ChatRoom(rs.getInt(1),list,null);
                roomlist.add(temp);
            }
            rs.close();
            stmt.close();
            return roomlist;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     *将聊天信息添加到数据库
     */
    public void addMessageToDatabase(Message message){
        long timestemp = message.getTimestamp();
        int roomid = message.getChatRoom().getRoomId();
        String from = message.getSentFrom();
        String data = message.getData();
        String alluser = copyList_to_String(message.getChatRoom().getUserList());
        Connection conn = connection.getCon();
        try {
            PreparedStatement stmt = conn.prepareStatement("insert into chatroom values (?,?,?,?,?)");
            stmt.setInt(1, roomid);
            stmt.setString(2,alluser);
            stmt.setString(3,data);
            stmt.setTimestamp(4,Timestamp.valueOf(String.valueOf(timestemp)));
            stmt.setString(5,from);
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param list
     * @return
     */

    public String copyList_to_String(CopyOnWriteArrayList<String> list){
        StringBuilder result = null;
        for (int i = 0; i < list.size()-1; i++) {
            result.append(list.get(i)).append(";");
        }
        result.append(list.get(list.size() - 1));
        return result.toString();
    }

    public CopyOnWriteArrayList<String> string_to_copylist(String string){
        String[] temp = string.split(";");
        CopyOnWriteArrayList<String> result = new CopyOnWriteArrayList<>(Arrays.asList(temp));
        return result;
    }



}
