package ziptrack.zip.vc;

import java.util.HashMap;

import ziptrack.event.EventType;
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
		if(this.getType().isLockType() || this.getType().isExtremeType()){
			this.threadOrLockCount.put(this.getDecor(), 1);
		}
	}

    @Override
    // update as we go
    public void computeData() {

    }
}
