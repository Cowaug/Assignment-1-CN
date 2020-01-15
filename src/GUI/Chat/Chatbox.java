package GUI.Chat;

import GUI.MainProgram;
import ProtocolHandler.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;

import static GUI.MainProgram.LOGIN_AS;
import static GUI.MainProgram.PATH;
import static ProtocolHandler.CustomURLStreamHandlerFactory.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Chatbox extends JFrame {
    private JFrame frame;
    private volatile JTextArea messageHistory;
    private volatile JTextField messageField;
    private volatile JButton sendButton;
    private volatile JPanel panel;
    private String title;
    private String username;
    private JButton selectFile;
    private String file = null;
    private CustomURLConnection connection = null;

    public Chatbox(String title, String hostAddress) {
        this.title = title;
        this.username = title.replace(" at " + hostAddress, "");
        frame = new JFrame(title);
        frame.setContentPane(this.panel);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(480, 240);
        messageHistory.setEditable(false);
        try {
            if (connection == null)
                this.connection = (CustomURLConnection) new URL(PROTOCOL + ":/" + hostAddress).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadMessage();

        selectFile.addActionListener(e -> {
            FileDialog dialog = new FileDialog((Frame) null, "Select file to send, press Cancel to send nothing");
            dialog.setMode(FileDialog.LOAD);
            dialog.setVisible(true);
            file = dialog.getDirectory() + dialog.getFile();
            System.out.println(file + " chosen.");
            if (!("nullnull".equals(file) || file == null)) {
                selectFile.setText(dialog.getFile());
            }else {
                selectFile.setText("Select file");
            }

        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                messageField.requestFocus();
            }
        });

        sendButton.addActionListener(e -> {
            if (connection == null) return;
            connection.connect();
            if (!("nullnull".equals(file) || file == null)) {
                connection.sendFile(file);
                messageHistory.append("Sent " + file + " to " + username + "\n");
                selectFile.setText("Select file");
                file = null;
            }
            if (!messageField.getText().equals("")) {
                connection.sendData(new CustomPackage(MESSAGE, messageField.getText(), null, null));
                messageHistory.append(messageField.getText() + "\n");
                messageField.setText("");
                messageField.requestFocus();
                messageHistory.setCaretPosition(messageHistory.getDocument().getLength() - 1);
            }
            saveMessage();
        });
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    sendButton.doClick();
            }
        });
    }

    public void addMessage(final String newMessage) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> addMessage(newMessage));
            return;
        }
        if (newMessage != null) {
            messageHistory.append(username + ": " + newMessage + "\n");
            reconnect();
            int w = frame.getWidth();
            int h = frame.getHeight();
            frame.setContentPane(this.panel);
            frame.pack();
            frame.setVisible(true);
            frame.setSize(w, h);
            messageHistory.setCaretPosition(messageHistory.getDocument().getLength() - 1);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    messageField.requestFocus();
                }
            });
        } else {
            frame.setVisible(true);
            reconnect();
        }
        saveMessage();
    }

    private void reconnect() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::reconnect);
            return;
        }

        if (connection == null) {
            try {
                this.connection = (CustomURLConnection) new URL(PROTOCOL + ":/" + MainProgram.getAddress(username)).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                frame.setTitle(title);
                sendButton.setEnabled(true);
                selectFile.setEnabled(true);
            }
        }
    }

    public void userOffline() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::userOffline);
            return;
        }

        connection = null;
        frame.setTitle(username + " (OFFLINE)");
        sendButton.setEnabled(false);
        selectFile.setEnabled(false);
    }

    public String getName() {
        return username;
    }

    public boolean isOnline() {
        return connection != null;
    }

    private void saveMessage() {
        new File(PATH + "/" + LOGIN_AS).mkdir();
        new File(PATH + "/" + LOGIN_AS + "/" + this.username + ".txt").delete();
        try (FileWriter fileWriter = new FileWriter(PATH + "/" + LOGIN_AS + "/" + this.username + ".txt", true)) {
            fileWriter.write(messageHistory.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMessage() {
        try {
            BufferedReader bufferreader = new BufferedReader(new FileReader(PATH + "/" + LOGIN_AS + "/" + this.username + ".txt"));
            String line;
            while ((line = bufferreader.readLine()) != null) {
                messageHistory.append(line+"\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}