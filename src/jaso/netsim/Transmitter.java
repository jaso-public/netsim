package jaso.netsim;

import jaso.netsim.queueing.Queue;

public class Transmitter implements Handler {
	
	public final String name;
	public final long thinkTime;
	public final long picosPerByte;
	
	enum State {Idle, Thinking, Sending} 
	
	State state = State.Idle;
	Queue queue = null;
	Connector connector = null;


	public Transmitter(String name, long thinkTime, long picosPerByte) {
		this.name = name;
		this.thinkTime = thinkTime;
		this.picosPerByte = picosPerByte;
		state = State.Idle;
		System.out.println("Transmitter:"+name+" thinkTime:"+thinkTime+" picosPerByte:"+picosPerByte);
	}	

	public void setQueue(Queue queue) {
		if(this.queue != null) throw new RuntimeException("queue already set");
		this.queue = queue;
		this.queue.register(this);
	}

	public void setConnector(Connector connector) {
		if(this.connector != null) throw new RuntimeException("transmitter already connected");		
		this.connector = connector;
	}

	public void wakeUp() {
		
		if(state == State.Idle) {
			// System.out.println(Dispatcher.dispatcher.now+" "+name+" being awakened");			
			Dispatcher.dispatcher.register(thinkTime, this);
		} else {
			// System.out.println(Dispatcher.dispatcher.now+" "+name+" not idle");
		}
	}

	@Override
	public void onEvent(long time) {
		// System.out.println(Dispatcher.dispatcher.now+" "+name+" onEvent");
		
		
		if(state == State.Sending) {
			Dispatcher.dispatcher.register(thinkTime, this);
			state = State.Thinking;
			return;
		}
		
		Packet packet = queue.dequeue();
		if(packet == null) {
			// System.out.println(Dispatcher.dispatcher.now+" "+name+" going idle");			
			state = State.Idle;
			return;
		}
		
		// System.out.println(Dispatcher.dispatcher.now+" "+name+" send packet:"+packet);			

		Handler handler = new Handler() {
			public void onEvent(long time) {
				//System.out.println(Dispatcher.dispatcher.now+" "+name+" delivering packet to sink");			
				connector.peer.sink.receive(packet);						
			}
		};
		
		Dispatcher.dispatcher.register(connector.cable.delayPicos, handler); 
		Dispatcher.dispatcher.register(picosPerByte * packet.size, this);
	}
}
