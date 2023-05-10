package jaso.netsim.queueing;

import java.util.ArrayList;
import java.util.LinkedList;

import jaso.netsim.Packet;
import jaso.netsim.Transmitter;

public class UnboundedQueue implements Queue {
	
	ArrayList<Transmitter> transmitters = new ArrayList<>();
	LinkedList<Packet> queue = new LinkedList<>();
	
	public void enqueue(Packet packet) {
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
		return queue.poll();
	}
	
	
	void register(Transmitter transmitter) {
		transmitters.add(transmitter);
	}

}
