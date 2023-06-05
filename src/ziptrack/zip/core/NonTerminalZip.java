package ziptrack.zip.core;

import java.util.ArrayList;

public interface NonTerminalZip<
    T extends SymbolZip<T, U> & NonTerminalZip<T, U>,
    U extends SymbolZip<T, U> & TerminalZip
> {

    public void createCriticalChildren();

    public void addToCriticalChildren(SymbolZip<T, U> symb);

    public ArrayList<? extends SymbolZip<T, U>> getRule();

    public void setRule(ArrayList<? extends SymbolZip<T, U>> r);
    
    public boolean getAllTerminals();

    public void setAllTerminals(boolean b);

    public void setIntermediateRelevantData();
}
