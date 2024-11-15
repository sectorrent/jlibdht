package org.sectorrent.jlibdht.messages;

import org.sectorrent.jlibbencode.variables.BencodeObject;
import org.sectorrent.jlibdht.messages.inter.Message;
import org.sectorrent.jlibdht.messages.inter.MessageException;
import org.sectorrent.jlibdht.messages.inter.MessageType;
import org.sectorrent.jlibdht.messages.inter.MethodMessageBase;
import org.sectorrent.jlibdht.utils.UID;

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
