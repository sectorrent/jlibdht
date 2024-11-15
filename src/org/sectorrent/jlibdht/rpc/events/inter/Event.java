package org.sectorrent.jlibdht.rpc.events.inter;

public class Event {

    protected boolean preventDefault;

    public boolean isPreventDefault(){
        return preventDefault;
    }

    public void preventDefault(){
        preventDefault = true;
    }
}
