package jaso.netsim;

import java.util.ArrayList;
import java.util.LinkedList;

public class Queue {
	
	ArrayList<Transmitter> transmitters = new ArrayList<>();
	LinkedList<Packet> queue = new LinkedList<>();
	
	void enqueue(Packet packet) {
		boolean wasEmpty = queue.isEmpty();
		queue.add(packet);
		if(wasEmpty) {
			for(Transmitter t :transmitters) {
				t.wakeUp();
			}
		}
	}
	
	Packet dequeue() {
		if(queue.isEmpty()) return null;
		return queue.poll();
	}
	
	
	void register(Transmitter transmitter) {
		transmitters.add(transmitter);
	}

}
