package GUI.Authorize;

import GUI.Chat.OnlineList;
import GUI.Main;
import GUI.MainProgram;
import ProtocolHandler.CustomPackage;
import ProtocolHandler.CustomURLConnection;

import javax.swing.*;
import java.net.URL;
import java.util.Arrays;

import static ProtocolHandler.CustomURLStreamHandlerFactory.*;

public class Login {
    private static JFrame frame;
    private JButton loginButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel panel;
    private JButton backButton;
    private JLabel notification;
    private CustomURLConnection connection;

    public Login() {
        backButton.addActionListener(e -> {
            Main.StartForm(frame.getX(), frame.getY());
            frame.dispose();
        });
        loginButton.addActionListener(e -> {
            try {
                connection = (CustomURLConnection) new URL(PROTOCOL + "://" + SERVER_IP).openConnection();
                connection.connect();
                String result = connection.sendData(new CustomPackage(LOGIN, usernameField.getText() + " " + Arrays.toString(passwordField.getPassword()), null, null));
                switch (result) {
                    case "LOGIN_SUCCESS":
                        MainProgram.LOGIN_AS = usernameField.getText();
                        MainProgram.onlineList = new OnlineList(frame.getX(), frame.getY(), connection);
                        frame.dispose();
                        break;
                    case "LOGIN_FAIL":
                        notification.setText("Incorrect username or password.");
                        connection.close();
                        break;
                    case "ALREADY_LOGIN":
                        notification.setText("Already login somewhere else");
                        connection.close();
                        break;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                notification.setText("Can't connect to server, please try again later.");
                connection.close();
            }
        });
    }

    public static void StartForm(int x, int y) {
        frame = new JFrame("Login");
        frame.setContentPane(new Login().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);
        frame.setSize(480, 240);
        frame.setLocation(x, y);

    }
}
