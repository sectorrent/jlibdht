package org.sectorrent.jlibdht.utils;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SpamThrottle {

    private static final int BURST = 10, PER_SECOND = 2;
    private Map<InetAddress, Integer> hitCounter;
    private AtomicLong lastDecayTime;

    public SpamThrottle(){
        hitCounter = new ConcurrentHashMap<>();
        lastDecayTime = new AtomicLong(System.currentTimeMillis());
    }

    public boolean addAndTest(InetAddress address){
        return (saturatingAdd(address) >= BURST);
    }

    public void remove(InetAddress address){
        hitCounter.remove(address);
    }

    public boolean test(InetAddress address){
        return hitCounter.getOrDefault(address, 0) >= BURST;
    }

    public int calculateDelayAndAdd(InetAddress address){
        int counter = hitCounter.compute(address, (key, old) -> old == null ? 1 : old+1);
        int diff = counter - BURST;
        return Math.max(diff, 0)*1000/PER_SECOND;
    }

    public void saturatingDec(InetAddress address){
        hitCounter.compute(address, (key, old) -> old == null || old == 1 ? null : old-1);
    }

    public int saturatingAdd(InetAddress address){
        return hitCounter.compute(address, (key, old) -> old == null ? 1 : Math.min(old+1, BURST));
    }

    public void decay(){
        long now = System.currentTimeMillis();
        long last = lastDecayTime.get();
        long deltaT = TimeUnit.MILLISECONDS.toSeconds(now-last);

        if(deltaT < 1){
            return;
        }

        if(!lastDecayTime.compareAndSet(last, last+deltaT*1000)){
            return;
        }

        int deltaC = (int) (deltaT*PER_SECOND);

        hitCounter.entrySet().removeIf(entry -> entry.getValue() <= deltaC);
        hitCounter.replaceAll((k, v) -> v - deltaC);
    }
}
