package jaso.netsim;

import jaso.netsim.flowgenerator.FlowGenerator;
import jaso.netsim.flowgenerator.UniformFlowGenerator;
import jaso.netsim.queueing.UnboundedQueue;

public class SimulatorHostToHost {

	public static void main(String[] args) {
		
		long hostThinkTime = 100;
		long hostBitRate = Conversion.gbpsTo_bps(10);
				
		long cableDelayPicos = Conversion.microsToPicos(2);		
		
		long hostPicosPerByte = Conversion.picoPerSecond / Conversion.bitsToBytes(hostBitRate);		

		FlowGenerator flowGenerator = new UniformFlowGenerator();
		
		Host host1 = new Host(1, hostThinkTime, hostPicosPerByte, new UnboundedQueue());
		flowGenerator.addHost(host1);
		Host host2 = new Host(2, hostThinkTime, hostPicosPerByte, new UnboundedQueue());
		flowGenerator.addHost(host2);
		
		Cable cable = new Cable(cableDelayPicos);
		host1.connectCable(cable.north);
		host2.connectCable(cable.south);
				
		Dispatcher.dispatcher.processEvents(1_000_000_000_000L);	
	}
}
