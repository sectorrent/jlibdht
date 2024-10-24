package org.octorrent.jlibdht.rpc.events;

import org.octorrent.jlibdht.messages.inter.MessageBase;
import org.octorrent.jlibdht.rpc.events.inter.MessageEvent;
import org.octorrent.jlibdht.utils.Node;

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
