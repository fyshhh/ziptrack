package ziptrack.zip.vc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ziptrack.util.VectorClock;

import ziptrack.zip.core.SymbolZip;

public abstract class SymbolZipVC extends SymbolZip<NonTerminalZipVC, TerminalZipVC> {
    protected String name;

    // Number of locks and threads in the whole system.
    protected int numLocks;    
    // set numThreads in computeData_allTerminals() in NonTerminalZipVC.
    protected int numThreads;

    // Relevant data and structures used by the algorithm.
    protected HashMap<Integer, VectorClock> lastWriteClock;
    protected HashMap<Integer, HashMap<Integer, VectorClock>> lastReadClock;    // t -> x -> VC
    protected HashMap<Integer, VectorClock> firstWriteClock;
    protected HashMap<Integer, HashMap<Integer, VectorClock>> firstReadClock;   // t -> x -> VC

    protected ArrayList<VectorClock> lastReleases;
    protected ArrayList<VectorClock> firstAcquires;
    protected HashSet<Integer> locksAcquired;
    protected HashSet<Integer> locksAcquiredBeforeLastRead;
    protected HashSet<Integer> locksAcquiredBeforeLastWrite;
    protected HashSet<Integer> locksAcquiredBeforeFirstRead;
    protected HashSet<Integer> locksAcquiredBeforeFirstWrite;

    protected ArrayList<VectorClock> lastEvents;        // per thread
    protected ArrayList<VectorClock> lastForkEvents;    // per thread

    protected HashSet<Integer> threadsJoined;
    protected HashSet<Integer> threadsJoinedBeforeLastRead;
    protected HashSet<Integer> threadsJoinedBeforeLastWrite;
    protected HashSet<Integer> threadsJoinedBeforeFirstRead;
    protected HashSet<Integer> threadsJoinedBeforeFirstWrite;

    SymbolZipVC(String n) {
        super(n);
        this.name = n;
        init();
    }

    @Override
    protected void init() {
        numLocks = 0;
        numThreads = 0;
        
        lastReadClock = null;
        lastWriteClock = null;
        firstReadClock = null;
        firstWriteClock = null;
    }

}
