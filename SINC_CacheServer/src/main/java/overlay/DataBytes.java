package overlay;


import java.io.Serializable;

public class DataBytes implements Serializable {

    public byte[] data;
        
    public DataBytes(byte[] data) {
        this.data = data;
    }        
}
