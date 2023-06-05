package ziptrack.zip.vc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import ziptrack.zip.core.NonTerminalZip;
import ziptrack.zip.core.SymbolZip;

public class NonTerminalZipVC extends SymbolZipVC implements NonTerminalZip<NonTerminalZipVC, TerminalZipVC> {
    protected ArrayList<SymbolZipVC> rule;
	public boolean allTerminals; //on RHS

    public HashSet<SymbolZipVC> criticalChildren;

    public NonTerminalZipVC(String name) {
        super(name);
        this.rule = null;
        this.criticalChildren = null;
    }

    public NonTerminalZipVC(String name, ArrayList<SymbolZipVC> r) {
        super(name);
        this.rule = r;
        this.criticalChildren = null;
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
		for(SymbolZipVC symb: this.rule){
			for (HashMap.Entry<Integer, Integer> entry : symb.threadOrLockCount.entrySet()){
				int u = entry.getKey();
				int u_cnt = 0;
				if(this.threadOrLockCount.containsKey(u)){
					u_cnt = this.threadOrLockCount.get(u);
				}
				this.threadOrLockCount.put(u,  u_cnt + 1);
			}
		}
	}

    // TODO: implement this
    @Override
    public void setIntermediateRelevantData() {

    }

    // TODO: implement this too
    @Override
    public void computeData() {

    }
}
