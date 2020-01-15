package GUI;

import GUI.Chat.Chatbox;
import GUI.Chat.OnlineList;
import ProtocolHandler.CustomURLStreamHandlerFactory;

import static ProtocolHandler.CustomURLStreamHandlerFactory.*;

import ProtocolHandler.MyEntry;
import ProtocolHandler.Server;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainProgram {
    public static String PATH;

    static {
        try {
            PATH = URLDecoder.decode(MainProgram.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
            PATH = PATH.substring(1,PATH.lastIndexOf("/"));
            System.out.println(PATH);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String LOGIN_AS;
    private static ArrayList<MyEntry<String, InetAddress>> ONLINE_USER = new ArrayList<>();

    public static OnlineList onlineList;

    private static ArrayList<Chatbox> CHATTING_USER_UI = new ArrayList<>();
    private static ArrayList<String> OPENING_CONNECTION = new ArrayList<>();

    public static void main(String[] args) {
        if (SERVER_IP == null)
            if (args.length > 0)
                SERVER_IP = args[0];
            else
                SERVER_IP = "26.135.76.71";
        System.out.println(SERVER_IP);
        URL.setURLStreamHandlerFactory(new CustomURLStreamHandlerFactory());
        new Thread(() -> {
            try {
                Server.startServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Main.StartForm(10, 10);
    }

    public static InetAddress getAddress(String username) {
        for (MyEntry<String, InetAddress> entry : ONLINE_USER) {
            if (entry.getKey().equals(username)) return entry.getValue();
        }
        return null;
    }

    public static String getUsername(InetAddress address) {
        for (MyEntry<String, InetAddress> entry : ONLINE_USER) {
            if (entry.getValue().equals(address)) return entry.getKey();
        }
        return "<unknown>";
    }

    public static Chatbox createChatbox(InetAddress address) {
        if (address == null) return null;
        if (!MainProgram.OPENING_CONNECTION.contains(address.toString())) {
            MainProgram.OPENING_CONNECTION.add(address.toString());

            Chatbox cb = new Chatbox(getUsername(address) + " at " + address.toString(), address.toString());
            MainProgram.CHATTING_USER_UI.add(cb);
            return cb;
        } else {
            return MainProgram.CHATTING_USER_UI.get(MainProgram.OPENING_CONNECTION.indexOf(address.toString()));
        }
    }

    public static void updateOnlineUser(ArrayList<MyEntry<String, InetAddress>> onlineUser) {
        //update online user list
        ONLINE_USER = onlineUser;

        //get online user name
        Object[] obj = new Object[ONLINE_USER.size()];
        for (int i = 0; i < ONLINE_USER.size(); i++) {
            obj[i] = ONLINE_USER.get(i).getKey();
        }

        //convert array to list
        List list = Arrays.asList(obj);
        for (Chatbox cb : CHATTING_USER_UI) {
            if (!list.contains(cb.getName())) {
                cb.userOffline();
            }
            if (!cb.isOnline()) {
                cb.addMessage(null);
            }
        }
        onlineList.updateGUI(obj);
    }


}
