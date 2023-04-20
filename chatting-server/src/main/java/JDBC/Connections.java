package JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 获取数据库连接
 *
 * @author York
 * @date 2018-11-30 15:08:58
 */

public class Connections {
    public static String host = "localhost";
    public static String dbname = "java2as2";
    public static String user = "postgres";
    public static String pwd = "sjm405vn666";
    public static String port = "5432";
    private static Connection conn = null;


    public Connections() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            conn = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载驱动
     */
    /*public Connections() {
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASSWD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /**
     * 获取连接
     *
     * @return
     */
    public Connection getCon() {
        return conn;
    }
}
