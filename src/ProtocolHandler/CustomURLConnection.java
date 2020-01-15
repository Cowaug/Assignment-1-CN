package ProtocolHandler;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ProtocolHandler.CustomURLStreamHandlerFactory.*;

public class CustomURLConnection extends URLConnection {
    private String serverHost;
    private Socket socketOfClient = null;
    private ObjectOutputStream os;
    private ObjectInputStream is;

    CustomURLConnection(URL url) {
        super(url);
        serverHost = url.toString().replace(CustomURLStreamHandlerFactory.PROTOCOL + "://", "");
    }

    @Override
    public void connect() {
        try {
            if (socketOfClient == null) {
                socketOfClient = new Socket(serverHost, PORT);
                os = new ObjectOutputStream(socketOfClient.getOutputStream());
                is = new ObjectInputStream(socketOfClient.getInputStream());
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverHost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            sendData(new CustomPackage(QUIT, null, null, null));
            os.close();
            is.close();
            socketOfClient.close();
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }

    public String sendData(CustomPackage customPackage) {
        try {
            URL.setURLStreamHandlerFactory(new CustomURLStreamHandlerFactory());
        } catch (Error ignore) {

        }
        try {
            System.out.print(" >> " + serverHost + ": " + customPackage.getType() + " " + customPackage.getMessage());
            os.writeObject(customPackage);
            os.flush();
            os.flush();
            os.flush();

            CustomPackage responsePackage;
            if ((responsePackage = (CustomPackage) is.readObject()) != null) {
                System.out.println(" >> " + responsePackage.getType() + " " + responsePackage.getMessage());
                if (responsePackage.getType().equals(RESPONSE))
                    return responsePackage.getMessage();
            }

        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public void sendFile(String path) {
        try {
            sendData(new CustomPackage(FILE, path.substring(path.lastIndexOf("\\")), Files.readAllBytes(Paths.get(path)), null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}