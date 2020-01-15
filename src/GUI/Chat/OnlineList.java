package GUI.Chat;

import GUI.MainProgram;
import ProtocolHandler.CustomURLConnection;
import javax.swing.*;
import java.awt.event.*;
import java.util.Objects;

public class OnlineList extends JFrame{
    private volatile JPanel panel;
    private volatile JList<Object> list1;

    public OnlineList(int x, int y,CustomURLConnection connection) {
        JFrame frame = new JFrame("Online User");
        frame.setContentPane(this.panel);

        frame.pack();
        frame.setVisible(true);
        frame.setSize(480, 240);
        frame.setLocation(x, y);

        WindowAdapter adapter = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
               connection.close();
               System.exit(0);
            }
        };
        frame.addWindowListener(adapter);
        frame.addWindowFocusListener(adapter);

        list1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    Objects.requireNonNull(MainProgram.createChatbox(MainProgram.getAddress(list.getSelectedValue().toString()))).addMessage(null);
                }
            }
        });
    }

    public void updateGUI(Object[] list) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateGUI(list);
            });
            return;
        }
        list1.setListData(list);
    }
}

