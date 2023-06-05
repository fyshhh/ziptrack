package ziptrack.zip.hb;

import java.util.HashSet;
import java.util.HashMap;

import ziptrack.zip.core.SymbolZip;

public abstract class SymbolZipHB extends SymbolZip<NonTerminalZipHB, TerminalZipHB> {
	protected String name;

	// Data structures used by the algorithm.
	protected HashMap<Integer, HashSet<Integer>> relevantAfterFirst;
	protected HashMap<Integer, HashSet<Integer>> relevantBeforeLast;
	protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> relevantAfterReads; // t -> x -> set_of_threads_locks
	protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> relevantBeforeReads; // t -> x -> set_of_threads_locks
	protected HashMap<Integer, HashSet<Integer>> relevantAfterWrites;
	protected HashMap<Integer, HashSet<Integer>> relevantBeforeWrites;

	SymbolZipHB(String n){
		super(n);
		this.name = n;
		init();
	}

    @Override
	protected void init(){
		relevantAfterFirst = null;
		relevantBeforeLast = null;
		relevantAfterReads = null;
		relevantBeforeReads = null;
		relevantAfterWrites = null;
		relevantBeforeWrites = null;
		
        super.init();
	}

	protected void destroy_helper(){
		deleteCounts();
		this.relevantThreadsOrLocks = null;
		this.relevantWrites = null;
		this.relevantReads = null;
		this.relevantAfterFirst = null;
		this.relevantBeforeLast = null;
		this.relevantAfterReads = null;
		this.relevantBeforeReads = null;
		this.relevantAfterWrites = null;
		this.relevantBeforeWrites = null;
		this.parents = null;
	}
	
	public abstract void destroy();
	
}
