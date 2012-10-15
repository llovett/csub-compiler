package parser;

import java.util.*;

public class Symbol {
  
    // Class members
    protected String repr;			// String representation of this particular Symbol
    protected List<Symbol> children;		// Children of this Symbol in a syntax tree
    protected String type;			// String for the tyep of the Symbol, like "nonterminal" or "if-stmt"
    protected HashMap<String,String> attrs;	// Attributes, used in syntax-directed translation (semantic analysis)
    protected HashMap<String,Integer> localvars;// Used in the code generator. Maps symbolic variable names to
						// offsets from current frame pointer.
    public Symbol tag = null;			// Reference to a Symbol that can be used to give more context
						// information for this Symbol

    public String line, errorColumn;
    public int lineNumber;
    public String place;			// Used in the code generator.
    
    /**
     * CONSTRUCTOR
     *
     * Symbol( String repr )
     *
     * Creates a new Symbol with the given String representation.
     * This representation should probably contain any essential data
     * we need to remember with this Symbol.
     *
     * @param repr - A String representation of this Symbol. This will also
     * be used to "match" two symbols together (i.e., matching a symbol in a production
     * to something we got off the input stream from the lexer). If left blank,
     * then this Symbol will be assumed to be a nonterminal.
     * @param type - Type of this Symbol. Could be something like "if-stmt" or "id".
     * */
    public Symbol( String repr, String type ) {
	this.repr = repr;
	this.type = type;

	// Initialize children
	children = new ArrayList<Symbol>();
	
	// Initialize attributes to be used in a syntax-directed translation
	attrs = new HashMap<String,String>();

	// Initialize local variable map
	localvars = new HashMap<String,Integer>();
    }

    /**
     * setContext( line, lineNumber, columnNumber )
     *
     * Set the context for errors for this Symbol. This is copied over
     * from a Token in the Lexer.
     * */
    public void setContext( String line, int lineNumber, String errorColumn ) {
	this.line = line;
	this.lineNumber = lineNumber;
	this.errorColumn = errorColumn;
    }

    public String getLine() {
	return line;
    }

    public int getLineNumber() {
	return lineNumber;
    }

    public String getErrorColumn() {
	return errorColumn;
    }

    /**
     * add( child )
     *
     * @param child - A Symbol that will become one of this Symbol's children
     * in a syntax tree.
     * */
    public void add( Symbol child ) {
	children.add( child );
    }

    /**
     * insert( index, child )
     *
     * @param index - Index at which the child belongs. 
     * @param child - A Symbol that will become one of this Symbol's children
     * in a syntax tree.
     * */
    public void add( int index, Symbol child ) {
	children.add( index, child );
    }

    /**
     * getChildren()
     *
     * @return - A List of Symbols that constitute this Symbol's children
     * in a syntax tree.
     * */
    public List<Symbol> getChildren() {
	return children;
    }

    public Symbol getChild( int which ) {
	return children.get( which );
    }

    public void setChildren(List<Symbol> children) {
	this.children = children;
    }

    public String type() {
	return type;
    }

    public boolean checkType( String type ) {
	return type().equals( type );
    }

    public void setType(String type) {
	this.type = type;
    }

    /**
     * data()
     *
     * @return - The String of data stored in this Symbol
     * (i.e., its String representation.
     * */
    public String data() {
	return repr;
    }

    public void setData( String data ) {
	this.repr = data;
    }

    public boolean checkData( String data ) {
	return data().equals( data );
    }

    /**
     * setAttribute( name, value )
     *
     * @param name - Name of attribute to set.
     * @param value - Value of the attribute specified.
     *
     * @return - The old value associated with the attribute. null of no previous
     * value was specified.
     * */
    public String setAttribute( String name, String value ) {
	return attrs.put( name, value );
    }
    
    /**
     * getAttribute( name )
     *
     * @return - String value associated with given named attribute.
     * */
    public String getAttribute( String name ) {
	return attrs.get( name );
    }

    public HashMap<String,String> getAttributes() {
	return attrs;
    }

    public void addLocalVariable( String name, int offset ) {
	localvars.put( name, offset );
    }

    public int getOffset( String varname ) {
	return localvars.get( varname );
    }

    /**
     * prune()
     *
     * @return - An abstract syntax tree with less junk in it
     * than the parse tree itself, which includes ALL Nonterminals.
     *
     * Gross.
     *
     * What Nonterminals are "useless" is determined by the String
     * array at the top of Parser.java
     *
     * WHAT ALL DOES PRUNE DO:
     * 1. Removes USELESS_TERMINALS from our parse tree
     * 2. Removes Nonterminals with no children, unless it is a function call with no arguments 
     * 3. Remove Nonterminals with only one child, pass up the child to parent Nonterminal
     * 4. Combines type and * to make a "pointer type", ex. type: int and punctuation: * get combined to type:int*
     * 5. Reformats VAR-DECs and FUN_DECs from DECs.
     *
     * Productions to clean up:
     * 1. FUN-DEC - done
     * 2. VAR-DEC - done
     * 3. CALL
     * 4. EXPR?
     * 5. PARAMS
     * */
    public ArrayList<Symbol> prune() {

	ArrayList<Symbol> ret = new ArrayList<Symbol>();

	if ( this instanceof Terminal ) {
	    boolean useless = false;
	    for ( String s : Parser.USELESS_TERMINALS )
		if ( s.equals( data() ) ) useless = true;

	    if ( useless )
		return ret;

	    Symbol clone = this.copy();

	    ret.add( this.copy() );
	    return ret;
	}

	

	// Prune our children.
	ArrayList<Symbol> prunedChildren = new ArrayList<Symbol>();
	for ( int i = 0; i<children.size(); i++){
	    Symbol child = children.get(i);
	    prunedChildren.addAll( child.prune() );
	}
	
	//Combine type and * to make a "pointer type"
	for ( int i = 0; i<prunedChildren.size(); i++){
	    Symbol child = prunedChildren.get(i);
	    if(child.type().equals("type") && prunedChildren.get(i+1).data().equals("*")){
		child.setData(child.data()+"*");
		prunedChildren.remove(i+1);
	    }
	}

	// AD-HOC FIX: try to associate function names/types with a FUN-DEC,
	// rather than dangling as Terminal children inside the DEC node.
	// Also try to make all variable declarations part of a VAR-DEC node.
	// This includes global variable declarations, vardecs within funcs,
	// params, and args. (some of this stuff TODO).
	if ( type().equals( "DEC" ) ) {

	    // At this point remember: all our children are pruned already.
	    for ( int i=0; i<prunedChildren.size(); i++ ) {
	    	Symbol child = prunedChildren.get( i );
	    	// 
	    	// CASE 1: DEC --> FUN-DEC
	    	// 
	    	if ( child.type().equals( "FUN-DEC" ) ) {
	    	    child.add( 0, prunedChildren.remove( i-2 ) );	// Add the type
	    	    child.add( 1, prunedChildren.remove( i-2 ) );	// Add function name
	    	}
	    }
	    for ( int i=0; i<prunedChildren.size(); i++ ) {
	    	Symbol child = prunedChildren.get( i );
	    	// 
	    	// CASE 2: DEC --> type (*) identifier ([ integer ]) ;
	    	// 
	    	if ( type().equals( "DEC" ) && !child.type().equals( "FUN-DEC" ) ) {
	    	    Nonterminal varDec = new Nonterminal( "VAR-DEC", "VAR-DEC" );
	    	    while ( prunedChildren.size() > 0 )
	    		varDec.add( prunedChildren.remove( i ) );
	    	    prunedChildren.add( varDec );
	    	}
	    }
	}
	// AD-HOC FIX: Move around information in PARAM so that the first
	// child is always the abbreviated type (type including *, if it's
	// there), and the second child is the identifier's name.
	else if ( type().equals( "PARAM*" ) ) {
	    for ( int i=0; i<prunedChildren.size(); i++ ) {
		Symbol child = prunedChildren.get( i );
		if ( child.data().equals("[") || child.data().equals("]") )
		    continue;
		ret.add( prunedChildren.remove( i-- ) );	// This is SUPER jank. Hahahah!
	    }
	}
	// AD-HOC FIX: Move around information in CALL so that identifiers
	// appear inside of the CALL node, rather than before.
	else if ( type().equals("PRIMARY-EXPR" ) ) {
	    for ( int i=0; i<prunedChildren.size(); i++ ) {
		Symbol child = prunedChildren.get( i );
		if ( child.type().equals("identifier") ) {
		    //Is the next child a CALL?
		    if ( (i+1 < prunedChildren.size()) && prunedChildren.get( i+1 ).type().equals("CALL") ) {
			prunedChildren.get( i+1 ).add(0, prunedChildren.remove(i));
		    }
		}
	    }
	}
	// AD-HOC FIX: Move around information in ARGS so that it more closely
	// resembles the structure of VAR-DEC, FUN-DEC, and PARAMS
	else if (type().equals("ARGS") ) {
	    for ( int i=0; i<prunedChildren.size(); i++ ) {
		Symbol child = prunedChildren.get( i );
		if(child.type().equals("POSTFIX-EXPR")) {
		    // TODO??
		}
	    }
	}
	// AD-HOC FIX: Move around information in EXPR and EXPR* so that it is
	// clearer what assignment operation we're dealing with. Note that by
	// the nature of our grammar, the LHS of the assignop will always be a
	// single node.
	else if ( type().equals( "EXPR" ) ) {
	    if ( prunedChildren.size() >= 2 && prunedChildren.get( 1 ).type().equals( "ASSIGN-EXPR*" ) ) {
		// Move LHS of EXPR* assignment operation into EXPR*,
		// and give EXPR* a more appropriate name.
		Symbol exprStar = prunedChildren.get( 1 );
		exprStar.add( 0, prunedChildren.remove( 0 ) );
		exprStar.setType( "ASSIGN-EXPR" );
	    }
	}
	else if ( type().equals( "EXPR*" ) ) {
	    setType( "ASSIGN-EXPR*" );	// I mean, WTF is an EXPR*, anways?
	}

	// Nonterminal with no children = useless, automatically, Unless it's a call to a funtion with no arguments??
	//if ( prunedChildren.size() == 0 )
	if ( prunedChildren.size() == 0 && !type().equals("ARGS") && !type().equals("OPTIONAL-EXPR"))
	    return ret;

	// If after pruning we have only 1 child, then just pass
	// up the child and omit this node. We want to keep the CALL and ARGS nodes though
	if ( prunedChildren.size() == 1 &&
	     !type().equals("ARGS") &&
	     !type().equals("PARAMS") &&
	     !type().equals("CALL")  &&
	     !type().equals("FUN-DEC")  && //Added FUN-DEC here...not sure why this needs to be here, but it fixes things...
	     !type().equals("PROGRAM") ) {

	    return prunedChildren;
	}

	// Check to see if we are useless
	boolean useless = true;
	for ( String s : Parser.USEFUL_NONTERMINALS )
	    if ( s.equals( type() ) ) useless = false;

	if ( useless ) {
	    return prunedChildren;
	} else {
	    Symbol newSym = this.copy();
	    newSym.setChildren( prunedChildren );
	    ret.add( newSym );
	    return ret;
	}
    }


    /**
     * copy()
     *
     * A mechanism for copying a Symbol. Not broken like clone().
     * NEVER use clone().
     * */
    public Symbol copy() {
	Symbol dup = new Symbol( this.repr, this.type );
	dup.setContext( this.getLine(), this.getLineNumber(), this.getErrorColumn() );

	return dup;
    }

    public boolean equals( Object other ) {
	if ( ! ( other instanceof Symbol ) )
	    return false;
	Symbol sym = (Symbol)other;
	return ( sym.type().equals( this.type() )
		 && sym.data().equals( this.data() ) );
    }

    public String toString() {
	return String.format("( %s )", type );
    }
}
