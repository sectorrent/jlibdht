package org.sectorrent.jlibdht.rpc;

import org.sectorrent.jlibdht.messages.FindNodeRequest;
import org.sectorrent.jlibdht.messages.FindNodeResponse;
import org.sectorrent.jlibdht.messages.PingResponse;
import org.sectorrent.jlibdht.routing.kb.KBucket;
import org.sectorrent.jlibdht.rpc.events.RequestEvent;
import org.sectorrent.jlibdht.rpc.events.inter.RequestMapping;
import org.sectorrent.jlibdht.utils.Node;

import java.util.List;

public class KRequestListener extends RequestListener {

    @RequestMapping("ping")
    public void onPingRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        PingResponse response = new PingResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());
        event.setResponse(response);
    }

    @RequestMapping("find_node")
    public void onFindNodeRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        FindNodeRequest request = (FindNodeRequest) event.getMessage();

        List<Node> nodes = getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE);
        nodes.remove(event.getNode());

        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());
        response.addNodes(nodes);
        event.setResponse(response);
    }
}
