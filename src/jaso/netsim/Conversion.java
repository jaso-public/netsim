package jaso.netsim;

public class Conversion {

	public static long picoPerSecond = 1_000_000_000_000L;
	
	public static long millisToPicos(long value) {
		return value * 1_000_000_000;
	}

	public static long microsToPicos(long value) {
		return value * 1_000_000;
	}
	
	public static long gbpsTo_bps(long value) {
		return value * 1_000_000_000;
	}
	
	public static long bitsToBytes(long value) {
		return value / 8;		
	}
}
