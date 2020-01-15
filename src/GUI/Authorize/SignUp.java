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

public class SignUp {
    private static JFrame frame;
    private JButton signUpButton;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;
    private JPanel panel;
    private JButton backButton;
    private JLabel notification;
    private CustomURLConnection connection;

    private SignUp() {
        backButton.addActionListener(e -> {
            Main.StartForm(frame.getX(), frame.getY());
            frame.dispose();
        });
        signUpButton.addActionListener(e -> {
            if(textField1.getText().contains(" "))
                notification.setText("Username must not contain space");
            else if (Arrays.equals(passwordField1.getPassword(), passwordField2.getPassword()))
                try {
                    connection = (CustomURLConnection) new URL(PROTOCOL + "://"+SERVER_IP).openConnection();
                    connection.connect();
                    if (connection.sendData(new CustomPackage(REGISTER, textField1.getText() + " " + Arrays.toString(passwordField1.getPassword()), null, null)).equals("REGISTERED_SUCCESS")) {
                        MainProgram.LOGIN_AS = textField1.getText();
                        MainProgram.onlineList = new OnlineList(frame.getX(), frame.getY(),connection);
                        frame.dispose();
                    } else {
                        notification.setText("Username invalid.");
                        connection.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    notification.setText("Can't connect to server, please try again later.");
                    connection.close();
                }
            else
                notification.setText("Password not match");
        });
    }

    public static void StartForm(int x, int y) {
        frame = new JFrame("Sign up");
        frame.setContentPane(new SignUp().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);
        frame.setSize(480, 240);
        frame.setLocation(x, y);
    }
}
