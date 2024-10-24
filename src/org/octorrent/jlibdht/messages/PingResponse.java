package org.octorrent.jlibdht.messages;

import org.octorrent.jlibdht.messages.inter.Message;
import org.octorrent.jlibdht.messages.inter.MessageBase;
import org.octorrent.jlibdht.messages.inter.MessageType;
import org.octorrent.jlibdht.messages.inter.MethodMessageBase;

@Message(method = "ping", type = MessageType.RSP_MSG)
public class PingResponse extends MethodMessageBase {

    public PingResponse(byte[] tid){
        super(tid);
    }
}
