package ProtocolHandler;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class CustomPackage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private String message;
    private byte[] data;
    private ArrayList<MyEntry<String, InetAddress>> obj;

    public CustomPackage(String type, String message, byte[] data, ArrayList<MyEntry<String, InetAddress>> o){
        this.type=type;
        this.message=message;
        this.data=data;
        this.obj = o;
    }

    ArrayList<MyEntry<String, InetAddress>> getObj() {
        return obj;
    }

    byte[] getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
