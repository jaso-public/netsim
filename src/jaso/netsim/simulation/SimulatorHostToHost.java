package jaso.netsim.simulation;

import jaso.netsim.Cable;
import jaso.netsim.Conversion;
import jaso.netsim.Dispatcher;
import jaso.netsim.Host;
import jaso.netsim.flowgenerator.FlowGenerator;
import jaso.netsim.flowgenerator.SingleFlowGenerator;
import jaso.netsim.queueing.BoundedQueue;

/**
 * A simple simulation that directly connects two hosts together 
 * and sends a single flow from one host to the other.
 */
public class SimulatorHostToHost {

	public static void main(String[] args) {
		
	    // The key constants for this simulation 
	    long hostQueueSize = Conversion.megabytesToBytes(8);
		long hostThinkTime = Conversion.nanosToPicos(3);
		long hostBitRate = Conversion.gbpsTo_bps(10);				
		long cableDelayPicos = Conversion.microsToPicos(24) / 2;
		
		// a flow generator that creates a single flow -- good for testing
		FlowGenerator flowGenerator = new SingleFlowGenerator(10000);

		// derived from the hostBitRate.
		long hostPicosPerByte = Conversion.bpsToPicosPerByte(hostBitRate);        

		// create two hosts and tell the flow generator about them.
		Host host1 = new Host(1, hostThinkTime, hostPicosPerByte, new BoundedQueue(hostQueueSize));
		flowGenerator.addHost(host1);
		Host host2 = new Host(2, hostThinkTime, hostPicosPerByte, new BoundedQueue(hostQueueSize));
		flowGenerator.addHost(host2);
		
		// create a single cable and connect each end of the cable to a host.
		Cable cable = new Cable(cableDelayPicos);
		host1.connectCable(cable.north);
		host2.connectCable(cable.south);
		
		// run the dispatch loop for 1 second or until there are no more events.
		Dispatcher.dispatcher.processEvents(Conversion.secondsToPicos(1));	
	}
}
