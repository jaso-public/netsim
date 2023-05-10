package jaso.netsim.flowgenerator;

public class FlowIdGenerator {
	
	private static long nextFlowId = 0;
	
	public static long nextFlowId() {
		return nextFlowId++;
	}	
}


