package ziptrack.ziphb;

import java.util.HashSet;
import java.util.HashMap;

import ziptrack.grammar.Symbol;
import ziptrack.util.Interval;

public abstract class SymbolZipHB extends Symbol {
	protected String name;

	protected HashMap<Integer, Integer> threadOrLockCount; // How many children have occurrences of some thread t
	protected HashMap<Integer, Integer> writeCount; // How many children have occurrences of w(x) in them
	protected HashMap<Integer, HashMap<Integer, Integer>> readCount; // t -> x -> cnt(t,x)

	protected HashSet<Integer> relevantThreadsOrLocks;
	protected HashSet<Integer> relevantWrites;
	protected HashMap<Integer, HashSet<Integer>> relevantReads; // t -> Set(relevant reads)

	protected HashMap<Integer, HashSet<Integer>> relevantAfterFirst;
	protected HashMap<Integer, HashSet<Integer>> relevantBeforeLast;
	protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> relevantAfterReads; // t -> x -> set_of_threads_locks
	protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> relevantBeforeReads; // t -> x -> set_of_threads_locks
	protected HashMap<Integer, HashSet<Integer>> relevantAfterWrites;
	protected HashMap<Integer, HashSet<Integer>> relevantBeforeWrites;
	protected Boolean hasRace;

	public HashMap<NonTerminalZipHB, Integer> parents; // For parents(p) = #of occurrences of this as a child node of p.
	public int topologicalIndex;
	
	//Sanity
	public int len;
	public boolean crossRace;
	public HashMap<Integer, HashSet<Interval>> setRange; // Variable ->Set of intervals

	SymbolZipHB(String n){
		super(n);
		this.name = n;
		init();
	}

	public String getName(){
		return this.name;
	}

	protected abstract void countThreadsOrLocks();
	protected abstract void countVariables();
	
	protected void countObjects() {
		this.countThreadsOrLocks();
		this.countVariables();
	}

	public boolean Race(){
		return this.hasRace;
	}

	protected void init(){
		threadOrLockCount = null;
		writeCount = null;
		readCount = null;

		relevantWrites = null;
		relevantReads = null;
		relevantThreadsOrLocks = null;

		relevantAfterFirst = null;
		relevantBeforeLast = null;
		relevantAfterReads = null;
		relevantBeforeReads = null;
		relevantAfterWrites = null;
		relevantBeforeWrites = null;
		
		hasRace = null;
		parents = null;
		topologicalIndex = -1;
		
		len = 0;
		crossRace = false;
		setRange = null;
	}

	public abstract void computeData(boolean stopAfterFirstRace, boolean sanityCheck);	

	private void computeRelevantVariables(){
		this.relevantWrites = new HashSet<Integer> ();
		for (HashMap.Entry<Integer, Integer> entry : this.writeCount.entrySet()){
			int x = entry.getKey();
			for(NonTerminalZipHB p : this.parents.keySet()){
				if(p.relevantWrites.contains(x)){
					this.relevantWrites.add(x);
					break;
				}
				if(p.writeCount.get(x) >= 2){
					this.relevantWrites.add(x);
					break;
				}
				
				for(int t: p.threadOrLockCount.keySet()){ // Here t could be a lock as well. But in the next line, this is going to be ruled out because the domain of threadReadCount is going to be restricted to threads 
					if(p.readCount.containsKey(t)){
						if(p.readCount.get(t).containsKey(x)){					
							if(p.readCount.get(t).get(x) >= 1){ // This is an over-approximation. "this" could be the only child that has a read event of x.
								
								boolean this_has_read_t_x = false;
								
								//Case-1. this has a read(t, x) for some t, and this occurs atleast twice in parent
								if(this.readCount.containsKey(t)){
									if(this.readCount.get(t).containsKey(x)){
										this_has_read_t_x = true;
										if(this.parents.get(p) >= 2){
											this.relevantWrites.add(x);
											break;
										}
										//Case-2. there is atleast one more sibling that has read(t,x) for some t, apart from me.
										else if(p.readCount.get(t).get(x) >= 2){
											this.relevantWrites.add(x);
											break;
										}

									}
								}
								//Case-3. this does not have a read(t,x) for any t, but there is a sibling (and thus a parent) that has.
								if(!this_has_read_t_x){
									if(p.readCount.get(t).get(x) >= 2){
										this.relevantWrites.add(x);
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		
		this.relevantReads = new HashMap<Integer, HashSet<Integer>> ();
		for (HashMap.Entry<Integer, HashMap<Integer, Integer>> entry : this.readCount.entrySet()){
			int t = entry.getKey();
			HashMap<Integer, Integer> read_to_cnt = entry.getValue();			
			for(int x : read_to_cnt.keySet()){
				for(NonTerminalZipHB p : this.parents.keySet()){
					if(p.relevantReads.containsKey(t)){
						if(p.relevantReads.get(t).contains(x)){
							if(!this.relevantReads.containsKey(t)){
								this.relevantReads.put(t, new HashSet<Integer> ());
							}
							this.relevantReads.get(t).add(x);
							break;
						}
					}
					if(p.writeCount.containsKey(x)){
						//Case-1. I write to x and my parent has 2 children that write to x.
						if(this.writeCount.containsKey(x)){
							if(p.writeCount.get(x) >= 2){
								if(!this.relevantReads.containsKey(t)){
									this.relevantReads.put(t, new HashSet<Integer> ());
								}
								this.relevantReads.get(t).add(x);
								break;
							}
						}
						//Case-2. I do not write to x, and there is one child of my parent that writes to x.
						else if(p.writeCount.get(x) >= 1){
							if(!this.relevantReads.containsKey(t)){
								this.relevantReads.put(t, new HashSet<Integer> ());
							}
							this.relevantReads.get(t).add(x);
							break;
						}
					}
				}
				
			}
		}
	}
	
	private void computeRelevantThreadsOrLocks(){
		this.relevantThreadsOrLocks = new HashSet<Integer> ();
		for (HashMap.Entry<Integer, Integer> entry : this.threadOrLockCount.entrySet()){
			int u = entry.getKey();
			for(NonTerminalZipHB p : this.parents.keySet()){
				if(p.relevantThreadsOrLocks.contains(u)){
					this.relevantThreadsOrLocks.add(u);
					break;
				}
				if(p.threadOrLockCount.get(u) >= 2){
					this.relevantThreadsOrLocks.add(u);
					break;
				}
			}
		}
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
		
	public void computeRelevantData(){
		computeRelevantVariables();
		computeRelevantThreadsOrLocks();
	}
	
	public void deleteCounts(){
		//Counts are not needed once all the relevant 
		//things (including those for intermediate non terminals) have been calculated
		this.threadOrLockCount = null;
//		this.writeCount = null;
//		this.readCount = null;
	}
}
