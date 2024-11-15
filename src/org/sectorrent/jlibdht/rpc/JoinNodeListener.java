package org.sectorrent.jlibdht.rpc;

import org.sectorrent.jlibdht.kad.KademliaBase;
import org.sectorrent.jlibdht.messages.FindNodeResponse;
import org.sectorrent.jlibdht.messages.PingRequest;
import org.sectorrent.jlibdht.rpc.events.ErrorResponseEvent;
import org.sectorrent.jlibdht.rpc.events.ResponseEvent;
import org.sectorrent.jlibdht.rpc.events.StalledEvent;
import org.sectorrent.jlibdht.rpc.events.inter.ResponseCallback;
import org.sectorrent.jlibdht.utils.Node;
import org.sectorrent.jlibdht.utils.UID;

import java.io.IOException;
import java.util.List;

public class JoinNodeListener extends ResponseCallback {

    private KademliaBase kademlia;

    public JoinNodeListener(KademliaBase kademlia){
        this.kademlia = kademlia;
    }

    @Override
    public void onResponse(ResponseEvent event){
        kademlia.getRoutingTable().insert(event.getNode());
        System.out.println("JOINED "+event.getNode());

        FindNodeResponse response = (FindNodeResponse) event.getMessage();

        if(response.hasNodes()){
            List<Node> nodes = response.getAllNodes();

            PingResponseListener listener = new PingResponseListener(kademlia.getRoutingTable());

            long now = System.currentTimeMillis();
            UID uid = kademlia.getRoutingTable().getDerivedUID();

            for(Node n : nodes){
                if(uid.equals(n.getUID()) ||
                        (kademlia.getRoutingTable().isSecureOnly() && !n.hasSecureID()) ||
                        n.hasQueried(now)){
                    System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                    continue;
                }

                PingRequest req = new PingRequest();
                req.setDestination(n.getAddress());
                try{
                    kademlia.getServer().send(req, n, listener);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        if(!kademlia.getRefreshHandler().isRunning()){
            kademlia.getRefreshHandler().start();
        }
    }

    @Override
    public void onErrorResponse(ErrorResponseEvent event){

    }

    @Override
    public void onStalled(StalledEvent event){

    }
}
