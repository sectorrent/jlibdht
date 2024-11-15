package org.sectorrent.jlibdht.rpc;

import org.sectorrent.jlibdht.kad.KademliaBase;
import org.sectorrent.jlibdht.kad.Server;
import org.sectorrent.jlibdht.routing.inter.RoutingTable;

public class RequestListener {

    private KademliaBase kademlia;

    public Server getServer(){
        return kademlia.getServer();
    }

    public RoutingTable getRoutingTable(){
        return kademlia.getRoutingTable();
    }
}
