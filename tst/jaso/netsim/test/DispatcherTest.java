package jaso.netsim.test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import jaso.netsim.Dispatcher;
import jaso.netsim.Handler;

public class DispatcherTest {
    
    private static final Random rng = new Random();
    
    class TestHandler implements Handler {
        final AtomicInteger count;
        final AtomicLong lastEventTime;
        final AtomicBoolean asExpected;
        final long expectedTime;
          
  
        public TestHandler(AtomicInteger count, AtomicLong lastEventTime, AtomicBoolean asExpected, long expectedTime) {
            this.count = count;
            this.lastEventTime = lastEventTime;
            this.asExpected = asExpected;
            this.expectedTime = expectedTime;
        }


        @Override
        public void onEvent(long time) {
            count.incrementAndGet();
            
            if(expectedTime != time) {
                System.out.println("expected time does not match expected:"+expectedTime+"got:"+time);
                asExpected.set(false);            
            }
            
            if(lastEventTime.get() > time) {
                System.out.println("events are out of order");
                asExpected.set(false);
            }
            lastEventTime.set(time);;
        }


        @Override
        public String toString() {
            return "TestHandler [count=" + count + ", lastEventTime=" + lastEventTime + ", asExpected=" + asExpected
                    + ", expectedTime=" + expectedTime + "]";
        }   
    }

    @Test (timeout=1000)
    public void testEmptyDispacther() {
        Dispatcher dispatcher = new Dispatcher();
        // nothing to assert, if the test times out then dispatcher has a problem
        dispatcher.processEvents(1000);        
    }
    
    
    @Test (timeout=1000)
    public void testTwoEvents() {
        Dispatcher dispatcher = new Dispatcher();

        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastTime = new AtomicLong(0);
        AtomicBoolean asEpected = new AtomicBoolean(true);
        
        TestHandler th1 = new TestHandler(count, lastTime, asEpected, 100);
        dispatcher.register(100, th1);

        TestHandler th2 = new TestHandler(count, lastTime, asEpected, 150);
        dispatcher.register(150, th2);
        
        dispatcher.processEvents(1000);
        
        Assert.assertEquals(2,  count.get());
        Assert.assertTrue(asEpected.get());
    }

    @Test (timeout=1000)
    public void test100Events() {
        Dispatcher dispatcher = new Dispatcher();

        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastTime = new AtomicLong(0);
        AtomicBoolean asEpected = new AtomicBoolean(true);
        
        for(int i=0 ; i<100 ; i++ ) {
            long time = i * 73;
            dispatcher.register(time, new TestHandler(count, lastTime, asEpected, time));
        }

        dispatcher.processEvents(1000000);
        
        Assert.assertEquals(100,  count.get());
        Assert.assertTrue(asEpected.get());
    }

    @Test (timeout=1000)
    public void test100EventsReverseOrder() {
        Dispatcher dispatcher = new Dispatcher();

        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastTime = new AtomicLong(0);
        AtomicBoolean asEpected = new AtomicBoolean(true);
        
        for(int i=0 ; i<100 ; i++ ) {
            long time = 100000 - i * 73;
            dispatcher.register(time, new TestHandler(count, lastTime, asEpected, time));
        }

        dispatcher.processEvents(1000000);
        
        Assert.assertEquals(100,  count.get());
        Assert.assertTrue(asEpected.get());
    }
    
    @Test (timeout=1000)
    public void testMakeitGrow() {
        Dispatcher dispatcher = new Dispatcher();

        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastTime = new AtomicLong(0);
        AtomicBoolean asEpected = new AtomicBoolean(true);
        
        int number = Dispatcher.INITIAL_HEAP_SIZE * 3;
        
        for(int i=0 ; i<number ; i++ ) {
            long time = Long.MAX_VALUE - i * 73 - 100;
            dispatcher.register(time, new TestHandler(count, lastTime, asEpected, time));
        }

        dispatcher.processEvents(Long.MAX_VALUE);
        
        Assert.assertEquals(number,  count.get());
        Assert.assertTrue(asEpected.get());
    }

    
    class AddingHandler implements Handler {
        final Dispatcher dispatcher;
        final AtomicInteger count;
        final AtomicLong lastEventTime;
        final AtomicBoolean asExpected;
        final long expectedTime;
        
          
  
        public AddingHandler(Dispatcher dispatcher, AtomicInteger count, AtomicLong lastEventTime, AtomicBoolean asExpected, long expectedTime) {
            this.dispatcher = dispatcher;
            this.count = count;
            this.lastEventTime = lastEventTime;
            this.asExpected = asExpected;
            this.expectedTime = expectedTime;
        }


        @Override
        public void onEvent(long time) {
            count.incrementAndGet();
            
            if(expectedTime != time) {
                System.out.println("expected time does not match expected:"+expectedTime+"got:"+time);
                asExpected.set(false);            
            }
            
            if(lastEventTime.get() > time) {
                System.out.println("events are out of order");
                asExpected.set(false);
            }
            lastEventTime.set(time);
            
            if(count.get() <= 1000000) {
                int nextTime = rng.nextInt(1000);
                dispatcher.register(nextTime, new AddingHandler(dispatcher, count, lastEventTime, asExpected, dispatcher.now+nextTime));
            }
        }
    }

    
    @Test (timeout=1000)
    public void testAddsAsItGoes() {
        Dispatcher dispatcher = new Dispatcher();

        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastTime = new AtomicLong(0);
        AtomicBoolean asEpected = new AtomicBoolean(true);
        
        int number = 100;
        
        for(int i=0 ; i<number ; i++ ) {
            long time = i;
            dispatcher.register(time, new AddingHandler(dispatcher, count, lastTime, asEpected, time));
        }

        dispatcher.processEvents(Long.MAX_VALUE);
        
        Assert.assertEquals(1000000+ number,  count.get());
        Assert.assertTrue(asEpected.get());
    }
    
    @Test 
    public void testToStopsAtTheRightTime() {
        Dispatcher dispatcher = new Dispatcher();
        
        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastTime = new AtomicLong(0);
        AtomicBoolean asEpected = new AtomicBoolean(true);

        int time = 20;
        dispatcher.register(time, new TestHandler(count, lastTime, asEpected, time));
        
        time = 10;
        dispatcher.register(time, new TestHandler(count, lastTime, asEpected, time));

        dispatcher.processEvents(15);
        
        Assert.assertEquals(1,  count.get());
        Assert.assertTrue(asEpected.get());
    }


    
    
    @Test 
    public void testToStringDoesNotHaveAmpersat() {
        Dispatcher dispatcher = new Dispatcher();
        
        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastTime = new AtomicLong(0);
        AtomicBoolean asEpected = new AtomicBoolean(true);

        int time = 10;
        dispatcher.register(time, new TestHandler(count, lastTime, asEpected, time));
        
        String s = dispatcher.toString();
        Assert.assertEquals(s, -1, s.indexOf('@'));
    }
    
    


}
