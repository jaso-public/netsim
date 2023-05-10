package jaso.netsim.flowgenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import jaso.netsim.Dispatcher;
import jaso.netsim.Handler;
import jaso.netsim.Host;

public class UniformFlowGenerator implements Handler, FlowGenerator {

	Random rng = new Random();
	
	ArrayList<Host> hosts = new ArrayList<>();
	Set<Integer> hostIds = new HashSet<>();
	
	public UniformFlowGenerator() {
		Dispatcher.dispatcher.register(0, this);	
	}
	
	@Override
    public void addHost(Host host) {
		if(hostIds.contains(host.hostId)) {
			throw new RuntimeException("Already have host:"+host.hostId);
		}
		
		hostIds.add(host.hostId);
		hosts.add(host);
	}
	
	
	@Override
	public void onEvent(long time) {
		int dst = rng.nextInt(hosts.size());
		int src = rng.nextInt(hosts.size()-1);
		if(src >= dst) src++;
		
		src = 0;
		dst = 1;
		
		long flowId = FlowIdGenerator.nextFlowId();
		int destId = hosts.get(dst).hostId;		
		Host srcHost = hosts.get(src);
		srcHost.startFlow(flowId, destId, 10_000);
		
		Dispatcher.dispatcher.register(100, this);
	}
	

}
