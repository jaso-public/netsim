package jaso.netsim;

import java.util.TreeMap;

public class TcpDestination implements Sink {

	public final String name;
	private final Host host;
	private final long flowId;
	
	long cumulativeAck;
	private final TreeMap<Long,Integer> received = new TreeMap<>();

	
	// the next packet that will be sent.
	// this isn't part of a TCP packet, but can be useful for debugging
	int nextPacketId = 0; 
	
	
	public TcpDestination(Host host, long flowId) {
		this.name = "TcpDst-"+flowId;
		this.host = host;
		this.flowId = flowId;
	}


	@Override
	public void receive(Packet packet) {	
	    long now = Dispatcher.dispatcher.now;
		System.out.println(now+" "+name+" recv "+packet);
		
		advanceAck(packet);
		
		Packet response = new Packet(packet.dst, packet.src, flowId, 64, 1L, cumulativeAck);
		response.timestampEchoReply = packet.timestamp;
		host.send(response);
	}
	
	void advanceAck(Packet packet) {

	    // this packet is ack'ing the past (already acked)
	    if (packet.seqNum <= cumulativeAck) return;	    	
	    	
	    if (packet.seqNum == cumulativeAck + 1) {
	    	// It's the next expected sequence number.
	    	cumulativeAck = packet.seqNum + packet.size - 1;

	        // Are there any additional received packets that we can now ack?
	    	// move the cumulative ack forward for any packets we may have
	    	// received previously.
	    	while(received.size() > 0) {
	    		long receivedAck = received.firstKey();
	    		 if (cumulativeAck + 1 == receivedAck) {
	    			 cumulativeAck += received.remove(receivedAck);
	    		 } else {
	    			 break;	    			
	    		 }
	    	}
	    	return;
	    }
	    
	    received.put(packet.seqNum, packet.size);
	}

	@Override
	public String getName() {
		return name;
	}	
}
