package org.octorrent.jlibdht.rpc.events.inter;

import org.octorrent.jlibdht.rpc.events.ErrorResponseEvent;
import org.octorrent.jlibdht.rpc.events.ResponseEvent;
import org.octorrent.jlibdht.rpc.events.StalledEvent;

public abstract class ResponseCallback {

    public abstract void onResponse(ResponseEvent event);

    public void onErrorResponse(ErrorResponseEvent event){
    }

    //public void onException(MessageException exception){
    //}

    public void onStalled(StalledEvent event){
    }
}
