package jaso.netsim;

import jaso.netsim.flowgenerator.FlowGenerator;
import jaso.netsim.flowgenerator.UniformFlowGenerator;
import jaso.netsim.queueing.UnboundedQueue;

public class SimulatorFatTree {

	public static void main(String[] args) {
		
		int numPorts = 4;
		
		long switchThinkTime = 100;
		long switchBitRate = Conversion.gbpsTo_bps(40);
		
		long hostThinkTime = 100;
		long hostBitRate = Conversion.gbpsTo_bps(10);
				
		long cableDelayPicos = Conversion.microsToPicos(12);		
		
		
		long switchPicosPerByte = Conversion.picoPerSecond / Conversion.bitsToBytes(switchBitRate);		
		long hostPicosPerByte = Conversion.picoPerSecond / Conversion.bitsToBytes(hostBitRate);
		
	
		System.out.println("switchPicosPerByte:"+switchPicosPerByte);
		System.out.println("hostPicosPerByte:"+hostPicosPerByte);
		
		
		int nOverTwo = numPorts / 2;
		int numPods = numPorts;

		
		int numCores = nOverTwo * nOverTwo;
		int numAggs = numPods * nOverTwo;
		int numTors = numAggs;
		int numHosts = numTors * nOverTwo;
		
		System.out.println("numPorts:"+numPorts+" nOverTwo:"+nOverTwo+" numPods:"+numPods+" numCores:"+numCores+" numAggs:"+numAggs+" numTors:"+numTors+" numHosts:"+numHosts);
		
		
		Switch cores[] = new Switch[numCores];
		Switch aggs[] = new Switch[numAggs];
		Switch tors[] = new Switch[numTors];
		Host hosts[] = new Host[numHosts];
		
		for(int i=0; i< numCores; i++) {
			cores[i] = new Switch("cores-"+i, numPorts, switchThinkTime, switchPicosPerByte);			
		}

		for(int i=0; i< numAggs; i++) {
			aggs[i] = new Switch("aggs-"+i, numPorts, switchThinkTime, switchPicosPerByte);		
		}
		
		for(int i=0; i< numTors; i++) {
			tors[i] = new Switch("tor-"+i, numPorts, switchThinkTime, switchPicosPerByte);			
		}

		FlowGenerator flowGenerator = new UniformFlowGenerator();
		
		Host host1 = new Host(1, hostThinkTime, hostPicosPerByte, new UnboundedQueue());
		flowGenerator.addHost(host1);
		Host host2 = new Host(2, hostThinkTime, hostPicosPerByte, new UnboundedQueue());
		flowGenerator.addHost(host2);
		
		Cable cablex = new Cable(cableDelayPicos);
		host1.connectCable(cablex.north);
		host2.connectCable(cablex.south);
		
		
		Dispatcher.dispatcher.processEvents(1_000_000_000_000L);	

		if(hosts[0]==null) return;


		// create the hosts and cable them up to their proper tor.
		for(int hostId=0; hostId<numHosts; hostId++) {
			hosts[hostId] = new Host(hostId, hostThinkTime, hostPicosPerByte, new UnboundedQueue());
			flowGenerator.addHost(hosts[hostId]);
			
			int tor = hostId / nOverTwo;
			int torPort = hostId % nOverTwo;
			
			Cable cable = new Cable(cableDelayPicos);
			tors[tor].connectCable(cable.north, torPort);
			hosts[hostId].connectCable(cable.south);
		}
		
		// cable the core switches to the agg switches
		for(int cableId=0; cableId<numAggs*nOverTwo ; cableId++) {
			int agg = cableId/nOverTwo;
			int aggPort = cableId % nOverTwo ; 
			
			int corePort = cableId / numCores;
			int core = cableId % numCores;
			
			Cable cable = new Cable(cableDelayPicos);
			cores[core].connectCable(cable.north, corePort);
			aggs[agg].connectCable(cable.south, aggPort+ nOverTwo);
		}
		
		// cable the tor switches to the agg switches in each pod.
		for(int cableId=0; cableId<numAggs*nOverTwo ; cableId++) {
			int tor = cableId/nOverTwo;
			int torPort = cableId % nOverTwo;
			
			int pod = cableId / (nOverTwo * nOverTwo);
			
			int agg = pod * nOverTwo + torPort;
			int aggPort = tor - (pod * nOverTwo);
	
			// System.out.println("cableId:"+cableId+" agg:"+agg+" aggPort:"+aggPort+" tor:"+tor+" torPort:"+torPort);			
			Cable cable = new Cable(cableDelayPicos);
			aggs[agg].connectCable(cable.north, aggPort);
			tors[tor].connectCable(cable.south, torPort + nOverTwo);
		}
		

		// compute the routes for all the switches
		boolean changed;
		do {
			changed = false;
			
			for(int i=0; i< numCores; i++) {
				changed |= cores[i].computeRoute();			
			}

			for(int i=0; i< numAggs; i++) {
				changed |= aggs[i].computeRoute();					
			}
			
			for(int i=0; i< numTors; i++) {
				changed |= tors[i].computeRoute();				
			}
		} while(changed);
		
		
		for(int i=0; i< numCores; i++) {
			cores[i].queuePerPort();		
		}

		for(int i=0; i< numAggs; i++) {
			aggs[i].queuePerPort();	
		}
		
		for(int i=0; i< numTors; i++) {
			tors[i].queuePerPort();			
		}

		
//		System.out.println("Start Routes --------------------------------------------");
//		for(int i=0; i< numCores; i++) {
//			cores[i].printRoutes();			
//		}
//
//		for(int i=0; i< numAggs; i++) {
//			aggs[i].printRoutes();					
//		}
//		
//		for(int i=0; i< numTors; i++) {
//			tors[i].printRoutes();				
//		}
//		System.out.println("End Routes --------------------------------------------");
		
		
		Dispatcher.dispatcher.processEvents(1_000_000_000_000L);	
		
		System.out.println("Returned from dispatcher");

	}

}
