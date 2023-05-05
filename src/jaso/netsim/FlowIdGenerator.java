package jaso.netsim;

public class FlowIdGenerator {
	
	private static long nextFlowId = 0;
	
	public static long nextFlowId() {
		return nextFlowId++;
	}	
}


