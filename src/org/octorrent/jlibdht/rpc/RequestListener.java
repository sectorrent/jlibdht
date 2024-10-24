package org.octorrent.jlibdht.rpc;

import org.octorrent.jlibdht.kad.KademliaBase;
import org.octorrent.jlibdht.kad.Server;
import org.octorrent.jlibdht.routing.inter.RoutingTable;

public class RequestListener {

    private KademliaBase kademlia;

    public Server getServer(){
        return kademlia.getServer();
    }

    public RoutingTable getRoutingTable(){
        return kademlia.getRoutingTable();
    }
}
