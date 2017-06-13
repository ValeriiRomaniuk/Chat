import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

public class client {
    private static JTextPane TAM;
    private static JTextField t, logT;
    private static JPasswordField pasT;
    private static JFrame f;
    private static BufferedReader reader;
    private static PrintWriter writer, setNameTable;
    private static String login, nameTable, item;
    private static ArrayList<String> list = new ArrayList<String>();

    public static void main(String args[]){
        enter();
    }

    private static void enter() {
        f = new JFrame("Авторизация");
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(365, 120);
        f.setResizable(true);
        f.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel log = new JLabel("Введите логин");
        log.setBounds(5, 5, 130, 20);
        panel.add(log);

        JLabel pas = new JLabel("Введите пароль");
        pas.setBounds(5, 30, 130, 20);
        panel.add(pas);

        logT = new JTextField();
        logT.setBounds(140, 5, 200, 20);
        panel.add(logT);

        pasT = new JPasswordField();
        pasT.setBounds(140, 30, 200, 20);
        panel.add(pasT);

        JButton entB = new JButton("Войти");
        entB.setBounds(135, 55, 100, 20);
        panel.add(entB);
        entB.addActionListener(new enterUs());

        JButton pasB = new JButton("Отмена");
        pasB.setBounds(240, 55, 100, 20);
        panel.add(pasB);

        JButton reg = new JButton("Регистрация");
        reg.setBounds(5, 55, 125, 20);
        panel.add(reg);
        reg.addActionListener(new sendUs());

        f.getContentPane().add(panel);
    }

    private static void go() {
        JFrame f = new JFrame("Java Chat");
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600, 510);
        f.setResizable(true);
        f.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel Luser = new JLabel("Сообщения");
        Luser.setBounds(122, 5, 150, 20);
        panel.add(Luser);

        t = new JTextField();
        t.setBounds(5, 435, 192, 20);
        panel.add(t);

        TAM = new JTextPane();
        TAM.setBounds(5, 25, 300, 400);
        JScrollPane sp = new JScrollPane(TAM);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(TAM);

        JButton b1 = new JButton("Отправить");
        b1.setBounds(211, 435, 95, 20);
        b1.addActionListener(new send());
        panel.add(b1);

        JLabel Lname = new JLabel("Вы вошли под именем:");
        Lname.setBounds(383, 5, 150, 20);
        panel.add(Lname);

        JTextArea JTname = new JTextArea();
        JTname.setBounds(375, 25, 150, 17);
        panel.add(JTname);
        JTname.setText(login);

        JLabel Lperech = new JLabel("Перечень пользователей:");
        Lperech.setBounds(375, 45, 170, 20);
        panel.add(Lperech);

        try {
            getList();
        } catch (Exception e) {}

        JComboBox comboBox = new JComboBox();
        comboBox.setBounds(320, 70, 250, 20);
        comboBox.addActionListener(new connectWithUser());
        panel.add(comboBox);

        for (String aList : list) {
            comboBox.addItem(aList);
        }

        setNet();
        setNet2();

        Thread thread = new Thread(new listener());
        thread.start();

        f.getContentPane().add(panel);
    }

    private static void getList() throws Exception {
        setDB set = new setDB();
        String SQL = "SELECT * FROM `messenger`.`users`";

        try {
            Statement st = setDB.getConnection().createStatement();
            ResultSet rs = st.executeQuery(SQL);
            list.add(0, null);

            while (rs.next()) {
                if (!rs.getString("login").contains(login)) {
                    list.add(rs.getString("login"));
                }
            }
        } catch (Exception e) {}
    }

    private static class connectWithUser implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            TAM.setText("");
            JComboBox box = (JComboBox)e.getSource();
            item = (String)box.getSelectedItem();
            System.out.println(item);
            nameTable = login + "_" + item;
            try {
                checkOfDialog();

            } catch (Exception e1) {}

            setNameTable.println(nameTable);
            setNameTable.flush();
        }

        private static void checkOfDialog() throws Exception {
            setDB set = new setDB();
            String SQLCheck = "SELECT * FROM `messenger`.`"+item + "_" + login+"`";

            try {
                Statement st = setDB.getConnection().createStatement();
                st.executeQuery(SQLCheck);
                nameTable = item + "_" + login;
            } catch(Exception exc) {
                String SQL = "create table `"+nameTable+"` \n" +
                        "(`#` int AUTO_INCREMENT,\n" +
                        "`login` varchar(20),\n" +
                        "`msg` text,\n" +
                        "PRIMARY KEY (`#`));";

                try {
                    Statement st = setDB.getConnection().createStatement();
                    st.execute(SQL);
                }catch (Exception e) {}
            }
        }
    }

    public static class listener implements Runnable{
        public void run() {
            String msg;
            int i = 0;
            TAM.setContentType("text/html");
            TAM.setEditable(false);
            HTMLDocument doc = (HTMLDocument) TAM.getStyledDocument();
            try{
                while((msg = reader.readLine())!=null){
                    int x = msg.indexOf(":");
                    if (!msg.substring(x + 2).contains("http")) {
                        try {
                            doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()),"<plaintext>"+msg+"</plaintext><br>");
                        } catch (BadLocationException e) {}
                    } else if (msg.substring(x + 2).contains("http")) {
                        try {
                            doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()), "<plaintext>"+msg
                                    .substring(0, x + 2) + "</plaintext> " +
                                    "<a href="+msg.substring(x + 2)+">"+msg.substring(x + 2)+"</a> <br>");
                        } catch (BadLocationException e) {}
                    }

                    for (; i < 1; i++) {
                        System.out.println(i);
                        TAM.addHyperlinkListener(new HyperlinkListener() {
                            public void hyperlinkUpdate(HyperlinkEvent e) {
                                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                    if (Desktop.isDesktopSupported()) {
                                        try {
                                            Desktop.getDesktop().browse(e.getURL().toURI());
                                        } catch (IOException e1) {} catch (URISyntaxException e1) {}
                                    }
                                }
                            }
                        });
                    }
                }
            } catch (Exception e){}
            i++;
        }
    }

    private static class sendUs implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                setDB set = new setDB();
            } catch (Exception e1) {}

            String check = JOptionPane.showInputDialog("Введите пароль ещё раз.");
            login = logT.getText();
            String pass = pasT.getText();

            if (pass.contains(check)) {
                String SQL = "INSERT INTO `messenger`.`users` (`login`, `password`) VALUES ('" + login + "', '" + pass + "');";
                try {
                    Statement st = setDB.getConnection().createStatement();
                    st.executeUpdate(SQL);
                } catch (Exception e1) {}

                logT.setText("");
                pasT.setText("");

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Вы зарегистрированы, повторите ввод " +
                        "данных и войдите.");
            } else {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Пароли не совпадают! Повторите " +
                        "регистрацию.");
                pasT.setText("");
            }
        }
    }

    private static class enterUs implements ActionListener {
        public void actionPerformed(ActionEvent r) {
            boolean True = false;

            try {
                setDB set = new setDB();
            } catch (Exception e1) {}

            login = logT.getText();
            String pass = pasT.getText();
            String SQL = "SELECT * FROM `messenger`.`users`";

            try {
                Statement st = setDB.getConnection().createStatement();
                ResultSet rs = st.executeQuery(SQL);

                while (rs.next()) {
                    if (login.contains(rs.getString("login")) && pass.contains(rs.getString("password"))) {
                        True = true;
                        break;
                    }
                }
            } catch (Exception e1) {}

            if (True) {
                f.dispose();
                go();
            } else {
                logT.setText("");
                pasT.setText("");
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Неверный логин или пароль.");
            }
        }
    }

    private static class send implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            String msg = login + ": " + t.getText();
            writer.println(msg);
            writer.flush();

            t.setText("");
            t.requestFocus();
        }
    }

    private static void setNet(){
        try{
            Socket sock = new Socket("localhost", 5678);
            InputStreamReader is = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(is);
            writer = new PrintWriter(sock.getOutputStream());
        } catch (Exception ex){}
    }

    private static void setNet2(){
        try{
            Socket sock = new Socket("localhost", 5679);
            setNameTable = new PrintWriter(sock.getOutputStream());
        } catch (Exception ex){}
    }
}