package org.sectorrent.jlibdht.kad;

import org.sectorrent.jlibbencode.variables.BencodeObject;
import org.sectorrent.jlibdht.messages.ErrorResponse;
import org.sectorrent.jlibdht.messages.inter.*;
import org.sectorrent.jlibdht.rpc.Call;
import org.sectorrent.jlibdht.rpc.RequestListener;
import org.sectorrent.jlibdht.rpc.ResponseTracker;
import org.sectorrent.jlibdht.rpc.events.ErrorResponseEvent;
import org.sectorrent.jlibdht.rpc.events.RequestEvent;
import org.sectorrent.jlibdht.rpc.events.ResponseEvent;
import org.sectorrent.jlibdht.rpc.events.inter.MessageEvent;
import org.sectorrent.jlibdht.rpc.events.inter.PriorityComparator;
import org.sectorrent.jlibdht.rpc.events.inter.RequestMapping;
import org.sectorrent.jlibdht.rpc.events.inter.ResponseCallback;
import org.sectorrent.jlibdht.utils.ByteWrapper;
import org.sectorrent.jlibdht.utils.Node;
import org.sectorrent.jlibdht.utils.ReflectMethod;
import org.sectorrent.jlibdht.utils.SpamThrottle;
import org.sectorrent.jlibdht.utils.net.AddressUtils;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.sectorrent.jlibdht.messages.inter.MessageBase.TID_KEY;

public class Server {

    public static final int TID_LENGTH = 6;

    protected final KademliaBase kademlia;
    protected Thread handle;
    protected DatagramSocket server;

    private SecureRandom random;
    private boolean allowBogon;
    protected ResponseTracker tracker;
    protected ConcurrentLinkedQueue<DatagramPacket> senderPool;
    protected Map<String, List<ReflectMethod>> requestMapping;
    protected Map<MessageKey, Constructor<?>> messages;
    private SpamThrottle senderThrottle, receiverThrottle;

    public Server(KademliaBase kademlia){
        this.kademlia = kademlia;
        tracker = new ResponseTracker();

        senderPool = new ConcurrentLinkedQueue<>();
        requestMapping = new HashMap<>();
        messages = new HashMap<>();
        senderThrottle = new SpamThrottle();
        receiverThrottle = new SpamThrottle();

        try{
            random = SecureRandom.getInstance("SHA1PRNG");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    public void start(int port)throws SocketException {
        //We add the packet to a pool so that we can read in a different thread so that we don't affect the RPC thread
        //without doing 2 threads we may clog up the RPC server and miss packets
        if(isRunning()){
            throw new IllegalArgumentException("Server has already started.");
        }

        server = new DatagramSocket(port);
        server.setSoTimeout(10);

        handle = new Thread(new Runnable(){
            @Override
            public void run(){
                long lastDecayTime = System.currentTimeMillis();

                while(!server.isClosed()){
                    try{
                        DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                        server.receive(packet);

                        if(packet != null){
                            if(!receiverThrottle.addAndTest(packet.getAddress())){
                                onReceive(packet);
                            }
                        }
                    }catch(IOException e){
                    }

                    if(!senderPool.isEmpty()){
                        DatagramPacket packet = senderPool.poll();

                        if(!senderThrottle.test(packet.getAddress())){
                            try{
                                server.send(packet);
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }

                    long now = System.currentTimeMillis();
                    if(now-lastDecayTime >= 1000){
                        receiverThrottle.decay();
                        senderThrottle.decay();
                        tracker.removeStalled();

                        lastDecayTime = now;
                    }

                    try{
                        Thread.sleep(1);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        handle.start();
    }

    public void stop(){
        if(!isRunning()){
            throw new IllegalArgumentException("Server is not currently running.");
        }
        server.close();
    }

    public void registerRequestListener(RequestListener listener)throws NoSuchFieldException, IllegalAccessException,
            InvocationTargetException {
        Field f = RequestListener.class.getDeclaredField("kademlia");
        f.setAccessible(true);
        f.set(listener, kademlia);

        for(Method method : listener.getClass().getDeclaredMethods()){
            if(method.isAnnotationPresent(RequestMapping.class)){
                Parameter[] parameters = method.getParameters();

                if(parameters.length != 1){
                    continue;
                }

                if(parameters[0].getType().getSuperclass().equals(MessageEvent.class)){
                    method.setAccessible(true);

                    //EventKey key = new EventKey(method.getAnnotation(RequestMapping.class));
                    String key = method.getAnnotation(RequestMapping.class).value();

                    System.out.println("Registered "+method.getName()+" request mapping");

                    if(requestMapping.containsKey(key)){
                        requestMapping.get(key).add(new ReflectMethod(listener, method));
                        requestMapping.get(key).sort(new PriorityComparator());
                        continue;
                    }

                    List<ReflectMethod> m = new ArrayList<>();
                    m.add(new ReflectMethod(listener, method));
                    requestMapping.put(key, m);
                }
            }
        }
    }

    public void registerRequestListener(Class<?> c)throws NoSuchFieldException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        if(!RequestListener.class.isAssignableFrom(c)){//.getSuperclass().equals(RequestListener.class)){
            throw new IllegalArgumentException("Class '"+c.getSimpleName()+"' isn't a assignable from '"+RequestListener.class.getSimpleName()+"'");
        }

        registerRequestListener((RequestListener) c.getDeclaredConstructor().newInstance());
    }

    public void registerMessage(Class<?> c)throws NoSuchMethodException {
        if(!MethodMessageBase.class.isAssignableFrom(c)){
            throw new IllegalArgumentException("Class doesn't extend 'MethodMessageBase' class");
        }

        if(!c.isAnnotationPresent(Message.class)){
            throw new IllegalArgumentException("Class is missing '@Message' annotation");
        }

        messages.put(new MessageKey(c.getAnnotation(Message.class)), c.getDeclaredConstructor(byte[].class));
        System.out.println("Registered "+c.getSimpleName()+" message");
    }

    public boolean isRunning(){
        return (server != null && !server.isClosed());
    }

    public boolean isAllowBogon(){
        return allowBogon;
    }

    public void setAllowBogon(boolean allowBogon){
        this.allowBogon = allowBogon;
    }

    public int getPort(){
        return (server != null) ? server.getLocalPort() : 0;
    }

    protected void onReceive(DatagramPacket packet){
        if(!allowBogon && AddressUtils.isBogon(packet.getAddress(), packet.getPort())){
            return;
        }

        try{
            BencodeObject ben = new BencodeObject(packet.getData());

            if(!ben.containsKey(TID_KEY) || !ben.containsKey(MessageType.TYPE_KEY)){
                return;
            }

            MessageType t = MessageType.fromRPCTypeName(ben.getString(MessageType.TYPE_KEY));

            switch(t){
                case REQ_MSG: {
                        MessageKey k = new MessageKey(ben.getString(t.getRPCTypeName()), t);

                        try{
                            if(!messages.containsKey(k)){
                                throw new MessageException("Method Unknown", 204);
                            }

                            MethodMessageBase m = (MethodMessageBase) messages.get(k).newInstance(ben.getBytes(TID_KEY));
                            m.decode(ben); //ERROR THROW - SEND ERROR MESSAGE
                            m.setOrigin(packet.getAddress(), packet.getPort());

                            if(!requestMapping.containsKey(m.getMethod())){
                                throw new MessageException("Method Unknown", 204);
                            }

                            Node node = new Node(m.getUID(), m.getOrigin());
                            kademlia.routingTable.insert(node);
                            System.out.println("SEEN REQ "+node);

                            RequestEvent event = new RequestEvent(m, node);
                            event.received();

                            for(ReflectMethod r : requestMapping.get(m.getMethod())){
                                r.getMethod().invoke(r.getInstance(), event); //THROW ERROR - SEND ERROR MESSAGE
                            }

                            if(event.isPreventDefault()){
                                return;
                            }

                            if(!event.hasResponse()){
                                throw new MessageException("Method Unknown", 204);
                            }

                            send(event.getResponse());

                        }catch(MessageException e){
                            ErrorResponse response = new ErrorResponse(ben.getBytes(TID_KEY));
                            response.setDestination(packet.getAddress(), packet.getPort());
                            response.setPublic(packet.getAddress(), packet.getPort());
                            response.setCode(e.getCode());
                            response.setDescription(e.getMessage());
                            send(response);
                        }

                        if(!kademlia.getRefreshHandler().isRunning()){
                            kademlia.getRefreshHandler().start();
                        }
                    }
                    break;

                case RSP_MSG: {
                        byte[] tid = ben.getBytes(TID_KEY);
                        Call call = tracker.poll(new ByteWrapper(tid));

                        try{
                            if(call == null){
                                throw new MessageException("Server Error", 202);
                            }

                            MessageKey k = new MessageKey(call.getMessage().getMethod(), t);

                            if(!messages.containsKey(k)){
                                throw new MessageException("Method Unknown", 204);
                            }

                            MethodMessageBase m = (MethodMessageBase) messages.get(k).newInstance(tid);
                            m.decode(ben);
                            m.setOrigin(packet.getAddress(), packet.getPort());

                            if(m.getPublic() != null){
                                kademlia.getRoutingTable().updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                            }

                            if(!call.getMessage().getDestination().equals(m.getOrigin())){
                                throw new MessageException("Generic Error", 201);
                            }

                            ResponseEvent event;

                            if(call.hasNode()){
                                if(!call.getNode().getUID().equals(m.getUID())){
                                    throw new MessageException("Generic Error", 201);
                                }
                                event = new ResponseEvent(m, call.getNode());

                            }else{
                                event = new ResponseEvent(m, new Node(m.getUID(), m.getOrigin()));
                            }

                            event.received();
                            event.setSentTime(call.getSentTime());
                            event.setRequest(call.getMessage());

                            call.getResponseCallback().onResponse(event);

                        }catch(MessageException e){
                            e.printStackTrace();
                        }
                    }
                    break;

                case ERR_MSG: {
                        byte[] tid = ben.getBytes(TID_KEY);
                        Call call = tracker.poll(new ByteWrapper(tid));

                        try{
                            if(call == null){
                                throw new MessageException("Server Error", 202);
                            }

                            ErrorResponse m = new ErrorResponse(tid);
                            m.decode(ben);
                            m.setOrigin(packet.getAddress(), packet.getPort());

                            if(m.getPublic() != null){
                                kademlia.getRoutingTable().updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                            }

                            if(!call.getMessage().getDestination().equals(m.getOrigin())){
                                throw new MessageException("Generic Error", 201);
                            }

                            ErrorResponseEvent event;

                            if(call.hasNode()){
                                event = new ErrorResponseEvent(m, call.getNode());

                            }else{
                                event = new ErrorResponseEvent(m);
                            }

                            event.received();
                            event.setSentTime(call.getSentTime());
                            event.setRequest(call.getMessage());

                            call.getResponseCallback().onErrorResponse(event);

                        }catch(MessageException e){
                            e.printStackTrace();
                        }
                    }
                    break;
            }

        }catch(IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException |
               IOException e){
            e.printStackTrace();
        }
    }

    public void send(MessageBase message)throws IOException {
        if(message.getDestination() == null){
            throw new IllegalArgumentException("Message destination set to null");
        }

        if(!allowBogon && AddressUtils.isBogon(message.getDestination())){
            throw new IllegalArgumentException("Message destination set to bogon");
        }

        if(message.getType() != MessageType.ERR_MSG){
            message.setUID(kademlia.getRoutingTable().getDerivedUID());
        }

        byte[] data = message.encode().toBencode();
        if(!senderThrottle.addAndTest(message.getDestinationAddress())){
            senderPool.add(new DatagramPacket(data, 0, data.length, message.getDestination()));
        }

        //server.send(new DatagramPacket(data, 0, data.length, message.getDestination()));
    }

    public void send(MethodMessageBase message, ResponseCallback callback)throws IOException {
        if(message.getType() != MessageType.REQ_MSG){
            send(message);
            return;
        }

        byte[] tid = generateTransactionID();
        message.setTransactionID(tid);
        tracker.add(new ByteWrapper(tid), new Call(message, callback));
        send(message);
    }

    public void send(MethodMessageBase message, Node node, ResponseCallback callback)throws IOException {
        if(message.getType() != MessageType.REQ_MSG){
            send(message);
            return;
        }

        byte[] tid = generateTransactionID();
        message.setTransactionID(tid);
        tracker.add(new ByteWrapper(tid), new Call(message, node, callback));
        send(message);
    }

    private byte[] generateTransactionID(){
        byte[] tid = new byte[TID_LENGTH];
        random.nextBytes(tid);
        return tid;
    }
}
