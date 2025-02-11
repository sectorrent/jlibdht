package org.sectorrent.jlibdht.rpc.events;

import org.sectorrent.jlibdht.messages.inter.MessageBase;
import org.sectorrent.jlibdht.rpc.events.inter.MessageEvent;
import org.sectorrent.jlibdht.utils.Node;

public class RequestEvent extends MessageEvent {

    private MessageBase response;

    public RequestEvent(MessageBase message){
        super(message);
    }

    public RequestEvent(MessageBase message, Node node){
        super(message, node);
    }

    public boolean hasResponse(){
        return (response != null);
    }

    public MessageBase getResponse(){
        return response;
    }

    public void setResponse(MessageBase message){
        response = message;
    }

    //public MessageBase getResponse(){
    //    return response;
    //}

    /*
    public RequestEvent(MessageBase message, MessageCallback callback){
        this.callback = callback;
    }
    */
}
