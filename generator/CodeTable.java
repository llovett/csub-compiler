package generator;
import java.io.*;
import java.util.*;
import analyzer.*;
import parser.*;

public class CodeTable implements Iterable<CodeEntry> {

    private HashMap<String, Integer> labelMap;	// Mapping of label names to line numbers
    private int lastLabelNum;			// Last number used to generate a label
    private List<CodeEntry> theTable;		// Consecutive lines of 3-address code
    private int curIndex;			// Current line of code to be generated

    public CodeTable(){
	labelMap = new HashMap<String, Integer>();
	theTable = new ArrayList<CodeEntry>();
	curIndex = 0;
	lastLabelNum = 0;

    }

    /** Emits 3-address code for a given operator and two exprs (which are
     * places in the code table) Puts the code in the next slot in the
     * codeTable
     *
     */
    public void add( CodeEntry entry ){
	theTable.add( entry );
	curIndex++;
    }

    public void add( Collection<CodeEntry> entries ) {
	theTable.addAll( entries );
    }
    
    /**
     * getIndex()
     *
     * @return the current line number in the codeTable.
     */
    public int getIndex() {
	return curIndex;
    }

    /**
     * get( int lineNo )
     *
     * @param lineNo Line number to fetch
     * @return 4-tuple representing the line of code.
     * */
    public CodeEntry get( int lineNo ) {
	return theTable.get( lineNo );
    }

    // /**
    //  * addLabel( String labelName )
    //  *
    //  * @param labelName Name of the label.
    //  * */
    // public void addLabel( String labelName ) {
    // 	labelMap.put( labelName, curIndex );
    // }
    
    // /**
    //  * addLabel
    //  *
    //  * @return String representation of the label (automatically assigned).
    //  * */
    // public String addLabel() {
    // 	String labelName = "L"+(lastLabelNum++);
    // 	labelMap.put( labelName, curIndex );
    // 	return labelName;
    // }

    /**
     * labelFor( String labelName )
     *
     * @param labelName Name of the label.
     * @return Integer that the label points to.
     * */
    public int labelFor( String labelName ) {
	return labelMap.get( labelName );
    }

    public Iterator<CodeEntry> iterator() {
	return theTable.iterator();
    }

    public int size() {
	return theTable.size();
    }

    public String toString() {
	StringBuilder ret = new StringBuilder();
	ret.append("=======================================================\n");
	ret.append("3-ADDRESS CODE ========================================\n");
	ret.append("=======================================================\n");
	ret.append("#\tOP\tRESULT\tARG1\tARG2\n");
	for ( int i=0; i<theTable.size(); i++ ) {
	    CodeEntry line = get(i);
	    ret.append(""+i+"\t");
	    ret.append(line.toString());
	    ret.append("\n");
	}
	return ret.toString();
    }

}
