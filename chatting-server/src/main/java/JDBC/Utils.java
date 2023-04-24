package JDBC;


import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.PieceMessage;

import java.sql.*;
import java.text.SimpleDateFormat;
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
            } else {
                signIn(name, password);
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
     * 获取最大房间号+1
     */
    public int getnewRoomId() {
        Connection conn = connection.getCon();
        try {
            PreparedStatement stmt = conn.prepareStatement("select  roomid from chatroom order by roomid desc limit 1;");
            ResultSet rs = stmt.executeQuery();
            int roomid = 0;
            if (rs.next()) {
                roomid = rs.getInt(1) + 1;
            }
            rs.close();
            stmt.close();
            return roomid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }


    /**
     * 根据群成员获取所有历史消息
     */
    public CopyOnWriteArrayList<PieceMessage> getHistoryMessageByAllUser(String alluser) {
        Connection conn = connection.getCon();
        CopyOnWriteArrayList<PieceMessage> allMessage = new CopyOnWriteArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement("select * from chatroom as a where a.alluser like ? order by a.sendtime;");
            stmt.setString(1, alluser);
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
     * 根据群id获取所有历史消息
     */
    public CopyOnWriteArrayList<PieceMessage> getHistoryMessageByRoomId(int roomId) {
        Connection conn = connection.getCon();
        CopyOnWriteArrayList<PieceMessage> allMessage = new CopyOnWriteArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement("select * from chatroom as a where a.roomid = ? order by a.sendtime;");
            stmt.setInt(1, roomId);
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
     * 判断该群组是否已经存在
     */
    public boolean checkRoomExist(String alluser) {
        Connection conn = connection.getCon();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT roomid from chatroom where alluser = ? group by roomid limit 1;");
            stmt.setString(1, alluser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
            rs.close();
            stmt.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    /**
     * 根据用户名获取所有房间
     */
    public CopyOnWriteArrayList<ChatRoom> getAllRoomByName(String name) {
        Connection conn = connection.getCon();
        try {
            PreparedStatement stmt = conn.prepareStatement("select a.alluser,max(a.sendtime) as time from chatroom a  where a.alluser like ? group by a.alluser order by time desc ;");
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            CopyOnWriteArrayList<ChatRoom> roomlist = new CopyOnWriteArrayList<>();
            while (rs.next()) {
                String alluser = rs.getString(1);
                CopyOnWriteArrayList<String> list = string_to_copylist(alluser);
                ChatRoom temp = new ChatRoom(-1, list, null);
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
     * 根据alluser获取房间id
     */
    public int getRoomIdByAlluser(String allusers) {
        Connection conn = connection.getCon();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT roomid from chatroom where alluser = ? limit 1 ;");
            stmt.setString(1, allusers);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();
            stmt.close();
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 将聊天信息添加到数据库
     */
    public void addMessageToDatabase(Message message) {
        if (message.getData() != null) {
            long timestemp = message.getTimestamp();
            int roomid = message.getChatRoom().getRoomId();
            String from = message.getSentFrom();
            String data = message.getData();
            String alluser = copyList_to_String(message.getChatRoom().getUserList());
            Connection conn = connection.getCon();
            try {
                PreparedStatement stmt = conn.prepareStatement("insert into chatroom values (?,?,?,?,?)");
                stmt.setInt(1, roomid);
                stmt.setString(2, alluser);
                stmt.setString(3, data);
                Timestamp timestamp1q = new Timestamp(message.getTimestamp());
                SimpleDateFormat dfq = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                stmt.setTimestamp(4, Timestamp.valueOf(dfq.format(timestamp1q)));
                stmt.setString(5, from);
                stmt.executeUpdate();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param list
     * @return
     */

    public String copyList_to_String(CopyOnWriteArrayList<String> list) {
        String result = "";
        for (int i = 0; i < list.size() - 1; i++) {
            result += list.get(i) + ";";
        }
        result += list.get(list.size() - 1);
        return result;
    }

    public CopyOnWriteArrayList<String> string_to_copylist(String string) {
        String[] temp = string.split(";");
        CopyOnWriteArrayList<String> result = new CopyOnWriteArrayList<>(Arrays.asList(temp));
        return result;
    }


}
