package org.octorrent.jlibdht.refresh.tasks;

import org.octorrent.jlibdht.messages.FindNodeRequest;
import org.octorrent.jlibdht.messages.FindNodeResponse;
import org.octorrent.jlibdht.messages.PingRequest;
import org.octorrent.jlibdht.refresh.tasks.inter.Task;
import org.octorrent.jlibdht.routing.kb.KBucket;
import org.octorrent.jlibdht.rpc.PingResponseListener;
import org.octorrent.jlibdht.rpc.events.ErrorResponseEvent;
import org.octorrent.jlibdht.rpc.events.ResponseEvent;
import org.octorrent.jlibdht.rpc.events.StalledEvent;
import org.octorrent.jlibdht.rpc.events.inter.ResponseCallback;
import org.octorrent.jlibdht.utils.Node;
import org.octorrent.jlibdht.utils.UID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BucketRefreshTask extends Task {

    @Override
    public void execute(){
        FindNodeResponseListener listener = new FindNodeResponseListener();
        System.out.println("EXECUTING BUCKET REFRESH");

        for(int i = 1; i < UID.ID_LENGTH*8; i++){
            if(getRoutingTable().getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                UID k = getRoutingTable().getDerivedUID().generateNodeIdByDistance(i);

                List<Node> closest = getRoutingTable().findClosest(k, KBucket.MAX_BUCKET_SIZE);
                if(closest.isEmpty()){
                    continue;
                }

                for(Node n : closest){
                    FindNodeRequest request = new FindNodeRequest();
                    request.setDestination(n.getAddress());
                    request.setTarget(k);

                    try{
                        getServer().send(request, n, listener);

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class FindNodeResponseListener extends ResponseCallback {

        private PingResponseListener listener;
        private List<Node> queries;

        public FindNodeResponseListener(){
            listener = new PingResponseListener(getRoutingTable());
            queries = new ArrayList<>();
        }

        @Override
        public void onResponse(ResponseEvent event){
            event.getNode().setSeen();
            System.out.println("SEEN FN "+event.getNode());
            FindNodeResponse response = (FindNodeResponse) event.getMessage();

            if(response.hasNodes()){
                List<Node> nodes = response.getAllNodes();

                long now = System.currentTimeMillis();
                UID uid = getRoutingTable().getDerivedUID();

                for(int i = nodes.size()-1; i > -1; i--){
                    if(uid.equals(nodes.get(i).getUID()) ||
                            queries.contains(nodes.get(i)) ||
                            getRoutingTable().hasQueried(nodes.get(i), now)){
                        nodes.remove(nodes.get(i));
                    }
                }

                queries.addAll(nodes);

                for(Node n : nodes){
                    if(getRoutingTable().isSecureOnly() && !n.hasSecureID()){
                        System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                        continue;
                    }

                    PingRequest req = new PingRequest();
                    req.setDestination(n.getAddress());
                    try{
                        getServer().send(req, n, listener);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onErrorResponse(ErrorResponseEvent event){
            event.getNode().setSeen();
        }

        @Override
        public void onStalled(StalledEvent event){
            event.getNode().markStale();
        }
    }
}
