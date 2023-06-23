package ziptrack.zip.vc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

// This functions as an external wrapper for handling data that should be globally known relative
// to individual symbols that cannot otherwise generate such data.
public class ZipVCState {

    // Internal data
	protected HashMap<Integer, Integer> threadToIndex;
	protected HashSet<Integer> performerThreads;
	protected HashMap<Integer, Integer> lockToIndex;
	protected int numThreads;
	protected int numLocks;

    public ZipVCState(HashSet<Integer> tSet) {

    }

    protected void initInternalData(HashSet<Integer> tSet) {
        this.threadToIndex = new HashMap<Integer, Integer>();
        this.performerThreads = new HashSet<Integer> ();
        this.numThreads = 0;
        Iterator<Integer> tIter = tSet.iterator();
        while (tIter.hasNext()) {
            int thread = tIter.next();
            this.threadToIndex.put(thread, (Integer) this.numThreads);
            this.numThreads ++;
        }
        this.lockToIndex = new HashMap<Integer, Integer>();
        this.numLocks = 0;
	}
}
