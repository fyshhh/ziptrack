package ziptrack.zip.vc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import ziptrack.event.EventType;
import ziptrack.util.VectorClock;
import ziptrack.zip.core.TerminalZip;

public class TerminalZipVC extends SymbolZipVC implements TerminalZip {
	protected EventType type;
	protected int thread;
	protected int decor;

    public boolean allParentsNative;

    public TerminalZipVC(String name, EventType tp, int th, int dec) {
        super(name);
		this.type = tp;
		this.thread = th;
		this.decor = dec;
		this.allParentsNative = false;
    }

    @Override
	public boolean getAllParentsNative() {
		return this.allParentsNative;
	}

	@Override
	public void setAllParentsNative(boolean b) {
		this.allParentsNative = b;
	}

    public String toEventString(String tname, String decorName) {
		return "<" + tname+ "," + this.type.toString() + "(" + decorName + ")>";
	}

	public String toEventString() {
		return "<" + this.thread+ "," + this.type.toString() + "(" + this.decor + ")>";
	}

	public EventType getType() {
		return this.type;
	}

	public int getThread(){
		return this.thread;
	}

	public int getDecor(){
		return this.decor;
    }

    @Override
	protected void countVariables(){
		this.writeCount = new HashMap<Integer, Integer> ();
		if(this.getType().isWrite()){
			this.writeCount.put(this.getDecor(), 1);
		}
		this.readCount = new HashMap<Integer, HashMap<Integer, Integer>> ();
		if(this.getType().isRead()){
			this.readCount.put(this.getThread(), new HashMap<Integer, Integer> ());
			this.readCount.get(this.getThread()).put(this.getDecor(), 1);
		}
	}

	@Override
	protected void countThreadsOrLocks(){
		this.threadOrLockCount = new HashMap<Integer, Integer> ();
		this.threadOrLockCount.put(this.getThread(), 1);
		if (this.getType().isLockType() || this.getType().isExtremeType()) {
			this.threadOrLockCount.put(this.getDecor(), 1);
		}
	}

	public void computeClocks() {
		this.lastReadClocks = new HashMap<>();
		this.lastWriteClocks = new HashMap<>();
		this.firstReadClocks = new HashMap<>();
		this.firstWriteClocks = new HashMap<>();

		VectorClock vc = new VectorClock(this.numThreads);
		vc.setClockIndex(this.getThread(), 1);
		if (this.getType().isRead()) {
			HashMap<Integer, VectorClock> map = new HashMap<>();
			map.put(this.getDecor(), vc);
			this.lastReadClocks.put(this.getThread(), map);
			this.firstReadClocks.put(this.getThread(), map);
			map.put(7, vc);
			System.out.println(lastReadClocks.size());
			System.out.println(firstReadClocks.size());
		} else if (this.getType().isWrite()) {
			this.lastWriteClocks.put(this.getThread(), vc);
			this.firstWriteClocks.put(this.getThread(), vc);
		}
	}

	public void computeLockEvents() {
		// find out how to get number of locks
		this.lastReleases = new ArrayList<>(Collections.nCopies(1, new VectorClock(this.numThreads)));
		this.firstAcquires = new ArrayList<>(Collections.nCopies(1, new VectorClock(this.numThreads)));

		if (this.getType().isRelease()) {
			this.lastReleases.get(this.getDecor()).setClockIndex(this.getThread(), 1);
		} else if (this.getType().isAcquire()) {
			this.firstAcquires.get(this.getDecor()).setClockIndex(this.getThread(), 1);
			this.locksAcquired.add(this.getDecor());
		}
	}

	public void computeForkEvents() {
		this.lastEvents = new ArrayList<>(
			Collections.nCopies(this.numThreads, new VectorClock(this.numThreads)));
		this.lastEvents.get(this.getThread()).setClockIndex(this.getThread(), 1);
		this.lastForkEvents = new ArrayList<>(
			Collections.nCopies(this.numThreads, new VectorClock(this.numThreads)));
		if (this.getType().isFork()) {
			this.lastForkEvents.get(this.getThread()).setClockIndex(this.getThread(), 1);
		}
	}

	public void computeJoinEvents() {
		if (this.getType().isJoin()) {
			this.threadsJoined.add(this.getDecor());
		}
	}

    @Override
    // update as we go
    public void computeData() {
		this.computeClocks();
		this.computeLockEvents();
		this.computeForkEvents();
		this.computeJoinEvents();
    }
}
