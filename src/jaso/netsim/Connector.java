package jaso.netsim;

public class Connector {

	public final Cable cable;
	
	public Connector peer =null;	
	public Sink sink = null;
	private int port = -1;
	
	public Connector(Cable cable) {
		this.cable = cable;		
	}
	
	public void setPeer(Connector peer) {
		this.peer = peer;
	}
	
	public void setSink(Sink sink, int port) {
		if(this.sink != null) throw new RuntimeException("sink already set");
		this.sink = sink;
		this.port = port;
	}
		
	@Override
	public String toString() {
		return "Connector [sink=" + sink + ", port=" + port + "]";
	}
}
