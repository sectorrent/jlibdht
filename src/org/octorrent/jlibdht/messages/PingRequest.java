package org.octorrent.jlibdht.messages;

import org.octorrent.jlibdht.messages.inter.*;

@Message(method = "ping", type = MessageType.REQ_MSG)
public class PingRequest extends MethodMessageBase {

    public PingRequest(){
    }

    public PingRequest(byte[] tid){
        super(tid);
    }
}
