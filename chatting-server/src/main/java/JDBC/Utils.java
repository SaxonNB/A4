package JDBC;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            String sql = "select * from userinfo where userName = '" + name + "'";
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
                String sql = "select * from userinfo where userName = '" + name + "' and userPWD = '" + password + "'";
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
                conn.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 返回登陆用户的好友信息
     *
     * @param connections
     * @param userBean
     * @return
     */
   /* public ResultSet isMyFri(Connections connections, UserBean userBean) {
        Connection conn = connections.getCon();
        Statement statement;
        ResultSet rs = null;
        try {
            statement = conn.createStatement();
            String sql = "select * from friends where username = '" + userBean.getUserName() + "'";
            rs = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }*/

    /**
     * 删除好友操作指令
     *
     * @param connections
     * @param username
     * @param friendName
     */
   /* public void delFri(Connections connections, String username, String friendName) {
        Connection conn = connections.getCon();
        Statement statement;
        try {
            statement = conn.createStatement();
            String sql = "delete from friends where username = '" + username + "' and friendname = '" + friendName + "'";
            statement.execute(sql);
            String sql2 = "delete from friends where username = '" + friendName + "' and friendname = '" + username + "'";
            statement.execute(sql2);
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * 增加好友操作指令
     *
     * @param connections
     * @param username
     * @param friendName
     * @return
     */
    /*public boolean addFri(Connections connections, String username, String friendName) {
        Connection conn = connections.getCon();
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.createStatement();
            String sql = "select * from userinfo where userName = '" + friendName + "'";
            rs = statement.executeQuery(sql);
            if (rs.next()) {
                String sql3 = "insert into friends (username, friendname) values ('" + username + "','" + friendName + "' )";
                statement.execute(sql3);
                String sql4 = "insert into friends (username, friendname) values ('" + friendName + "','" + username + "' )";
                statement.execute(sql4);
                System.out.println("插入好友成功");
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                statement.close();
                conn.close();
            } catch (SQLException ee) {
                ee.printStackTrace();
            }
        }
        return true;
    }*/
}
