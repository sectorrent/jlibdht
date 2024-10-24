package org.octorrent.jlibdht.messages;

import org.octorrent.jlibbencode.variables.BencodeObject;
import org.octorrent.jlibdht.messages.inter.Message;
import org.octorrent.jlibdht.messages.inter.MessageException;
import org.octorrent.jlibdht.messages.inter.MessageType;
import org.octorrent.jlibdht.messages.inter.MethodMessageBase;
import org.octorrent.jlibdht.utils.UID;

@Message(method = "find_node", type = MessageType.REQ_MSG)
public class FindNodeRequest extends MethodMessageBase {

    private UID target;

    public FindNodeRequest(){
    }

    public FindNodeRequest(byte[] tid){
        super(tid);
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();
        ben.getBencodeObject(type.innerKey()).put("target", target.getBytes());
        return ben;
    }

    @Override
    public void decode(BencodeObject ben)throws MessageException {
        super.decode(ben);
        if(!ben.getBencodeObject(type.innerKey()).containsKey("target")){
            throw new MessageException("Protocol Error, such as a malformed packet.", 203);
        }

        target = new UID(ben.getBencodeObject(type.innerKey()).getBytes("target"));
    }

    public UID getTarget(){
        return target;
    }

    public void setTarget(UID target){
        this.target = target;
    }
}
