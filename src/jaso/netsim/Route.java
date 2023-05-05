package jaso.netsim;

import java.util.ArrayList;

public class Route {
	int hostId;
	int cost;
	ArrayList<Integer> ports = new ArrayList<>();
	
	
	public Route(int hostId, int cost, int port) {
		this.hostId = hostId;
		this.cost = cost;
		ports.add(port);
	}


	@Override
	public String toString() {
		return "Route [hostId=" + hostId + ", cost=" + cost + ", ports=" + ports + "]";
	}	
}
