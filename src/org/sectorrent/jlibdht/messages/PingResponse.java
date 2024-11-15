package org.sectorrent.jlibdht.messages;

import org.sectorrent.jlibdht.messages.inter.Message;
import org.sectorrent.jlibdht.messages.inter.MessageType;
import org.sectorrent.jlibdht.messages.inter.MethodMessageBase;

@Message(method = "ping", type = MessageType.RSP_MSG)
public class PingResponse extends MethodMessageBase {

    public PingResponse(byte[] tid){
        super(tid);
    }
}
