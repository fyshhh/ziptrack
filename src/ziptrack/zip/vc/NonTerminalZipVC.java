package ziptrack.zip.vc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Vector;

import ziptrack.util.VectorClock;
import ziptrack.zip.core.NonTerminalZip;
import ziptrack.zip.core.SymbolZip;

public class NonTerminalZipVC extends SymbolZipVC implements NonTerminalZip<NonTerminalZipVC, TerminalZipVC> {
    protected ArrayList<SymbolZipVC> rule;
	public boolean allTerminals; //on RHS

    public HashSet<SymbolZipVC> criticalChildren;

	private ArrayList<Boolean> isEventInChunk; // lastRead, lastWrite, firstRead, firstWrite

    public NonTerminalZipVC(String name) {
        super(name);
        this.rule = null;
        this.criticalChildren = null;
		this.isEventInChunk = new ArrayList<>(Collections.nCopies(4, false));
    }

    public NonTerminalZipVC(String name, ArrayList<SymbolZipVC> r) {
        super(name);
        this.rule = r;
        this.criticalChildren = null;
		this.isEventInChunk = new ArrayList<>(Collections.nCopies(4, false));
    }

    @Override
	public void createCriticalChildren() {
		this.criticalChildren = new HashSet<>();
	}

	@Override
	public void addToCriticalChildren(SymbolZip<NonTerminalZipVC, TerminalZipVC> symb) {
		this.criticalChildren.add((SymbolZipVC) symb);
	}

    @Override
	public ArrayList<SymbolZipVC> getRule() {
		return this.rule;
	}

	@Override
	public void setRule(ArrayList<? extends SymbolZip<NonTerminalZipVC, TerminalZipVC>> r) {
		this.rule = new ArrayList<>(r.stream()
					.map(e -> (SymbolZipVC) e)
					.collect(Collectors.toList()));
	}

	@Override
	public boolean getAllTerminals() {
		return this.allTerminals;
	}

	@Override
	public void setAllTerminals(boolean b) {
		this.allTerminals = b;
	}

	public void printRule(){
		for(SymbolZipVC s: this.rule){
			System.out.print(s.name + " ");
		}
	}

    @Override
	protected void countVariables(){
		this.writeCount = new HashMap<Integer, Integer> ();
		for(SymbolZipVC symb: this.rule){
			for (HashMap.Entry<Integer, Integer> entry : symb.writeCount.entrySet()){
				int x = entry.getKey();
				int x_cnt = 0;
				if(this.writeCount.containsKey(x)){
					x_cnt = this.writeCount.get(x);
				}
				this.writeCount.put(x,  x_cnt + 1);	
			}
		}

		this.readCount = new HashMap<Integer, HashMap<Integer, Integer>> ();
		for(SymbolZipVC symb: this.rule){
			for (HashMap.Entry<Integer, HashMap<Integer, Integer>> entry : symb.readCount.entrySet()){
				int t = entry.getKey();

				HashMap<Integer, Integer> read_to_cnt = entry.getValue();
				for(int x : read_to_cnt.keySet()){
					if(!this.readCount.containsKey(t)){
						this.readCount.put(t, new HashMap<Integer, Integer> ());
					}
					int x_t_cnt = 0;
					if(this.readCount.get(t).containsKey(x)){
						x_t_cnt = this.readCount.get(t).get(x);
					}
					this.readCount.get(t).put(x, x_t_cnt + 1);
				}
			}
		}
	}

	@Override
	protected void countThreadsOrLocks(){
		this.threadOrLockCount = new HashMap<Integer, Integer> ();
		for (SymbolZipVC symb: this.rule) {
			for (HashMap.Entry<Integer, Integer> entry : symb.threadOrLockCount.entrySet()) {
				// System.out.println(entry);
				int u = entry.getKey();
				int u_cnt = 0;
				if (this.threadOrLockCount.containsKey(u)) {
					u_cnt = this.threadOrLockCount.get(u);
				}
				this.threadOrLockCount.put(u, u_cnt + 1);
			}
		}
	}

	private void initializeData(SymbolZipVC symb) {
		this.hasRace = symb.hasRace;

		this.lastReadClock = symb.lastReadClock;
		this.lastWriteClock = symb.lastWriteClock;
		this.firstReadClock = symb.firstReadClock;
		this.firstWriteClock = symb.firstWriteClock;

		this.locksAcquired = symb.locksAcquired;
		this.locksAcquiredBeforeLastRead = symb.locksAcquiredBeforeLastRead;
		this.locksAcquiredBeforeLastWrite = symb.locksAcquiredBeforeLastWrite;
		this.locksAcquiredBeforeFirstRead = symb.locksAcquiredBeforeFirstRead;
		this.locksAcquiredBeforeFirstWrite = symb.locksAcquiredBeforeFirstWrite;

		this.threadsJoined = symb.threadsJoined;
		this.threadsJoinedBeforeLastRead = symb.threadsJoinedBeforeLastRead;
		this.threadsJoinedBeforeLastWrite = symb.threadsJoinedBeforeLastWrite;
		this.threadsJoinedBeforeFirstRead = symb.threadsJoinedBeforeFirstRead;
		this.threadsJoinedBeforeFirstWrite = symb.threadsJoinedBeforeFirstWrite;

		// this.isEventInChunk.set(0, !symb.lastReadClock.isZero());
		// this.isEventInChunk.set(1, !symb.lastWriteClock.isZero());
		// this.isEventInChunk.set(2, !symb.firstReadClock.isZero());
		// this.isEventInChunk.set(3, !symb.firstWriteClock.isZero());
	}

	// Because we go through the terminals linearly, this object's clocks should
	// always precede symb's.
	// may have to shift inefficiently
	private VectorClock shiftVectorClock(VectorClock vc, SymbolZipVC symb) {
		VectorClock clock = new VectorClock(this.numThreads);
		for (int i = 0; i < clock.getDim(); i++) {
			if (clock.getClockIndex(i) > 0) {
				clock.setClockIndex(i, clock.getClockIndex(i) + this.lastEvents.get(i).getClockIndex(i));
			} else {
				// set vector maximum here
				clock.setClockIndex(i, i);
			}
		}
		return clock;
	}

	// Computes whether the four important events are in this or symb.
	private void computeIsEventInChunk(SymbolZipVC symb) {
		// if (!symb.lastReadClock.isZero()) this.isEventInChunk.set(0, true);
		// if (!symb.lastWriteClock.isZero()) this.isEventInChunk.set(1, true);
	}

	private void mergeLockSets(SymbolZipVC symb) {
		this.locksAcquired.addAll(symb.locksAcquired);

		// magic constant?
		if (!this.isEventInChunk.get(0)) {
			this.locksAcquiredBeforeLastRead = symb.locksAcquiredBeforeLastRead;
			this.locksAcquired.forEach((lock) -> {
				// if (this.firstAcquires.get(lock).isLessThanOrEqual(this.lastReadClock))
				// 	this.locksAcquiredBeforeLastRead.add(lock);
			});
		}
		if (!this.isEventInChunk.get(1)) {
			this.locksAcquiredBeforeLastWrite = symb.locksAcquiredBeforeLastWrite;
			this.locksAcquired.forEach((lock) -> {
				// if (this.firstAcquires.get(lock).isLessThanOrEqual(this.lastWriteClock))
				// 	this.locksAcquiredBeforeLastWrite.add(lock);
			});
		}
		if (!this.isEventInChunk.get(2)) {
			this.locksAcquiredBeforeFirstRead = symb.locksAcquiredBeforeFirstRead;
			this.locksAcquired.forEach((lock) -> {
				// if (this.firstAcquires.get(lock).isLessThanOrEqual(this.lastReadClock))
				// 	this.locksAcquiredBeforeFirstRead.add(lock);
			});
		}
		if (!this.isEventInChunk.get(3)) {
			this.locksAcquiredBeforeFirstWrite = symb.locksAcquiredBeforeFirstWrite;
			this.locksAcquired.forEach((lock) -> {
				// if (this.firstAcquires.get(lock).isLessThanOrEqual(this.firstWriteClock))
				// 	this.locksAcquiredBeforeFirstWrite.add(lock);
			});
		}
		
		// there should be some way to get the number of locks
		// save this for the end - perhaps move this to mergeClocks
		// for (int i = 0; i < this.lastReleases.size(); i++) {
		// 	if (!symb.lastReleases.get(i).isZero())
		// 		this.lastReleases.set(i,
		// 			shiftVectorClock(symb.lastReleases.get(i), symb));
		// 	if (this.firstAcquires.get(i).isZero())
		// 		this.firstAcquires.set(i,
		// 			shiftVectorClock(symb.firstAcquires.get(i), symb));
		// }
	}

	private void mergeForkSets(SymbolZipVC symb) {

	}

	private void mergeJoinSets(SymbolZipVC symb) {

	}

	private void checkForRace() {

	}

	private void mergeClocks(SymbolZipVC symb) {
		// if (!symb.lastReadClock.isZero()) {
		// 	this.lastReadClock = shiftVectorClock(symb.lastReadClock, symb);
		// }
		// if (!symb.lastWriteClock.isZero()) {
		// 	this.lastWriteClock = shiftVectorClock(symb.lastWriteClock, symb);
		// }
		// if (this.firstReadClock.isZero() && !symb.firstReadClock.isZero()) {
		// 	this.firstReadClock = shiftVectorClock(symb.firstReadClock, symb);
		// }
		// if (this.firstWriteClock.isZero() && !symb.firstWriteClock.isZero()) {
		// 	this.firstWriteClock = shiftVectorClock(symb.firstWriteClock, symb);
		// }
	}

	// this is called in ZipEngine and has to be defined in NonTerminalZip
	// i'm not sure whether the VC implementation needs this...
    @Override
    public void setIntermediateRelevantData() {

    }

    // TODO: implement this too
    @Override
    public void computeData() {
		HashSet<Integer> tSet = new HashSet<>();
		int rule_size = this.rule.size();
		for (int idx = 0; idx < rule_size; idx++) {
			TerminalZipVC term = (TerminalZipVC) this.rule.get(idx);
			tSet.add(term.getThread());
			if (term.getType().isExtremeType()){
				tSet.add(term.getDecor());
			}
		}

		// this.initializeData(this.rule.get(0));
		// merge all relevant data - in theory this should be relatively simple
		for (int idx = 1; idx < rule_size; idx++) {
			// if (this.hasRace) break;

			// TerminalZipVC term = (TerminalZipVC) this.rule.get(idx);
			// this.computeIsEventInChunk(term);
			// this.mergeLockSets(term);
			// this.mergeForkSets(term);
			// this.mergeJoinSets(term);
			// this.mergeClocks(term);
			// this.checkForRace();
		}
    }
}
