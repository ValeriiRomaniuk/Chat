import java.sql.Connection;
import java.sql.DriverManager;

public class setDB {

    public static Connection c;

    public setDB() throws Exception {
        String url = "jdbc:mysql://localhost/messenger";
        String login = "root";
        String pass = "";
        c = DriverManager.getConnection(url, login, pass);
    }

    public static Connection getConnection() {
        return c;
    }
}