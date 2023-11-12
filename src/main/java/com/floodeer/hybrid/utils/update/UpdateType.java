package com.floodeer.hybrid.utils.update;

import java.util.Arrays;

public enum UpdateType {

	MIN_64(3840000L), MIN_32(1920000L), MIN_16(960000L), MIN_08(480000L), MIN_04(240000L), MIN_02(120000L), MIN_01(
			60000L), SLOWEST(32000L), SLOWER(16000L), SLOW(4000L), SEC(1000L), FAST(500L), FASTER(250L), FASTEST(
			125L), TICKS_2(75L), TICK(49L);

	private final long time;
	private long last;

	UpdateType(long paramLong) {
		this.time = paramLong;
		this.last = System.currentTimeMillis();
	}

	public boolean elapsed() {
		if (elapsed(this.last, this.time)) {
			this.last = System.currentTimeMillis();
			return true;
		}
		return false;
	}
	
	public long getTime() {
		return time;
	}

	private boolean elapsed(long paramLong1, long paramLong2) {
		return System.currentTimeMillis() - paramLong1 > paramLong2;
	}
}