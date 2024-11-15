package org.sectorrent.jlibdht.rpc;

import org.sectorrent.jlibdht.routing.inter.RoutingTable;
import org.sectorrent.jlibdht.rpc.events.ErrorResponseEvent;
import org.sectorrent.jlibdht.rpc.events.ResponseEvent;
import org.sectorrent.jlibdht.rpc.events.StalledEvent;
import org.sectorrent.jlibdht.rpc.events.inter.ResponseCallback;

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
