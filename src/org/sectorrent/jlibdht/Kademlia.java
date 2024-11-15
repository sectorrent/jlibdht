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
import org.sectorrent.jlibdht.rpc.JoinNodeListener;
import org.sectorrent.jlibdht.rpc.KRequestListener;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.InetSocketAddress;

public class Kademlia extends KademliaBase {

    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    public Kademlia(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public Kademlia(RoutingTable routingTable){
        super(routingTable);

        BucketRefreshTask bucketRefreshTask = new BucketRefreshTask();

        routingTable.addRestartListener(new RoutingTable.RestartListener(){
            @Override
            public void onRestart(){
                bucketRefreshTask.execute();
            }
        });

        try{
            server.registerRequestListener(new KRequestListener());

            server.registerMessage(PingRequest.class);
            server.registerMessage(PingResponse.class);
            server.registerMessage(FindNodeRequest.class);
            server.registerMessage(FindNodeResponse.class);

            refresh.addOperation(bucketRefreshTask);
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
        server.send(request, new JoinNodeListener(this));
    }
}
