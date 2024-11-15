package org.sectorrent.jlibdht.messages.inter;

import org.sectorrent.jlibbencode.variables.BencodeObject;
import org.sectorrent.jlibdht.utils.UID;
import org.sectorrent.jlibdht.utils.net.AddressUtils;

public class MethodMessageBase extends MessageBase {

    protected String method;

    public MethodMessageBase(){
        if(getClass().isAnnotationPresent(Message.class)){
            Message message = getClass().getAnnotation(Message.class);
            method = message.method();
            type = message.type();
        }
    }

    public MethodMessageBase(byte[] tid){
        this();
        this.tid = tid;
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();

        switch(type){
            case REQ_MSG:
                ben.put(type.getRPCTypeName(), method);
                ben.put(type.innerKey(), new BencodeObject());
                ben.getBencodeObject(type.innerKey()).put("id", uid.getBytes());
                break;

            case RSP_MSG:
                ben.put(type.innerKey(), new BencodeObject());
                ben.getBencodeObject(type.innerKey()).put("id", uid.getBytes());

                if(publicAddress != null){
                    ben.put("ip", AddressUtils.packAddress(publicAddress)); //PACK MY IP ADDRESS
                }
                break;
        }

        return ben;
    }

    @Override
    public void decode(BencodeObject ben)throws MessageException {
        super.decode(ben);

        if(!ben.getBencodeObject(type.innerKey()).containsKey("id")){
            throw new MessageException("Protocol Error, such as a malformed packet.", 203);
        }

        uid = new UID(ben.getBencodeObject(type.innerKey()).getBytes("id"));

        switch(type){
            case RSP_MSG:
                if(ben.containsKey("ip")){
                    publicAddress = AddressUtils.unpackAddress(ben.getBytes("ip"));
                }
                break;
        }
    }

    public String getMethod(){
        return method;
    }
}
