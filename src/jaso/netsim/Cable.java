package jaso.netsim;

public class Cable {
	
	public final long delayPicos;
	public final Connector north;
	public final Connector south;

	public Cable(long delayPicos) {
		this.delayPicos = delayPicos;
		
		north = new Connector(this);
		south = new Connector(this);
		north.setPeer(south);
		south.setPeer(north);
	}

	@Override
	public String toString() {
		return "Cable [delayPicos=" + delayPicos + ", north=" + north + ", south=" + south + "]";
	}
}
