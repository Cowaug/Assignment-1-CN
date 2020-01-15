package Server;

import ProtocolHandler.CustomPackage;
import ProtocolHandler.CustomURLConnection;
import ProtocolHandler.CustomURLStreamHandlerFactory;
import ProtocolHandler.MyEntry;

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;

import static GUI.MainProgram.PATH;
import static ProtocolHandler.CustomURLStreamHandlerFactory.*;

@SuppressWarnings("ALL")
public class Server extends Thread {
    private final int serverPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();
    static ArrayList<MyEntry<String, String>> userList = new ArrayList<>();
    static ArrayList<MyEntry<String, InetAddress>> onlineUser = new ArrayList<>();

    private Server(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("About to accept client connection at " + InetAddress.getLocalHost() + " is " + serverSocket.getLocalPort());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("** Accepted connection from " + clientSocket);
                ServerWorker worker = null;
                worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }

    public static void main(String[] args) {
        Server.loadUserLIst();
        int port = 65000;
        Server server = new Server(port);
        server.start();
    }

    static void updateOnlineUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    URL.setURLStreamHandlerFactory(new CustomURLStreamHandlerFactory());
                } catch (Error ignored) {

                }

                for (MyEntry<String, InetAddress> entry : onlineUser) {
                    try {
                        CustomURLConnection connection = (CustomURLConnection) new URL(PROTOCOL + ":/" + entry.getValue().toString()).openConnection();
                        connection.connect();
                        connection.sendData(new CustomPackage(UPDATE, null, null, onlineUser));
                        connection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    static void saveUserlist() {
        new File(PATH + "user.txt").delete();
        try (FileWriter fileWriter = new FileWriter(PATH + "/user.txt", true)) {
            for (int i = 0; i < Server.userList.size(); i++) {
                fileWriter.write(Server.userList.get(i).getKey() + "\n");
                fileWriter.write(Server.userList.get(i).getValue() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadUserLIst() {
        try {
            BufferedReader bufferReader = new BufferedReader(new FileReader(PATH + "/user.txt"));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                Server.userList.add(new MyEntry<>(line, bufferReader.readLine()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private volatile boolean stop = false;

    ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleClientSocket() throws Exception {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();

        ObjectInputStream reader = new ObjectInputStream(inputStream);
        ObjectOutputStream writer = new ObjectOutputStream(outputStream);

        CustomPackage customPackage;
        while (!stop && (customPackage = (CustomPackage) reader.readObject()) != null) {

            System.out.print("INCOMING: " + clientSocket.getInetAddress() + " : " + customPackage.getType() + " " + customPackage.getMessage());

            check:
            switch (customPackage.getType()) {
                case LOGIN: {
                    String username = customPackage.getMessage().substring(0, customPackage.getMessage().indexOf(" "));
                    String password = customPackage.getMessage().replace(username, "");
                    if (!isLogin(username)) {
                        for (MyEntry<String, String> entry : Server.userList) {
                            if (entry.getKey().equals(username)
                                    && entry.getValue().equals(password)
                            ) {
                                writer.writeObject(new CustomPackage(RESPONSE, "LOGIN_SUCCESS", null, null));
                                System.out.println(" >> LOGIN SUCCESS");
                                Server.onlineUser.add(new MyEntry<>(username, clientSocket.getInetAddress()));
                                Server.saveUserlist();
                                Server.updateOnlineUser();
                                break check;
                            }
                        }
                        writer.writeObject(new CustomPackage(RESPONSE, "LOGIN_FAIL", null, null));
                        System.out.println(" >> LOGIN FAIL");
                    } else {
                        writer.writeObject(new CustomPackage(RESPONSE, "ALREADY_LOGIN", null, null));
                        System.out.println(" >> ALREADY LOGIN");
                    }
                    break;
                }
                case REGISTER: {
                    String username = customPackage.getMessage().substring(0, customPackage.getMessage().indexOf(" "));
                    String password = customPackage.getMessage().replace(username, "");
                    for (MyEntry<String, String> entry : Server.userList) {
                        if (entry.getKey().equals(username)) {
                            writer.writeObject(new CustomPackage(RESPONSE, "REGISTERED_FAIL", null, null));
                            System.out.println(" >> REGISTERED_FAIL");
                            break check;
                        }
                    }
                    Server.userList.add(new MyEntry<>(username, password));
                    writer.writeObject(new CustomPackage(RESPONSE, "REGISTERED_SUCCESS", null, null));
                    System.out.println(" >> REGISTERED_SUCCESS");
                    Server.onlineUser.add(new MyEntry<>(username, clientSocket.getInetAddress()));
                    Server.updateOnlineUser();
                    Server.saveUserlist();
                    break;
                }
                case QUIT: {
                    writer.writeObject(new CustomPackage(RESPONSE, "OK", null, null));
                    System.out.println(" >> OK");
                    close();
                    break;
                }
            }
        }
    }

    private void close() throws IOException {
        server.removeWorker(this);
        for (MyEntry<String, InetAddress> entry : Server.onlineUser) {
            if (entry.getValue().equals(clientSocket.getInetAddress())) {
                System.out.println("** Close connection to " + clientSocket.getInetAddress());
                Server.onlineUser.remove(entry);
                break;
            }
        }
        clientSocket.close();
        stop = true;
        Server.updateOnlineUser();
    }

    private boolean isLogin(String username) {
        for (MyEntry<String, InetAddress> entry : Server.onlineUser) {
            if (entry.getKey().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
