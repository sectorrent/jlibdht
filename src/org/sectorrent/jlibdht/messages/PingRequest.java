package org.sectorrent.jlibdht.messages;

import org.sectorrent.jlibdht.messages.inter.*;
import org.sectorrent.jlibdht.messages.inter.Message;
import org.sectorrent.jlibdht.messages.inter.MessageType;
import org.sectorrent.jlibdht.messages.inter.MethodMessageBase;

@Message(method = "ping", type = MessageType.REQ_MSG)
public class PingRequest extends MethodMessageBase {

    public PingRequest(){
    }

    public PingRequest(byte[] tid){
        super(tid);
    }
}
