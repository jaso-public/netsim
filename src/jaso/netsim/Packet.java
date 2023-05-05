package jaso.netsim;

import java.util.ArrayList;

public class Packet {
	
	
	// these get set in the constructor
	public final int src;
	public final int dst;	
	public final long flowId;
	public final int size;
	public final long seqNum;
	public final long ackSeqNum;  
		
	public final long timestamp;

	public int window = 0;
	public long timestampEchoReply = 0;
	
	
	public Packet(int src, int dst, long flowId, int size, long seqNum, long ackSeqNum) {
		this.src = src;
		this.dst = dst;
		this.flowId = flowId;
		this.size = size;
		this.seqNum = seqNum;
		this.ackSeqNum = ackSeqNum;
		
		this.timestamp = Dispatcher.dispatcher.now;
	}
	
	public ArrayList<Sink> route = null;
	public int nextHop;

	void setRoute(ArrayList<Sink> route) {
		this.route = route;
		this.nextHop = 0;
	}

	@Override
	public String toString() {
		return "Packet [src=" + src + ", dst=" + dst + ", flowId=" + flowId + ", size=" + size + ", seqNum=" + seqNum
				+ ", ackSeqNum=" + ackSeqNum + ", timestamp=" + timestamp
				+ ", timestampEchoReply=" + timestampEchoReply + "]";
	}
	
	

}
