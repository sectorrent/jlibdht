package org.sectorrent.jlibdht.rpc.events;

import org.sectorrent.jlibdht.messages.inter.MessageBase;
import org.sectorrent.jlibdht.rpc.events.inter.MessageEvent;
import org.sectorrent.jlibdht.utils.Node;

public class StalledEvent extends MessageEvent {

    private long sentTime;

    public StalledEvent(MessageBase message){
        super(message);
    }

    public StalledEvent(MessageBase message, Node node){
        super(message, node);
    }

    public void setSentTime(long sentTime){
        this.sentTime = sentTime;
    }

    public long getSentTime(){
        return sentTime;
    }
}
