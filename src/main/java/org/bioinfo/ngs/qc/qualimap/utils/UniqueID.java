package org.bioinfo.ngs.qc.qualimap.utils;

public class UniqueID {
	static long current= System.currentTimeMillis();
	static public synchronized long get(){
		return current++;
	}
}
