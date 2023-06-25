package ziptrack.zip.vc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Vector;

import ziptrack.grammar.Terminal;
import ziptrack.util.VectorClock;
import ziptrack.zip.core.NonTerminalZip;
import ziptrack.zip.core.SymbolZip;

public class NonTerminalZipVC extends SymbolZipVC implements NonTerminalZip<NonTerminalZipVC, TerminalZipVC> {
    protected ArrayList<SymbolZipVC> rule;
	public boolean allTerminals; //on RHS

    public HashSet<SymbolZipVC> criticalChildren;

	private HashMap<Integer, Boolean> isLastWriteInChunk;
	private HashMap<Integer, HashMap<Integer, Boolean>> isLastReadInChunk;	// t -> x -> bool
	private HashMap<Integer, Boolean> isFirstWriteInChunk;
	private HashMap<Integer, HashMap<Integer, Boolean>> isFirstReadInChunk;	// t -> x -> bool

    private HashMap<Integer, VectorClock> lastReleasesCopy;
    private HashMap<Integer, VectorClock> firstAcquiresCopy;

	private HashMap<Integer, VectorClock> lastEventsCopy;
    private HashMap<Integer, VectorClock> lastForkEventsCopy;

    public NonTerminalZipVC(String name) {
        super(name);
        this.rule = null;
        this.criticalChildren = null;
		this.isLastWriteInChunk = new HashMap<>();
		this.isLastReadInChunk = new HashMap<>();
		this.isFirstWriteInChunk = new HashMap<>();
		this.isFirstReadInChunk = new HashMap<>();
	}

    public NonTerminalZipVC(String name, ArrayList<SymbolZipVC> r) {
        super(name);
        this.rule = r;
        this.criticalChildren = null;
		this.isLastWriteInChunk = new HashMap<>();
		this.isLastReadInChunk = new HashMap<>();
		this.isFirstWriteInChunk = new HashMap<>();
		this.isFirstReadInChunk = new HashMap<>();
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

		this.lastReadClocks = symb.lastReadClocks;
		this.lastWriteClocks = symb.lastWriteClocks;
		this.firstReadClocks = symb.firstReadClocks;
		this.firstWriteClocks = symb.firstWriteClocks;

		this.lastReleases = symb.lastReleases;
		this.firstAcquires = symb.firstAcquires;
		this.locksAcquired = symb.locksAcquired;
		this.locksAcquiredBeforeLastRead = symb.locksAcquiredBeforeLastRead;
		this.locksAcquiredBeforeLastWrite = symb.locksAcquiredBeforeLastWrite;
		this.locksAcquiredBeforeFirstRead = symb.locksAcquiredBeforeFirstRead;
		this.locksAcquiredBeforeFirstWrite = symb.locksAcquiredBeforeFirstWrite;

		this.lastEvents = symb.lastEvents;
		this.lastForkEvents = symb.lastForkEvents;

		this.threadsJoined = symb.threadsJoined;
		this.threadsJoinedBeforeLastRead = symb.threadsJoinedBeforeLastRead;
		this.threadsJoinedBeforeLastWrite = symb.threadsJoinedBeforeLastWrite;
		this.threadsJoinedBeforeFirstRead = symb.threadsJoinedBeforeFirstRead;
		this.threadsJoinedBeforeFirstWrite = symb.threadsJoinedBeforeFirstWrite;

		if (symb instanceof TerminalZipVC) {
			TerminalZipVC csymb = (TerminalZipVC) symb;
			if (csymb.getType().isWrite()) {
				this.isLastWriteInChunk.put(csymb.getThread(), true);
				this.isFirstWriteInChunk.put(csymb.getThread(), true);
			} else if (csymb.getType().isRead()) {
				HashMap<Integer, Boolean> map = new HashMap<>();
				map.put(csymb.getDecor(), true);
				this.isLastReadInChunk.put(csymb.getThread(), map);
				this.isFirstReadInChunk.put(csymb.getThread(), map);
			}
		} else {
			NonTerminalZipVC csymb = (NonTerminalZipVC) symb;
			this.isLastWriteInChunk = csymb.isLastWriteInChunk;
			this.isLastReadInChunk = csymb.isLastReadInChunk;
			this.isFirstWriteInChunk = csymb.isFirstWriteInChunk;
			this.isFirstReadInChunk = csymb.isFirstReadInChunk;
		}
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

	private void computeClocks(SymbolZipVC symb) {
		this.lastReleasesCopy = new HashMap<>(this.lastReleases);
		for (HashMap.Entry<Integer, VectorClock> entry : symb.lastReleases.entrySet()) {
			lastReleasesCopy.put(entry.getKey(), shiftVectorClock(entry.getValue(), symb));
		}

		this.firstAcquiresCopy = new HashMap<>(this.firstAcquires);
		for (HashMap.Entry<Integer, VectorClock> entry : symb.firstAcquires.entrySet()) {
			firstAcquiresCopy.put(entry.getKey(), shiftVectorClock(entry.getValue(), symb));
		}

		this.lastEventsCopy = new HashMap<>(this.lastEvents);
		for (HashMap.Entry<Integer, VectorClock> entry : symb.lastEvents.entrySet()) {
			lastEventsCopy.put(entry.getKey(), shiftVectorClock(entry.getValue(), symb));
		}

		this.lastForkEventsCopy = new HashMap<>(this.lastForkEvents);
		for (HashMap.Entry<Integer, VectorClock> entry : symb.lastForkEvents.entrySet()) {
			lastForkEventsCopy.put(entry.getKey(), shiftVectorClock(entry.getValue(), symb));
		}
	}

	private void mergeLockSets(SymbolZipVC symb) {
		for (HashMap.Entry<Integer, VectorClock> entry : symb.lastWriteClocks.entrySet()) {
			int var = entry.getKey();
			HashSet<Integer> copy = new HashSet<>(symb.locksAcquiredBeforeLastWrite.get(var));
			this.locksAcquired.forEach(lock -> {
				if (this.firstAcquires
					.getOrDefault(lock, new VectorClock(this.numThreads))
					.isLessThanOrEqual(this.lastWriteClocks.getOrDefault(var, new VectorClock(this.numThreads)))) {
						copy.add(lock);
					}
			});
			this.locksAcquiredBeforeLastWrite.put(var, copy);
		}

		for (HashMap.Entry<Integer, HashMap<Integer, VectorClock>> entry : symb.lastReadClocks.entrySet()) {
			int var = entry.getKey();
			HashMap<Integer, HashSet<Integer>> threadMap = symb.locksAcquiredBeforeLastRead.get(var);
			for (HashMap.Entry<Integer, HashSet<Integer>> entry2 : threadMap.entrySet()) {
				int thread = entry2.getKey();
				HashSet<Integer> copy = new HashSet<>(threadMap.get(thread));
				this.locksAcquired.forEach(lock -> {
					if (this.firstAcquires
						.getOrDefault(lock, new VectorClock(this.numThreads))
						.isLessThanOrEqual(this.lastReadClocks
							.getOrDefault(var, new HashMap<>())
							.getOrDefault(thread, new VectorClock(this.numThreads)))) {
								copy.add(lock);
							}
				});
				this.locksAcquiredBeforeLastRead.computeIfAbsent(var, i -> new HashMap<>()).put(thread, copy);
			}
		}

		for (HashMap.Entry<Integer, VectorClock> entry : symb.firstWriteClocks.entrySet()) {
			int var = entry.getKey();
			HashSet<Integer> copy = new HashSet<>(symb.locksAcquiredBeforeFirstWrite.get(var));
			this.locksAcquired.forEach(lock -> {
				if (this.firstAcquires
					.getOrDefault(lock, new VectorClock(this.numThreads))
					.isLessThanOrEqual(this.firstWriteClocks.getOrDefault(var, new VectorClock(this.numThreads)))) {
						copy.add(lock);
					}
			});
			this.locksAcquiredBeforeFirstWrite.put(var, copy);
		}

		for (HashMap.Entry<Integer, HashMap<Integer, VectorClock>> entry : symb.firstReadClocks.entrySet()) {
			int var = entry.getKey();
			HashMap<Integer, HashSet<Integer>> threadMap = symb.locksAcquiredBeforeFirstRead.get(var);
			for (HashMap.Entry<Integer, HashSet<Integer>> entry2 : threadMap.entrySet()) {
				int thread = entry2.getKey();
				HashSet<Integer> copy = new HashSet<>(threadMap.get(thread));
				this.locksAcquired.forEach(lock -> {
					if (this.firstAcquires
						.getOrDefault(lock, new VectorClock(this.numThreads))
						.isLessThanOrEqual(this.firstReadClocks
							.getOrDefault(var, new HashMap<>())
							.getOrDefault(thread, new VectorClock(this.numThreads)))) {
								copy.add(lock);
							}
				});
				this.locksAcquiredBeforeFirstRead.computeIfAbsent(var, i -> new HashMap<>()).put(thread, copy);
			}
		}
		
		this.locksAcquired.addAll(symb.locksAcquired);
	}

	private void mergeJoinSets(SymbolZipVC symb) {

	}

	private void updateClocks(SymbolZipVC symb) {
		symb.lastWriteClocks.forEach((var, clock) -> this.lastWriteClocks.put(var, clock));
		symb.lastReadClocks.forEach((var, map) ->
			map.forEach((thread, clock) ->
				this.lastReadClocks.computeIfAbsent(var, v -> new HashMap<>()).put(thread, clock)));

		symb.firstWriteClocks.forEach((var, clock) -> this.firstWriteClocks.putIfAbsent(var, clock));

		symb.firstReadClocks.forEach((var, map) ->
			map.forEach((thread, clock) ->
				this.firstReadClocks.computeIfAbsent(var, v -> new HashMap<>()).putIfAbsent(var, clock)));

		this.lastReleases = this.lastReleasesCopy;
		this.firstAcquires = this.firstAcquiresCopy;

		this.lastEvents = this.lastEventsCopy;
		this.lastForkEvents = this.lastForkEventsCopy;
	}

	private void checkForRace(SymbolZipVC symb) {
		for (HashMap.Entry<Integer, VectorClock> entry : this.lastWriteClocks.entrySet()) {
			Integer var = entry.getKey();
			VectorClock clock = entry.getValue();
			if (clock.isLessThanOrEqual(symb.firstWriteClocks.get(var))) {
				this.hasRace = true;
				return;
			}
			for (VectorClock clock2 : symb.firstReadClocks.get(entry.getKey()).values()) {
				if (clock.isLessThanOrEqual(clock2)) {
					this.hasRace = true;
					return;
				}
			}
		}

		for (HashMap.Entry<Integer, HashMap<Integer, VectorClock>> entry : this.lastReadClocks.entrySet()) {
			Integer var = entry.getKey();
			HashMap<Integer, VectorClock> threadMap = entry.getValue();
			for (VectorClock clock : threadMap.values()) {
				if (clock.isLessThanOrEqual(symb.firstWriteClocks.get(var))) {
					this.hasRace = true;
					return;
				}
			}
		}
	}

	private void updateRWClocks(SymbolZipVC symb) {		
		symb.lastWriteClocks.replaceAll((var, clock) -> shiftVectorClock(clock, symb));

		symb.lastReadClocks.forEach((thr, map) -> {
			map.replaceAll((var, clock) -> shiftVectorClock(clock, symb));
		});

		symb.firstWriteClocks.replaceAll((var, clock) -> shiftVectorClock(clock, symb));

		symb.firstReadClocks.forEach((thr, map) -> {
			map.replaceAll((var, clock) -> shiftVectorClock(clock, symb));
		});
	}

	// this is called in ZipEngine and has to be defined in NonTerminalZip
	// i'm not sure whether the VC implementation needs this...
    @Override
    public void setIntermediateRelevantData() {

    }

    // TODO: implement this too
    @Override
    public void computeData() {
		// temporary - eventually our implementation shouldn't care.
		if (!this.allTerminals) {
			System.out.println("not all terminals!");
			return;
		}
		HashSet<Integer> tSet = new HashSet<>();
		int rule_size = this.rule.size();
		for (int idx = 0; idx < rule_size; idx++) {
			TerminalZipVC term = (TerminalZipVC) this.rule.get(idx);
			tSet.add(term.getThread());
			if (term.getType().isExtremeType()){
				tSet.add(term.getDecor());
			}
		}

		this.initializeData(this.rule.get(0));
		// merge all relevant data - in theory this should be relatively simple
		for (int idx = 1; idx < rule_size; idx++) {

			TerminalZipVC term = (TerminalZipVC) this.rule.get(idx);

			this.hasRace |= term.hasRace;
			if (this.hasRace) break;
			// Suppose we have a rule A -> BC. We need to be careful with the order
			// of our operations because this object originally stores B's data,
			// and we want to use it to store A's data (which involves calling shift)
			// without affecting correctness, so we need to do some preprocessing
			// and store structures that have been shifted while we merge the rest of
			// the data.
			// 1. (shift) update B's and C's last/first read/write clocks relative to A
			// 2. check for race
			// 3. (shift) compute A's lock/fork/join clocks, but don't overwrite B's yet
			// 4. merge lock and join sets
			// 5. overwrite B's sets to A's
			this.updateRWClocks(term);
			this.checkForRace(term);
			this.computeClocks(term);
			this.mergeLockSets(term);
			this.mergeJoinSets(term);
			this.updateClocks(term);
		}
    }
}
