package org.sectorrent.jlibdht.rpc;

import org.sectorrent.jlibdht.rpc.events.StalledEvent;
import org.sectorrent.jlibdht.utils.ByteWrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ResponseTracker {

    public static final int MAX_ACTIVE_CALLS = 512;

    public static final long STALLED_TIME = 60000;
    private final LinkedHashMap<ByteWrapper, Call> calls;

    public ResponseTracker(){
        calls = new LinkedHashMap<>(MAX_ACTIVE_CALLS);
    }

    public synchronized void add(ByteWrapper tid, Call call){
        calls.put(tid, call);
    }

    public synchronized Call get(ByteWrapper tid){
        return calls.get(tid);
    }

    public synchronized boolean contains(ByteWrapper tid){
        return calls.containsKey(tid);
    }

    public synchronized void remove(ByteWrapper tid){
        calls.remove(tid);
    }

    public synchronized Call poll(ByteWrapper tid){
        Call call = calls.get(tid);
        calls.remove(tid);
        return call;
    }

    public synchronized void removeStalled(){
        long now = System.currentTimeMillis();

        List<ByteWrapper> stalled = new ArrayList<>();

        for(ByteWrapper tid : calls.keySet()){
            if(!calls.get(tid).isStalled(now)){
                break;
            }

            stalled.add(tid);
        }

        for(ByteWrapper tid : stalled){
            Call call = calls.get(tid);
            calls.remove(tid);
            System.err.println("STALLED "+((call.hasNode()) ? call.getNode() : ""));

            StalledEvent event = new StalledEvent(call.getMessage());
            event.setSentTime(call.getSentTime());

            if(call.hasNode()){
                event.setNode(call.getNode());
            }

            call.getResponseCallback().onStalled(event);
        }
    }
}
