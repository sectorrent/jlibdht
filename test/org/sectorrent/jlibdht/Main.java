package org.sectorrent.jlibdht;

import java.net.InetAddress;

public class Main {

    public static void main(String[] args){
        try{
            Kademlia k = new Kademlia("Kademlia");
            k.getRoutingTable().setSecureOnly(false);
            k.getServer().setAllowBogon(true);
            //k.join(6881, InetAddress.getByName("router.sectorrent.com"), 6881);
            k.bind(8070);

            Kademlia k2 = new Kademlia();
            k2.getRoutingTable().setSecureOnly(false);
            k2.getServer().setAllowBogon(true);
            k2.join(8075, InetAddress.getLoopbackAddress(), 8070);

            while(true){
                Thread.sleep(10000);
                System.out.println("CONSENSUS-1: "+k.getRoutingTable().getDerivedUID()+"  "+k.getRoutingTable().getConsensusExternalAddress().getHostAddress()+"  "+k.getRoutingTable().getAllNodes().size());
                System.out.println("CONSENSUS-2: "+k2.getRoutingTable().getDerivedUID()+"  "+k2.getRoutingTable().getConsensusExternalAddress().getHostAddress()+"  "+k2.getRoutingTable().getAllNodes().size());
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
