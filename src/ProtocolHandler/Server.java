package ProtocolHandler;

import GUI.Chat.Chatbox;
import GUI.MainProgram;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


import java.io.*;


import static GUI.MainProgram.*;
import static ProtocolHandler.CustomURLStreamHandlerFactory.*;


@SuppressWarnings("ALL")
public class Server extends Thread {
    private final int serverPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();
    private Server(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("About to accept client connection at " + InetAddress.getLocalHost() + ":" + serverSocket.getLocalPort());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("** Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
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

    public static void startServer() {
        int port = 65000;
        Server server = new Server(port);
        server.start();
    }
}

class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private Chatbox cb = null;
    private volatile boolean stop = false;

    ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

        }
    }

    private void handleClientSocket() throws IOException, ClassNotFoundException {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();

        ObjectInputStream reader = new ObjectInputStream(inputStream);
        ObjectOutputStream writer = new ObjectOutputStream(outputStream);

        CustomPackage customPackage;
        while (!stop && (customPackage = (CustomPackage) reader.readObject()) != null) {

            System.out.println("INCOMING: " + clientSocket.getInetAddress() + " : " + customPackage.getType() + " " + customPackage.getMessage());

            writer.writeObject(new CustomPackage(RESPONSE, "OK", null, null));
            switch (customPackage.getType()) {
                case MESSAGE: {
                    if (cb == null) this.cb = MainProgram.createChatbox(clientSocket.getInetAddress());
                    if (!MainProgram.getUsername(clientSocket.getInetAddress()).equals(LOGIN_AS))
                        cb.addMessage(customPackage.getMessage());
                    break;
                }
                case QUIT: {
                    close();
                    break;
                }
                case FILE: {
                    if (cb == null) this.cb = MainProgram.createChatbox(clientSocket.getInetAddress());
                    cb.addMessage("Recieved file: " + customPackage.getMessage());
                    createFile(customPackage);
                    break;
                }
                case UPDATE: {
                    updateOnlineUser(customPackage.getObj());
                    break;
                }
            }

        }
    }

    private void close() throws IOException {
        server.removeWorker(this);
        clientSocket.close();
        stop = true;
    }

    private void createFile(CustomPackage customPackage) {
        BufferedOutputStream bos = null;

        try {
            if (customPackage != null) {
                File fileReceive = new File(PATH + "/" + customPackage.getMessage());
                bos = new BufferedOutputStream(
                        new FileOutputStream(fileReceive));
                bos.write(customPackage.getData());
                bos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}