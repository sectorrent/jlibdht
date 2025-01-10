package org.sectorrent.jlibdht.rpc;

import org.sectorrent.jlibdht.kad.KademliaBase;
import org.sectorrent.jlibdht.messages.FindNodeRequest;
import org.sectorrent.jlibdht.messages.FindNodeResponse;
import org.sectorrent.jlibdht.messages.PingRequest;
import org.sectorrent.jlibdht.routing.kb.KComparator;
import org.sectorrent.jlibdht.rpc.events.ErrorResponseEvent;
import org.sectorrent.jlibdht.rpc.events.ResponseEvent;
import org.sectorrent.jlibdht.rpc.events.StalledEvent;
import org.sectorrent.jlibdht.rpc.events.inter.ResponseCallback;
import org.sectorrent.jlibdht.utils.Node;
import org.sectorrent.jlibdht.utils.UID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class JoinNodeListener extends ResponseCallback {

    private KademliaBase kademlia;
    private List<Node> queries;
    private boolean stop;

    public JoinNodeListener(KademliaBase kademlia){
        this.kademlia = kademlia;
        queries = new ArrayList<>();
    }

    @Override
    public void onResponse(ResponseEvent event){
        kademlia.getRoutingTable().insert(event.getNode());
        System.out.println("JOINED "+event.getNode());

        FindNodeResponse response = (FindNodeResponse) event.getMessage();

        if(response.hasNodes()){
            List<Node> nodes = response.getAllNodes();

            long now = System.currentTimeMillis();
            UID uid = kademlia.getRoutingTable().getDerivedUID();
            int distance = uid.getDistance(event.getNode().getUID());

            TreeSet<Node> sortedSet = new TreeSet<>(new KComparator(uid));
            sortedSet.addAll(nodes);

            for(int i = nodes.size()-1; i > -1; i--){
                if(uid.equals(nodes.get(i).getUID()) ||
                        queries.contains(nodes.get(i)) ||
                        kademlia.getRoutingTable().hasQueried(nodes.get(i), now)){
                    System.out.println("SKIPPING "+now+"  "+nodes.get(i).getLastSeen()+"  "+nodes.get(i));
                    nodes.remove(nodes.get(i));
                }
            }

            queries.addAll(nodes);

            if(stop || distance < uid.getDistance(sortedSet.first().getUID())){
                stop = true;

                PingResponseListener listener = new PingResponseListener(kademlia.getRoutingTable());

                for(Node n : nodes){
                    PingRequest request = new PingRequest();
                    request.setDestination(n.getAddress());

                    try{
                        kademlia.getServer().send(request, n, listener);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }

                return;
            }

            for(Node n : nodes){
                FindNodeRequest request = new FindNodeRequest();
                request.setDestination(n.getAddress());
                request.setTarget(uid);

                try{
                    kademlia.getServer().send(request, this);
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
