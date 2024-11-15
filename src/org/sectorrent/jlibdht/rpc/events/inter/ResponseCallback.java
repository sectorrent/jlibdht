package org.sectorrent.jlibdht.rpc.events.inter;

import org.sectorrent.jlibdht.rpc.events.ErrorResponseEvent;
import org.sectorrent.jlibdht.rpc.events.ResponseEvent;
import org.sectorrent.jlibdht.rpc.events.StalledEvent;

public abstract class ResponseCallback {

    public abstract void onResponse(ResponseEvent event);

    public void onErrorResponse(ErrorResponseEvent event){
    }

    //public void onException(MessageException exception){
    //}

    public void onStalled(StalledEvent event){
    }
}
