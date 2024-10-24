package org.octorrent.jlibdht.routing.kb;

import org.octorrent.jlibdht.utils.Node;

import java.util.Comparator;

public class LSComparetor implements Comparator<Node> {

    @Override
    public int compare(Node a, Node b){
        return (a.equals(b)) ? 0 : (a.getLastSeen() > b.getLastSeen()) ? 1 : -1;
    }
}
