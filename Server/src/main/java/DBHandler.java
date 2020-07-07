import java.sql.*;
import java.sql.Connection;

public class DBHandler {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:cloud.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // возвращает из БД порядковый номер клиента, который будет использован в качестве имени каталога для данного клиента
    public static String checkAccount(String name, String password) {
        try {
            String sql = String.format("SELECT n, name, password FROM main WHERE name = '%s' AND password= '%s'", name, password);
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                if (rs.getString("name").equals(name) &&
                    rs.getString("password").equals(password)){
                        return rs.getString("n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean regNewUser(String name, String password){
        String sql = String.format("INSERT INTO main (name, password) VALUES ('%s', '%s')", name, password);
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
