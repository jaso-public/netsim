package jaso.netsim;

public class Conversion {

	public static long picoPerSecond = 1_000_000_000_000L;
	
    public static long secondsToPicos(long value) {
        return value * 1_000_000_000_000L;
    }
	
	public static long millisToPicos(long value) {
		return value * 1_000_000_000;
	}

    public static long microsToPicos(long value) {
        return value * 1_000_000;
    }
    
    public static long nanosToPicos(long value) {
        return value * 1000;
    }
    
	public static long gbpsTo_bps(long value) {
		return value * 1_000_000_000;
	}
	
	public static long bitsToBytes(long value) {
		return value / 8;		
	}
	
	public static long bpsToPicosPerByte(long bitRate) {
	    return Conversion.picoPerSecond / Conversion.bitsToBytes(bitRate);
	}

    public static long megabytesToBytes(long value) {
        return value * 1024 * 1024;
    }
}
  
