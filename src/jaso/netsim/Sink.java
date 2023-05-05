package jaso.netsim;

/**
 * a Sink is when connectors deliver their packets.
 * 
 * Note: the Tcp Src/Sink also are sinks for packets.  
 * (this should probably be a different interface for them)
 */
public interface Sink {
	void receive(Packet packet);
	String getName();
}
