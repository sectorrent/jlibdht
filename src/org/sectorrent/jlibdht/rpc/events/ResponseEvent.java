package org.sectorrent.jlibdht.rpc.events;

import org.sectorrent.jlibdht.messages.inter.MessageBase;
import org.sectorrent.jlibdht.rpc.events.inter.MessageEvent;
import org.sectorrent.jlibdht.utils.Node;

public class ResponseEvent extends MessageEvent {

    private MessageBase request;
    private long sentTime;

    public ResponseEvent(MessageBase message){
        super(message);
    }

    public ResponseEvent(MessageBase message, Node node){
        super(message, node);
    }

    public boolean hasRequest(){
        return (request != null);
    }

    public void setRequest(MessageBase request){
        this.request = request;
    }

    public MessageBase getRequest(){
        return request;
    }

    public void setSentTime(long sentTime){
        this.sentTime = sentTime;
    }

    public long getSentTime(){
        return sentTime;
    }
}
