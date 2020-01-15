package ProtocolHandler;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class CustomURLStreamHandlerFactory implements URLStreamHandlerFactory {

    final static int PORT = 65000;
    public final static String PROTOCOL = "customprotocol";
    public final static String MESSAGE = "MESSAGE";
    public final static String LOGIN = "LOGIN";
    public final static String REGISTER = "REGISTER";
    public final static String RESPONSE = "RESPONSE";
    public final static String QUIT = "QUIT";
    final static String FILE = "FILE";
    public final static String UPDATE = "UPDATE";
    //public static String SERVER_IP = 26.128.78.250;
    public static String SERVER_IP = null;

   @Override    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (PROTOCOL.equals(protocol)) {
            return new CustomURLStreamHandler();
        }
        return null;
    }

}
