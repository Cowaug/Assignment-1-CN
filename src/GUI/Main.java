package GUI;

import GUI.Authorize.Login;
import GUI.Authorize.SignUp;
import ProtocolHandler.CustomURLConnection;
import sun.rmi.runtime.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import static ProtocolHandler.CustomURLStreamHandlerFactory.MESSAGE;
import static ProtocolHandler.CustomURLStreamHandlerFactory.PROTOCOL;

public class Main {
    private static JFrame frame;
    private JPanel panel;
    private JButton signUpButton;
    private JButton loginButton;

    public Main() {
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                SignUp.StartForm(frame.getX(),frame.getY());
                frame.dispose();
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Login.StartForm(frame.getX(),frame.getY());
                frame.dispose();
            }
        });
    }

    public static void StartForm(int x,int y) {
        frame = new JFrame("Welcome");
        frame.setContentPane(new Main().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);
        frame.setSize(480,240);
        frame.setLocation(x,y);
    }


}
