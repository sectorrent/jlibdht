package org.sectorrent.jlibdht;

import org.sectorrent.jlibdht.kad.KademliaBase;
import org.sectorrent.jlibdht.messages.FindNodeRequest;
import org.sectorrent.jlibdht.messages.FindNodeResponse;
import org.sectorrent.jlibdht.messages.PingRequest;
import org.sectorrent.jlibdht.messages.PingResponse;
import org.sectorrent.jlibdht.refresh.tasks.BucketRefreshTask;
import org.sectorrent.jlibdht.refresh.tasks.StaleRefreshTask;
import org.sectorrent.jlibdht.routing.BucketTypes;
import org.sectorrent.jlibdht.routing.inter.RoutingTable;
import org.sectorrent.jlibdht.routing.kb.KBucket;
import org.sectorrent.jlibdht.rpc.JoinNodeResponseListener;
import org.sectorrent.jlibdht.rpc.KRequestListener;
import org.sectorrent.jlibdht.utils.Node;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.InetSocketAddress;
import java.util.List;

public class Kademlia extends KademliaBase {

    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    public Kademlia(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public Kademlia(RoutingTable routingTable){
        super(routingTable);

        routingTable.addRestartListener(new RoutingTable.RestartListener(){
            @Override
            public void onRestart(){
                List<Node> closest = getRoutingTable().findClosest(routingTable.getDerivedUID(), KBucket.MAX_BUCKET_SIZE);
                if(closest.isEmpty()){
                    return;
                }

                for(Node n : closest){
                    FindNodeRequest request = new FindNodeRequest();
                    request.setDestination(n.getAddress());
                    request.setTarget(routingTable.getDerivedUID());
                    try{
                        server.send(request, new JoinNodeResponseListener(Kademlia.this));
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        try{
            server.registerRequestListener(new KRequestListener());

            server.registerMessage(PingRequest.class);
            server.registerMessage(PingResponse.class);
            server.registerMessage(FindNodeRequest.class);
            server.registerMessage(FindNodeResponse.class);

            refresh.addOperation(new BucketRefreshTask());
            refresh.addOperation(new StaleRefreshTask());

        }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    @Override
    public void join(int localPort, InetSocketAddress address)throws IOException {
        super.join(localPort, address);

        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(routingTable.getDerivedUID());
        server.send(request, new JoinNodeResponseListener(this));
    }
}
