package org.octorrent.jlibdht.routing;

import org.octorrent.jlibdht.routing.inter.RoutingTable;
import org.octorrent.jlibdht.routing.kb.KRoutingTable;
import org.octorrent.jlibdht.routing.mainline.MRoutingTable;

public enum BucketTypes {

    MAINLINE {
        @Override
        public String value(){
            return "MainLine";
        }
        @Override
        public RoutingTable getRoutingTable(){
            return new MRoutingTable();
        }
    }, KADEMLIA {
        @Override
        public String value(){
            return "Kademlia";
        }
        @Override
        public RoutingTable getRoutingTable(){
            return new KRoutingTable();
        }
    };

    public static BucketTypes fromString(String name){
        for(BucketTypes value : values()){
            if(value.name().equalsIgnoreCase(name)){
                return value;
            }
        }
        throw new IllegalArgumentException("No enum constant "+BucketTypes.class.getName()+"."+name);
    }

    public String value(){
        return null;
    }

    public RoutingTable getRoutingTable(){
        return null;
    }
}
