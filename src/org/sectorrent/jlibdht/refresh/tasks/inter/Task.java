package org.sectorrent.jlibdht.refresh.tasks.inter;

import org.sectorrent.jlibdht.kad.KademliaBase;
import org.sectorrent.jlibdht.kad.Server;
import org.sectorrent.jlibdht.refresh.RefreshHandler;
import org.sectorrent.jlibdht.routing.inter.RoutingTable;

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
