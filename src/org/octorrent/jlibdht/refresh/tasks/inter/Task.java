package org.octorrent.jlibdht.refresh.tasks.inter;

import org.octorrent.jlibdht.kad.KademliaBase;
import org.octorrent.jlibdht.kad.Server;
import org.octorrent.jlibdht.refresh.RefreshHandler;
import org.octorrent.jlibdht.routing.inter.RoutingTable;

public abstract class Task {

    private KademliaBase kademlia;

    public Server getServer(){
        return kademlia.getServer();
    }

    public RoutingTable getRoutingTable(){
        return kademlia.getRoutingTable();
    }

    public RefreshHandler getRefreshHandler(){
        return kademlia.getRefreshHandler();
    }

    public abstract void execute();
}
