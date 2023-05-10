package jaso.netsim.queueing;

import jaso.netsim.Packet;
import jaso.netsim.Transmitter;

/**
 * Interface used by the transmitters to store packets to send.
 */
public interface Queue {
    public void enqueue(Packet packet);    
    public Packet dequeue();
    public void register(Transmitter transmitter);
}