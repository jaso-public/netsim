package jaso.netsim;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class Dispatcher {
	
	public static Dispatcher dispatcher = new Dispatcher();
	
	private SortedMap<Long, ArrayList<Handler>> events = new TreeMap<>();
	public long now = 0;
		
	private Dispatcher() {
		System.out.println("Dispatcher created");
	}
	
	public void register(long elapsed, Handler handler) {
		// System.out.println(now+" register "+handler+" in:"+elapsed);
		long futureTime = now + elapsed;
		
		ArrayList<Handler> concurrent = events.get(futureTime);
		if(concurrent == null) {
			concurrent = new ArrayList<>();
			events.put(futureTime, concurrent);
		}
		concurrent.add(handler);		
	}
	
	public void processEvents(long stopTime) {
		while( events.size() > 0) {
			now = events.firstKey();
			if(now > stopTime) {
				System.out.println("stop time reached:"+stopTime+" now:"+now);
				return;
			}
			
			ArrayList<Handler> concurrent = events.get(now);
			if(concurrent.isEmpty()) {
				events.remove(now);
				continue;
			}
			
			Handler handler = concurrent.remove(concurrent.size()-1);
			//System.out.println(now+" dispatching handler:"+handler);
			handler.onEvent(now);
		}
	}
}
