package jaso.netsim;

public class TcpSource implements Sink, Handler {
	
	public final static int MSS_BYTES = 1500;
	public final static long MIN_RTO_PICO =     200_000_000L; // 200 microseconds
	public final static long INIT_RTO_PICO = 25_000_000_000L; // 25 millis 
	
	enum State {IDLE, SLOW_START, CONG_AVOID, FAST_RECOV, FINISH}

	
	public final String name;
	final Host host;
	final long flowId;
	final int dst;
	final long flowSize;
	int nextPacketId = 0;
	State state;
	
	long lastAcked = 0;
	long highestSent = 0;
	long startTime = 0;
	long bytesSent = 0;
	long congestionWindow = 0;

	
	long _RFC2988_RTO_timeout = 0;
	long _rto = INIT_RTO_PICO;
	long _rtt;
	long _mdev;
	long _ssthresh = 0xffffffff;
	long duplicateAcks;
	long _drops;
	long _recover_seq;
	
	
	public TcpSource(Host host, long flowId, int dst, long flowSize) {
		this.name = "TcpSrc-"+flowId;
		this.host = host;
		this.flowId = flowId;
		this.dst = dst;
		this.flowSize = flowSize;
		
		if(flowSize < 1) throw new RuntimeException("flowSize must be greater than zero");
		
		startTime = Dispatcher.dispatcher.now;
        state = State.SLOW_START;
        congestionWindow = 2 * MSS_BYTES; 
        
        sendPackets();
        Dispatcher.dispatcher.register(_rto, this);
	}

	void slowdown() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onEvent(long now) {
		
		System.out.println(now+" "+name+" onEvent()");
	    slowdown();

		// Cleanup the finished flow.
	    if (state == State.FINISH) {
	    	return;
	    }

	    // Retransmission timeout.
	    if (_RFC2988_RTO_timeout != 0 && now >= _RFC2988_RTO_timeout) {
	    	System.out.println("retransmit "+name);

	        if (state == State.FAST_RECOV) {
	            long flightsize = highestSent - lastAcked;
	            congestionWindow = Math.min(_ssthresh, flightsize + MSS_BYTES);
	        }

	        _ssthresh = Math.max(congestionWindow / 2, MSS_BYTES * 2);

	        congestionWindow = MSS_BYTES;
	        state = State.SLOW_START;
	        _recover_seq = highestSent;
	        highestSent = lastAcked + MSS_BYTES;
	        duplicateAcks = 0;

	        // Reset rtx timerRFC 2988 5.5 & 5.6
	        _rto *= 2;
	        _RFC2988_RTO_timeout = now + _rto;

	        retransmitPacket();
	    }

	    long nextTimeOut = _rtt != 0 ? _rtt : MIN_RTO_PICO;
	    Dispatcher.dispatcher.register(nextTimeOut, this);
	}

	@Override
	public void receive(Packet packet) {
	    long now = Dispatcher.dispatcher.now;
		System.out.println(now+" "+name+" recv "+packet);
		
		slowdown();
 	    
	    if (state == State.FINISH) {
	        return;
	    }	    

	    if (packet.ackSeqNum >= flowSize) {

//	        if (_flowgen != NULL) {
//	            _flowgen->finishFlow(_flow_id, now - startTime);
//	        }
	        
	        state = State.FINISH;
	        return;
	    }

	    // Delayed / reordered ack. Shouldn't happen for simple queues.
	    if (packet.ackSeqNum < lastAcked) {
	        System.out.println("TCP " + "ACK from the past: packet.ackSeqNum " + packet.ackSeqNum + " lastAcked " + lastAcked); 
	        return;
	    }


	    // Update rtt and update _rto.
	    long m = now - packet.timestamp;
	    if (m > 0) {
	        if (_rtt > 0) {
	            long abs = Math.abs(m - _rtt);
	            _mdev = _mdev * 3 / 4 + abs / 4;
	            _rtt = _rtt * 7 / 8 + m / 8;
	        }
	        else {
	            _rtt = m;
	            _mdev = m/2;
	        }
	        
	        _rto = _rtt + 4 * _mdev;
	        if (_rto < MIN_RTO_PICO) {
	            _rto = MIN_RTO_PICO;
	        }
	        
	        System.out.println(now+" "+name+" rtt:"+_rtt+" mdev:"+_mdev);
	    }


	    // Brand new ack.
	    if (packet.ackSeqNum > lastAcked) {

	        // RFC 2988 5.3
	        _RFC2988_RTO_timeout = now + _rto;

	        // RFC 2988 5.2
	        if (packet.ackSeqNum == highestSent) {
	            _RFC2988_RTO_timeout = 0;
	        }

	        // Best behaviour: proper ack of a new packet, when we were expecting it.
	        if (state != State.FAST_RECOV) { // _state == SLOW_START || CONG_AVOID
	            lastAcked = packet.ackSeqNum;
	            duplicateAcks = 0;
	            inflateWindow();

	            sendPackets();
	            return;
	        }

	        // We're in fast recovery, i.e. one packet has been
	        // dropped but we're pretending it's not serious.
	        if (packet.ackSeqNum >= _recover_seq) {
	            // got ACKs for all the "recovery window": resume normal service
	            long flightsize = highestSent - packet.ackSeqNum;
	            congestionWindow = Math.min(_ssthresh, flightsize + MSS_BYTES);
	            lastAcked = packet.ackSeqNum;
	            duplicateAcks = 0;
	            state = State.CONG_AVOID;

	            sendPackets();
	            return;
	        }

	        // In fast recovery, and still getting ACKs for the "recovery window".
	        // This is dangerous. It means that several packets got lost, not just
	        // the one that triggered FR.
	        long new_data = packet.ackSeqNum - lastAcked;
	        lastAcked = packet.ackSeqNum;

	        if (new_data < congestionWindow) {
	            congestionWindow -= new_data;
	        } else {
	            congestionWindow = 0;
	        }

	        congestionWindow += MSS_BYTES;

	        retransmitPacket();
	        sendPackets();
	        return;
	    }

	    // It's a dup ack.
	    if (state == State.FAST_RECOV) {
	        // Still in fast recovery; hopefully the prodigal ACK is on it's way.
	        congestionWindow += MSS_BYTES;

	        sendPackets();
	        return;
	    }

	    // Not yet in fast recovery. Wait for more dupacks.
	    duplicateAcks++;

	    if (duplicateAcks != 3) {
	        sendPackets();
	        return;
	    }

	    // duplicateAcks == 3
	    if (lastAcked < _recover_seq) {
	        // See RFC 3782: if we haven't recovered from timeouts etc. don't do fast recovery.
	        return;
	    }

	    // Begin fast retransmit/recovery. (count drops only in CA state)
	    _drops++;

	    _ssthresh = Math.max(congestionWindow / 2, MSS_BYTES * 2);
	    congestionWindow = _ssthresh + 3 * MSS_BYTES;
	    state = State.FAST_RECOV;

	    // _recover_seq is the value of the ack that tells us things are back to normal
	    _recover_seq = highestSent;

	    retransmitPacket();
	}

	void inflateWindow()
	{
	    // Be very conservative - possibly not the best we can do, but
	    // the alternative has bad side effects.
	    long newly_acked = (lastAcked + congestionWindow) - highestSent;
	    long increment;

	    if (newly_acked < 0) {
	        return;
	    } else if (newly_acked > MSS_BYTES) {
	        newly_acked = MSS_BYTES;
	    }

	    if (congestionWindow < _ssthresh) {
	        // Slow start phase.
	        increment = Math.min(_ssthresh - congestionWindow, newly_acked);
	    } else {
	        // Congestion avoidance phase.
	        increment = (newly_acked * MSS_BYTES) / congestionWindow;
	        if (increment == 0) {
	            increment = 1;
	        }
	    }
	    congestionWindow += increment;
	}

	void sendPackets() {
	    long now = Dispatcher.dispatcher.now;

	    // Already sent out enough bytes.
	    if (flowSize > 0 && highestSent >= flowSize) {
	        return;
	    }


	    while (lastAcked + congestionWindow >= highestSent + MSS_BYTES) {
	    	Packet packet = new Packet(host.hostId, dst, flowId, MSS_BYTES, highestSent+1, 1);
	        host.send(packet);

	        highestSent += MSS_BYTES;
	        bytesSent += MSS_BYTES;

	        if (_RFC2988_RTO_timeout == 0) { // RFC2988 5.1
	            _RFC2988_RTO_timeout = now + _rto;
	        }

	        if (highestSent >= flowSize) {
	            break;
	        }
	    }
	}
	
	void retransmitPacket() {
		System.out.println("retransmit lastAcked:"+lastAcked+1);
		Packet packet = new Packet(host.hostId, dst, flowId, MSS_BYTES, lastAcked+1, 1);
	    host.send(packet);;
	    bytesSent += MSS_BYTES;

	    if(_RFC2988_RTO_timeout == 0) { // RFC2988 5.1
	        _RFC2988_RTO_timeout = Dispatcher.dispatcher.now + _rto;
	    }
	}


	@Override
	public String getName() {
		return name;
	}


}
