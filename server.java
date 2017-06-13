import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class server {
    private static PrintWriter writer;
    private static String nameTable1 = null;
    private static Statement st;
    private static ArrayList<PrintWriter> streams; // коллекция, в которой хранится PrintWriter'ы всех пользователей

    public static void main(String[] args) throws Exception {
        go();
    }

    private static void go() throws Exception {
        streams = new ArrayList<PrintWriter>();
        setDB();
        try {
            ServerSocket ss = new ServerSocket(5678);
            ServerSocket ss2 = new ServerSocket(5679);
            while(true){
                Socket sock = ss.accept();
                Socket socket = ss2.accept();

                writer = new PrintWriter(sock.getOutputStream());

                streams.add(writer); // добавляем writer в коллекцию

                Thread t = new Thread(new Listener(sock));
                t.start();

                Thread c = new Thread(new ListenerTable(socket));
                c.start();
            }
        } catch (Exception ex) {}
    }

    private static void sendHistory(String nameTable) throws Exception {
        String SQL = "SELECT msg FROM `messenger`.`"+nameTable+"`";
        ResultSet rs = st.executeQuery(SQL);

        while (rs.next()) {
            writer.println(rs.getString("msg"));
            writer.flush();
        }
    }

    private static void tellEveryone(String msg, String nameTable) throws Exception {
        int x = msg.indexOf(':');
        String login = msg.substring(0, x);

        save(login, msg, nameTable);

        Iterator<PrintWriter> it = streams.iterator(); // делаем итератор (можно использовать обычный цикл)
        while(it.hasNext()){
            try{
                writer = it.next();
                writer.println(msg);
                writer.flush();
            } catch(Exception ex){}
        }
    }

    private static void save(String login, String msg, String nameTable) throws Exception {
        String SQL = "INSERT INTO `messenger`.`"+nameTable+"` (`login`, `msg`) VALUES ('"+login+"', '"+msg+"');";
        st.executeUpdate(SQL);
    }

    private static void setDB() throws Exception {
        String url  = "jdbc:mysql://localhost/messenger";
        String login = "root";
        String pass = "";
        Class.forName("com.mysql.jdbc.Driver");
        Connection c = DriverManager.getConnection(url, login, pass);
        st = c.createStatement();
    }

    private static class Listener implements Runnable{
        BufferedReader reader;
        Listener(Socket sock){ // делаем конструктор, чтобы получить сокет
            InputStreamReader isMsg;
            try{
                isMsg = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isMsg);
            } catch (Exception e) {}
        }

        public void run() {
            String msg;
            try{
                while((msg = reader.readLine()) != null){
                    System.out.println(msg);
                    tellEveryone(msg, nameTable1);
                }
            } catch(Exception ex){}
        }
    }

    private static class ListenerTable implements Runnable {
        BufferedReader readNameTable;
        public ListenerTable(Socket sock) {
            InputStreamReader isName;
            try{
                isName = new InputStreamReader(sock.getInputStream());
                readNameTable = new BufferedReader(isName);
            } catch (Exception e) {}
        }

        public void run() {
            try{
                while((nameTable1 = readNameTable.readLine()) != null){
                    sendHistory(nameTable1);
                }
            } catch(Exception ex){}
        }
    }
}