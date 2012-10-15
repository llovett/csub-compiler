package generator;
import java.lang.Integer;
import java.util.*;
import analyzer.*;
import parser.*;

/** SymbolTableEntry
 *
 * Holds the following information:
 *
 * -For each symbol identifier, holds a negative offset from the $fp that will
 *  correspond to the location of the variable
 *  
 * -For each paramater, holds a postive offset from the $fp that will correspond
 *  to the location of the parameter
 *
 * -When we write our assembly code, we can just refer to the localVarsOffset and paramsOffset fields to determine how much offset we need
 * 
 */

public class SymbolTableEntry{
    //identifier --> offset from $fp
    private HashMap<String, Integer> localVars;
    private HashMap<String, Integer> params;

    public int paramsOffset;
    public int localVarsOffset;

    public SymbolTableEntry(){
	localVars = new HashMap<String, Integer>();
	params = new HashMap<String, Integer>();
	paramsOffset = 4;
	localVarsOffset = -8;
    }

    public HashMap<String, Integer> locals() {
	return localVars;
    }

    public HashMap<String, Integer> params() {
	return params;
    }

    public int size() {
	return localVars.size();
    }

    public Integer get( String name ) {
	Integer result;
	result = localVars.get( name );
	if ( null == result )
	    result = params.get( name );
	return result;
    }

    public void addParam( Symbol param ){

	params.put(param.getChild(1).data(), paramsOffset);
	paramsOffset += 4;

    }

    public void addVar( Symbol var ){

	//we've got a VAR_DEC here
	localVars.put(var.getChild(1).data(), localVarsOffset);

	String type = var.getAttribute("type");
	//Do we have an array?
	if( (type.compareTo("int*") == 0) || (type.compareTo("float*") == 0) || (type.compareTo("void*") == 0) ){
	    if(var.getAttribute("width") != null){
	    Integer width = Integer.parseInt(var.getAttribute("width"));
	    localVarsOffset -= width * 4;
	    }
	    else{
		localVarsOffset -= 4;
	    }

	}
	//We don't have an array
	else{
	    localVarsOffset -= 4;
	}
    }
    public void addTemp( Symbol temp){

	    localVars.put(temp.data(), localVarsOffset);
	    localVarsOffset -= 4;

    }

    public String toString(){
	StringBuilder ret = new StringBuilder();

	//Print params
	ret.append("  Params:\n");
	Set<String> ids = params.keySet();
	Collection<Integer> offsets = params.values();
	Iterator idIterator = ids.iterator();
	Iterator offsetIterator = offsets.iterator();
	while(idIterator.hasNext()){
	    String curid = (String)idIterator.next();
	    ret.append("    id: "+curid+" --> offset: "+offsetIterator.next()+"\n");
	}
	
	//Print localVars 
	ret.append("  Local Variables:\n");
	ids = localVars.keySet();
	offsets = localVars.values();
	idIterator = ids.iterator();
	offsetIterator = offsets.iterator();
	while(idIterator.hasNext())
	    ret.append("    id: "+idIterator.next()+" --> offset: "+offsetIterator.next()+"\n");

	return ret.toString();
    }
}

    
