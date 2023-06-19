package ziptrack.zip.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class ZipEngine {
    // For every symbol symb in the grammar, construct a 
	// map symb.parents : non-terminal -> integer
	// such that symb.parents(nt) = # of occurrences of symb 
	// in the unique production rule of nt.
	private static <
        T extends SymbolZip<T, U> & NonTerminalZip<T, U>,
        U extends SymbolZip<T, U> & TerminalZip
    > void assignParents(HashMap<String, U> terminalMap, HashMap<String, T> nonTerminalMap, SymbolZip<T, U> start){

        for (HashMap.Entry<String, U> entry : terminalMap.entrySet()){
            entry.getValue().parents = new HashMap<T, Integer> ();
        }
        for (HashMap.Entry<String, T> entry : nonTerminalMap.entrySet()){
            entry.getValue().parents = new HashMap<T, Integer> ();
        }
        for (HashMap.Entry<String, T> entry : nonTerminalMap.entrySet()){
            T nt = entry.getValue();
            for(SymbolZip<T, U> symb: nt.getRule()){
                if(!symb.parents.containsKey(nt)){
                    symb.parents.put(nt, 0);
                }
                int n = symb.parents.get(nt);
                symb.parents.put(nt, n+1);
            }
        }

        // If a terminal t is such that for every nt \in t.parents,
        // the production rule corresponding to nt consists only of terminal symbols,
        // we label t with the flag allParentsNative.
        for (HashMap.Entry<String, U> entry : terminalMap.entrySet()){
            U term = entry.getValue();
            boolean allParentsNative = true;
            for(T nt : term.parents.keySet()){
                if(!nt.getAllTerminals()){
                    allParentsNative = false;
                }
            }
            term.setAllParentsNative(allParentsNative);
        }
    }

    // For every non-terminal nt, this function assigns a 
    // set of symbols nt.criticalChildren such that
    //  s \in nt.criticalChildren iff nt has the largest topological index
    // amongst any parent of s. Here, by topological index, we mean
    // the index of a node in a fixed topological ordering of the underlying DAG of the grammar. 
    private static <
        T extends SymbolZip<T, U> & NonTerminalZip<T, U>,
        U extends SymbolZip<T, U> & TerminalZip
    > void assignCriticalChildren(ArrayList<? extends SymbolZip<T, U>> symbolList)
    {
        for(SymbolZip<T, U> symb : symbolList){
            if(symb instanceof NonTerminalZip<?, ?>) {
                ((NonTerminalZip<?, ?>) symb).createCriticalChildren();;
            }
        }
        for(SymbolZip<T, U> symb : symbolList){
            if(!symb.parents.isEmpty()){
                int max = -1;
                T maxParent = null;
                for(T p: symb.parents.keySet()){
                    int p_topoInd = p.topologicalIndex;
                    if(p_topoInd > max){
                        max = p_topoInd;
                        maxParent = p;
                    }
                }
                maxParent.addToCriticalChildren(symb);
            }
        }
    }

    private static <
        T extends SymbolZip<T, U> & NonTerminalZip<T, U>,
        U extends SymbolZip<T, U> & TerminalZip
    > void topoHelper(SymbolZip<T, U> curr, HashSet<SymbolZip<T, U>> visited, Stack<SymbolZip<T, U>> stack){
        visited.add(curr);
        if(curr instanceof NonTerminalZip<?, ?>){
            for(SymbolZip<T, U> symb: ((T) curr).getRule()){
                if(!visited.contains(symb)){
                    topoHelper(symb, visited, stack);
                }
            }
        }
        stack.push(curr);
    }

    // Return the topological ordering of the symbols in the grammar.
    private static <
        T extends SymbolZip<T, U> & NonTerminalZip<T, U>,
        U extends SymbolZip<T, U> & TerminalZip
    > ArrayList<SymbolZip<T, U>> getTopologicalOrder(T start)
    {
        HashSet<SymbolZip<T, U>> visited = new HashSet<SymbolZip<T, U>> ();

        Stack<SymbolZip<T, U>> stack = new Stack<SymbolZip<T, U>>();
        topoHelper(start, visited, stack);
        ArrayList<SymbolZip<T, U>> topoArray = new ArrayList<SymbolZip<T, U>> ();
        int topoInd = stack.size() - 1;
        SymbolZip<T, U> symb;
        while(!stack.empty()){
            symb = stack.pop();
            symb.topologicalIndex = topoInd;
            topoArray.add(symb);
            topoInd = topoInd - 1;
        }
        return topoArray;
    }

    private static <
        T extends SymbolZip<T, U> & NonTerminalZip<T, U>,
        U extends SymbolZip<T, U> & TerminalZip
    > ArrayList<SymbolZip<T, U>> initialAnalysis(ParseZip<T, U> parser, T start){
        ArrayList<SymbolZip<T, U>> inverseTopologicalSort = getTopologicalOrder(start);
        int totalSymbols = inverseTopologicalSort.size();

        assert(inverseTopologicalSort.get(totalSymbols-1) == start);

        for(int idx = totalSymbols-1; idx >= 0; idx --){
            inverseTopologicalSort.get(idx).countObjects();
        }

        assignParents(parser.terminalMap, parser.nonTerminalMap, start);
        assignCriticalChildren(inverseTopologicalSort);

        for(int idx = 0; idx < totalSymbols; idx ++){
            inverseTopologicalSort.get(idx).computeRelevantData();
        }

        for (HashMap.Entry<String, T> entry : parser.nonTerminalMap.entrySet())
        {
            entry.getValue().setIntermediateRelevantData();
        }

        for(int idx = 0; idx < totalSymbols; idx ++){
            inverseTopologicalSort.get(idx).deleteCounts();
        }

        return inverseTopologicalSort;
    }

    public static <
        T extends SymbolZip<T, U> & NonTerminalZip<T, U>,
        U extends SymbolZip<T, U> & TerminalZip
    > void analyze(String mapFile, String traceFile, boolean isHB) {
        // reflection would probably come in handy here
        ParseZip<T, U> parser = new ParseZip<>(isHB);
        parser.parse(mapFile, traceFile);

        // The "start" symbol in the context-free grammar.
        T start = parser.nonTerminalMap.get("0");

        // Sort the symbols in the grammar in the inverse topological ordering.
        ArrayList<SymbolZip<T, U>> inverseTopologicalSort = initialAnalysis(parser, start);
        int totalSymbols = inverseTopologicalSort.size();

        // Get the topological ordering.
        ArrayList<SymbolZip<T, U>> topologicalSort = new ArrayList<>();
        for (int idx = 0; idx < totalSymbols; idx++) {
            topologicalSort.add(inverseTopologicalSort.get(totalSymbols - idx - 1));
        }
        inverseTopologicalSort = null;
        parser = null;

        long startTimeAnalysis = System.currentTimeMillis();
        boolean race = false;
        //Analyze each of the symbols for races.
        for(int idx = 0; idx < totalSymbols; idx ++){
            SymbolZip<T, U> symb = topologicalSort.get(idx);
            if(symb instanceof TerminalZip){
                if (((TerminalZip) symb).getAllParentsNative()){
                    continue;
                }
            }
            // Analyze 'symb' for races.
            symb.computeData();
            race = symb.hasRace;
            // if(race){
            //     break;
            // }
        }
        long stopTimeAnalysis = System.currentTimeMillis();
        long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
        System.out.println("Time for compressed trace analysis = " + timeAnalysis + " miliseconds");

        if(race){
            System.out.println("Race detected");
        }
        else{
            System.out.println("Race free");
        }
    }
}
