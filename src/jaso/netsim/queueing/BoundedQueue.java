package jaso.netsim.queueing;

import java.util.ArrayList;
import java.util.LinkedList;

import jaso.netsim.Packet;
import jaso.netsim.Transmitter;

public class BoundedQueue implements Queue {
    
    private final int maxQueueSize;
    private int currentQueueSize =0;
    
    private final ArrayList<Transmitter> transmitters = new ArrayList<>();
    private final LinkedList<Packet> queue = new LinkedList<>();
    
    
    public BoundedQueue(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public void enqueue(Packet packet) {
        if(currentQueueSize + packet.size > maxQueueSize) {
            System.out.println("Drop:"+packet);
            return;
        }
        
        currentQueueSize += packet.size;
        boolean wasEmpty = queue.isEmpty();
        queue.add(packet);
        if(wasEmpty) {
            for(Transmitter t :transmitters) {
                t.wakeUp();
            }
        }
    }
    
    public Packet dequeue() {
        if(queue.isEmpty()) return null;
        Packet packet = queue.poll();
        currentQueueSize -= packet.size;
        return packet;
    }
    
    
    void register(Transmitter transmitter) {
        transmitters.add(transmitter);
    }

}