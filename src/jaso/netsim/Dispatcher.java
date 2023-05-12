package jaso.netsim;

public class Dispatcher {
	public static final int INITIAL_HEAP_SIZE = 1024 * 1024;
	
    
	public static Dispatcher dispatcher = new Dispatcher();
	
    public long now = 0;
	private int size = 0;
	private long[] eventTimes = new long[INITIAL_HEAP_SIZE];
	private Handler[] handlers = new Handler[INITIAL_HEAP_SIZE];

		
	public Dispatcher() {
//		System.out.println("Dispatcher created");
//		Exception e = new Exception();
//		e.printStackTrace();
	}
	
	private void enlarge() {
	    int oldSize = eventTimes.length;
	    long[] oldEventTimes = eventTimes;
	    Handler[] oldHandlers = handlers;
	    
	    int newSize = oldSize * 2;
	    eventTimes = new long[newSize];
	    handlers = new Handler[newSize];
	    
        System.arraycopy(oldEventTimes, 0, eventTimes, 0, oldSize);
        System.arraycopy(oldHandlers, 0, handlers, 0, oldSize);
	}
	
	

	public void register(long elapsed, Handler handler) {
	    if(size >= eventTimes.length) enlarge();
	    
	    long value = now + elapsed;
	    
		int index = size;
		size++;
		
		
        int parentIndex = (index - 1) / 2;
         
        while (index > 0 && value < eventTimes[parentIndex]) {
            eventTimes[index] = eventTimes[parentIndex];
            handlers[index] = handlers[parentIndex];
            index = parentIndex;
            parentIndex = (index - 1) / 2;
        }
        
        eventTimes[index] = value;
        handlers[index] = handler;
 	}
	 
	
    public void removeFirst() {
        if (size < 2) {
            if (size < 1) {
                throw new RuntimeException("heap is empty");
            }
            size--;
            return;
        }
        
        size--;
        eventTimes[0] = eventTimes[size];
        handlers[0] = handlers[size];

        int index = 0;
        int smallestIndex = index;
        boolean isDone = false;
        
        while (!isDone) {
            int leftChildIndex = 2 * index + 1;
            int rightChildIndex = 2 * index + 2;
            
            // Compare with left child
            if (leftChildIndex < size && eventTimes[leftChildIndex] < eventTimes[smallestIndex]) {
                smallestIndex = leftChildIndex;
            }
            
            // Compare with right child
            if (rightChildIndex < size && eventTimes[rightChildIndex] < eventTimes[smallestIndex]) {
                smallestIndex = rightChildIndex;
            }
            
            // Swap with the smaller child if necessary
            if (smallestIndex != index) {
                long tempEventTime = eventTimes[index];
                eventTimes[index] = eventTimes[smallestIndex];
                eventTimes[smallestIndex] = tempEventTime;
                
                Handler tempHandler = handlers[index];
                handlers[index] = handlers[smallestIndex];
                handlers[smallestIndex] = tempHandler;

                index = smallestIndex;
            } else {
                isDone = true;
            }
        }
    }

	public void processEvents(long stopTime) {
		while( size > 0) {
			now = eventTimes[0];
			if(now > stopTime) {
				System.out.println("stop time reached:"+stopTime+" now:"+now);
				return;
			}
			handlers[0].onEvent(now);
			removeFirst();
		}
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dispatcher -- now=" + now + ", size=" + size + "\n");
        for(int i=0 ; i<size ; i++) {
            sb.append("   i:"+i+" "+eventTimes[i]+" "+handlers[i]+"\n");
        }
        return sb.toString();
    }
}
