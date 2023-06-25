package ziptrack.zip.vc;

import java.util.HashMap;
import java.util.HashSet;

import ziptrack.util.VectorClock;

import ziptrack.zip.core.SymbolZip;

public abstract class SymbolZipVC extends SymbolZip<NonTerminalZipVC, TerminalZipVC> {
    protected String name;

    // set numThreads in computeData_allTerminals() in NonTerminalZipVC.

    // Relevant data and structures used by the algorithm.
    protected HashMap<Integer, VectorClock> lastWriteClocks;
    protected HashMap<Integer, HashMap<Integer, VectorClock>> lastReadClocks;    // x -> t -> VC
    protected HashMap<Integer, VectorClock> firstWriteClocks;
    protected HashMap<Integer, HashMap<Integer, VectorClock>> firstReadClocks;   // x -> t -> VC

    protected HashMap<Integer, VectorClock> lastReleases;
    protected HashMap<Integer, VectorClock> firstAcquires;
    protected HashSet<Integer> locksAcquired;
    protected HashMap<Integer, HashSet<Integer>> locksAcquiredBeforeLastWrite;
    protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> locksAcquiredBeforeLastRead;
    protected HashMap<Integer, HashSet<Integer>> locksAcquiredBeforeFirstWrite;
    protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> locksAcquiredBeforeFirstRead;

    protected HashMap<Integer, VectorClock> lastEvents;
    protected HashMap<Integer, VectorClock> lastForkEvents;

    protected HashSet<Integer> threadsJoined;
    protected HashMap<Integer, HashSet<Integer>> threadsJoinedBeforeLastWrite;
    protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> threadsJoinedBeforeLastRead;
    protected HashMap<Integer, HashSet<Integer>> threadsJoinedBeforeFirstWrite;
    protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> threadsJoinedBeforeFirstRead;

    SymbolZipVC(String n) {
        super(n);
        this.name = n;
        init();
    }

    @Override
    protected void init() {
        lastReadClocks = new HashMap<>();
        lastWriteClocks = new HashMap<>();
        firstReadClocks = new HashMap<>();
        firstWriteClocks = new HashMap<>();

        locksAcquired = new HashSet<>();
        locksAcquiredBeforeLastWrite = new HashMap<>();
        locksAcquiredBeforeLastRead = new HashMap<>();
        locksAcquiredBeforeFirstWrite = new HashMap<>();
        locksAcquiredBeforeFirstRead = new HashMap<>();

        threadsJoined = new HashSet<>();
        threadsJoinedBeforeLastWrite = new HashMap<>();
        threadsJoinedBeforeLastRead = new HashMap<>();
        threadsJoinedBeforeFirstWrite = new HashMap<>();
        threadsJoinedBeforeFirstRead = new HashMap<>();

        hasRace = false;
    }

}
