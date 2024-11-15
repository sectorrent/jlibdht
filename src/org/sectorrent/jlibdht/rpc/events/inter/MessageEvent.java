package org.sectorrent.jlibdht.rpc.events.inter;

import org.sectorrent.jlibdht.messages.inter.MessageBase;
import org.sectorrent.jlibdht.utils.Node;

public class MessageEvent extends Event {

    protected MessageBase message;
    protected Node node;
    protected long receivedTime;

    public MessageEvent(MessageBase message){
        this.message = message;
    }

    public MessageEvent(MessageBase message, Node node){
        this(message);
        this.node = node;
    }

    public MessageBase getMessage(){
        return message;
    }

    public boolean hasNode(){
        return (node != null);
    }

    public void setNode(Node node){
        this.node = node;
    }

    public Node getNode(){
        return node;
    }

    /*
    public void setSentTime(long sentTime){
        this.sentTime = sentTime;
    }

    public long getSentTime(){
        return sentTime;
    }
    */

    public void setReceivedTime(long receivedTime){
        this.receivedTime = receivedTime;
    }

    public long getReceivedTime(){
        return receivedTime;
    }

    /*
    public boolean isStalled(long now){
        return (now-sentTime > STALLED_TIME);
    }

    /*
    public void sent(){
        sentTime = System.currentTimeMillis();
    }
    */

    public void received(){
        receivedTime = System.currentTimeMillis();
    }
}
