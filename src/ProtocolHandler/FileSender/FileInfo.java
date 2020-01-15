package ProtocolHandler.FileSender;

import java.io.Serializable;
import java.io.StringReader;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String destinationDirectory;
    private String filename;
    private byte[] dataBytes;



    public void setDestinationDirectory(String destinationDirectory){
        this.destinationDirectory=destinationDirectory;
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}
