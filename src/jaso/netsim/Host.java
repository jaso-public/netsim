package jaso.netsim;

import java.util.HashMap;

import jaso.netsim.queueing.UnboundedQueue;

public class Host implements Sink {
	
	public final String name;
	public final int hostId;
	public final Transmitter transmitter;
	public final UnboundedQueue queue;

	HashMap<Long, Sink> packetHandlers = new HashMap<>();
	
	
	public Host(int hostId, long thinkTime, long picosPerByte, UnboundedQueue queue) {
		this.hostId = hostId;
		this.name = "host-" + hostId;
		this.transmitter = new Transmitter("TX-"+name, thinkTime, picosPerByte);
		this.queue = queue;
		this.transmitter.setQueue(queue);
	}

	void connectCable(Connector connector) {
		transmitter.setConnector(connector);
		connector.setSink(this, 0);
	}
	
	public void startFlow(long flowId, int dst, int size) {		
		System.out.println(Dispatcher.dispatcher.now +" start flow:" + flowId+ " src:"+hostId+" dst:"+dst+" size:"+size);
		
		TcpSource src = new TcpSource(this, flowId, dst, size);
		packetHandlers.put(flowId, src);
	}

	
	public void send(Packet packet) {
		// TODO this is where fair queueing needs to happen.
//		System.out.println(Dispatcher.dispatcher.now + " "+name+" send:"+packet);
		queue.enqueue(packet);
	}
	
	
	@Override
	public void receive(Packet packet) {
		if(packet.dst != hostId) {
			throw new RuntimeException("packet for wrong host");
		}
		
		Sink sink = packetHandlers.get(packet.flowId);
//		System.out.println(Dispatcher.dispatcher.now + " host:"+hostId+" recv:"+packet);
		if(sink == null) {
//			if(packet.synFlag) {
				sink = new TcpDestination(this, packet.flowId);
				packetHandlers.put(packet.flowId, sink);				
//			} else {
//				System.out.println("DDOS - no handler for packet:"+packet);
//				return;
//			}
		}
		
		sink.receive(packet);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	
}
