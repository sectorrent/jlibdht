package org.sectorrent.jlibdht.routing.kb;

import org.sectorrent.jlibdht.utils.Node;

import java.util.ArrayList;
import java.util.List;

public class KBucket {

    private List<Node> nodes, cache;
    public static final int MAX_BUCKET_SIZE = 5, MAX_STALE_COUNT = 1;

    public KBucket(){
        nodes = new ArrayList<>(MAX_BUCKET_SIZE);
        cache = new ArrayList<>(MAX_BUCKET_SIZE);
    }

    public synchronized void insert(Node n){
        if(nodes.contains(n)){
            nodes.get(nodes.indexOf(n)).setSeen();
            nodes.sort(new LSComparetor());

        }else if(nodes.size() >= MAX_BUCKET_SIZE){
            if(cache.contains(n)){
                cache.get(cache.indexOf(n)).setSeen();

            }else if(cache.size() >= MAX_BUCKET_SIZE){
                Node stale = null;
                for(Node s : cache){
                    if(s.getStale() >= MAX_STALE_COUNT){
                        if(stale == null || s.getStale() > stale.getStale()){
                            stale = s;
                        }
                    }
                }

                if(stale != null){
                    cache.remove(stale);
                    cache.add(n);
                }

            }else{
                cache.add(n);
            }
        }else{
            nodes.add(n);
            nodes.sort(new LSComparetor());
        }
    }

    public synchronized boolean containsIP(Node n){
        return nodes.contains(n) || cache.contains(n);
    }

    public synchronized boolean containsUID(Node n){
        return nodes.stream().anyMatch(c -> c.verify(n)) || cache.stream().anyMatch(c -> c.verify(n));
    }

    //IN THE CASE OF RESPONSE NODES - SAME NODE BUT NOT SAME VARIABLE
    public synchronized boolean hasQueried(Node n, long now){
        for(Node c : nodes){
            if(c.equals(n)){
                return c.hasQueried(now);
            }
        }
        return false;
    }

    public List<Node> getAllNodes(){
        //nodes.sort(new LSComparetor());
        return nodes;
    }

    /*
    public List<Node> getAllNodesIncludingCache(){
        List<Node> q = new ArrayList<>();
        q.addAll(this.nodes);
        q.addAll(cache);
        return q;
    }
    */

    public List<Node> getUnQueriedNodes(long now){
        List<Node> q = new ArrayList<>();

        for(Node n : nodes){
            if(!n.hasQueried(now)){
                q.add(n);
            }
        }

        return q;
    }


    public int size(){
        return nodes.size();
    }

    public int csize(){
        return cache.size();
    }
}
