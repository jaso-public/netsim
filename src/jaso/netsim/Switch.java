package jaso.netsim;

import java.util.HashMap;

public class Switch implements Sink {
	
	final String name;
	Transmitter transmitters[] = null;
	Queue queues[] = null;
	
	// maps a hostId to the port to use to send traffic to that host
	HashMap<Integer,Route> routes = new HashMap<>();

	
	public Switch(String name, int numPorts, long thinkTime, long picosPerByte) {
		this.name = name;
		transmitters = new Transmitter[numPorts];
		for(int i=0 ; i<numPorts ; i++) {			
			transmitters[i] = new Transmitter("TX-"+name+"-"+i, thinkTime, picosPerByte);			
		}
	}
	
	// this is used to set up a simple per port outbound queue
	public void queuePerPort() {
		int numPorts = transmitters.length;
		queues = new Queue[numPorts];
		for(int i=0 ; i<numPorts ; i++) {
			queues[i] = new Queue();
			transmitters[i].setQueue(queues[i]);
		}		
	}
	
	public void connectCable(Connector connector, int port) {
		transmitters[port].setConnector(connector);
		connector.setSink(this, port);
	}
	
	@Override
	public void receive(Packet packet) {		
		Route route = routes.get(packet.dst);
		int port;
		if(route.ports.size() != 1) {
			// only one path to destination
			port = route.ports.get(0);
		} else {
			// multiples paths -- use hashing to choose
			int hash = (int) packet.flowId * 37 + packet.src * 67 + packet.dst * 113;
			int index = hash % route.ports.size();
		    port = route.ports.get(index);
		}
		queues[port].enqueue(packet);	

	}	
	
	HashMap<Sink, Integer> sinkToPortMap = new HashMap<>();
	
	public void sourceRouting(Packet packet) {	
		Sink sink = packet.route.get(packet.nextHop++);
		int port = sinkToPortMap.get(sink);			
		queues[port].enqueue(packet);
	}	

	
	// add a route to a host via a particular port
	// returns false if nothing changed.
	boolean addRoute(int hostId, int cost, int port) {
		Route route = routes.get(hostId);
		if(route == null) {
			route = new Route(hostId, cost, port);
			routes.put(hostId, route);
			return true;
		}
		
		// already have a cheaper route;
		if(route.cost < cost) return false;
		
		if(route.cost == cost) {
			if(route.ports.contains(port)) return false;
			route.ports.add(port);
			return true;
		}
		
		route.cost = cost;
		route.ports.clear();
		route.ports.add(port);
		return true;
	}
	
	boolean computeRoute() {
		boolean changed = false;
		
		for(int i=0 ; i<transmitters.length ; i++) {
			
			Sink other = transmitters[i].connector.peer.sink;
			
			if(other instanceof Host) {
				Host host = (Host) other;
				changed |= addRoute(host.hostId, 1, i);
			} else if(other instanceof Switch) {
				Switch sw = (Switch) other;
				for(Route route : sw.routes.values()) {
					changed |= addRoute(route.hostId, route.cost+1, i);
				}
			} else {
				System.out.println("unknown peer type:"+other.getClass());
			}						
		}

		return changed;		
	}

	public void printRoutes() {
		System.out.println(name);
		for(Route route : routes.values()) {
			System.out.println("    "+route);
		}	
	}
	
	@Override
	public String getName() {
		return name;
	}


}
