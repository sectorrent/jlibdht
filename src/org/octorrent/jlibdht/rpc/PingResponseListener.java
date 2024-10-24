package org.octorrent.jlibdht.rpc;

import org.octorrent.jlibdht.routing.inter.RoutingTable;
import org.octorrent.jlibdht.rpc.events.ErrorResponseEvent;
import org.octorrent.jlibdht.rpc.events.ResponseEvent;
import org.octorrent.jlibdht.rpc.events.StalledEvent;
import org.octorrent.jlibdht.rpc.events.inter.ResponseCallback;

public class PingResponseListener extends ResponseCallback {

    private RoutingTable routingTable;

    public PingResponseListener(RoutingTable routingTable){
        this.routingTable = routingTable;
    }

    @Override
    public void onResponse(ResponseEvent event){
        routingTable.insert(event.getNode());
        System.out.println("SEEN "+event.getNode());
    }

    @Override
    public void onErrorResponse(ErrorResponseEvent event){

    }

    @Override
    public void onStalled(StalledEvent event){
        if(event.hasNode()){
            event.getNode().markStale();
        }
    }
}
