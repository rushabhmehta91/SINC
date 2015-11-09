package caching;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by rushabhmehta91 on 4/13/15.
 */
public class ContentPacket implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// incomingPacketType: 0=request,1=reply,2=incomingContent
    private Content content;
    private Object data;

    public ContentPacket(Content content,Object data) {
        this.content = content;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }   
}
