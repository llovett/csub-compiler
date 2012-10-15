package parser;

import java.util.ArrayList;
import java.util.HashMap;

public class ParseTable extends HashMap<Nonterminal,HashMap<Terminal,Production>> implements ParseTableContents {
    static final long serialVersionUID = -3329144914320111636L;

    /******************************
     * ENTER THE PARSE TABLE HERE.
     *
     * VALID TOKEN TYPES:
     *
     * assignop
     * comment
     * keyword
     * indentifier
     * divmodop
     * integer
     * floatpoint
     * plusorminus
     * postfixop
     * relop
     * type
     * punctuation
     * *****************************/

    /**
     * TYPES OF SYMBOL CLASSES THAT ARE NOT EQUIVALENCE CLASSES
     * punctuation 
     * keyword
     */

    private HashMap<Symbol, Symbol> table;
    
    public ParseTable() {

	// Loop through each Terminal, get each of its possible
	// productions and put them in the table tableEntry is the
	// whole production we're representing
	for ( String[] tableEntry : TABLE_CONTENTS ) {

	    // productionSyms is the list of terminals and nonterminals
	    // that are the production
	    String[] productionSyms = tableEntry[2].split(" ");

	    // prodRHS is the symbol form of where the production is stored
	    ArrayList<Symbol> prodRHS = new ArrayList<Symbol>();

	    // for each symbol in the production, turn them into
	    // Terminals or Nonterminals
	    for ( String prodSym : productionSyms ) {

		// NONTERMINAL
		if ( prodSym.compareTo("A") >= 0 && prodSym.compareTo("Z") <= 0 ){
		    prodRHS.add( new Nonterminal( "", prodSym ) );
		}

		// TERMINAL
		else if ( ! prodSym.equals("") ) {
		    prodRHS.add( getTerminalFromInputToken( prodSym ) );
		}
	    }

	    Production prod = new Production( new Symbol( "", tableEntry[0] ),
					      prodRHS );

	    this.put( new Nonterminal( "", tableEntry[0] ),
		      getTerminalFromInputToken( tableEntry[1] ),
		      prod );
	}
    }
    
    /**
     * getTerminalFromInputToken(String inputToken)
     * @param inputToken - An input string 
     *
     * @return The Terminal representation of the input token
     *
     * For example:
     *   inputToken is ";"
     *   return symbol is (punctuation: ;)
     *   
     *   inputToken is while
     *   return symbol is (keyword: while)
     *
     *   inputToken is type
     *   return symbol is (type: )
     */
    public Terminal getTerminalFromInputToken( String inputToken ){
	    String data = "";
	    String entryType = inputToken;

	    //Here we verify whether entryType is a control statement
	    if( inputToken.equals("if") ||
		inputToken.equals("else") ||
		inputToken.equals("while") ||
		inputToken.equals("for") ||
		inputToken.equals("return") ){

		data = inputToken;
		entryType = "keyword";
	    }

	    // Special case for type "void" (since it can be used either
	    // as a type, or to specify that a function takes no parameters).
	    else if ( inputToken.equals("void") ) {
		entryType = "no-params";
		data = "void";
	    }

	    //Here we verify whether entryType is a punctuation symbol
	    else if ( ! ( inputToken.equals("identifier") ||
			  inputToken.equals("number") ||
			  inputToken.equals("integer") ||
			  inputToken.equals("floatpoint") ||
			  inputToken.equals("type") ||
			  inputToken.equals("relop") ||
			  inputToken.equals("assignop") ||
			  inputToken.equals("logicalop") ||
			  inputToken.equals("plusorminus") ||
			  inputToken.equals("divmodop") ||
			  inputToken.equals("postfixop") ) ) {

		//data contains the specific punctuation symbol
		data = inputToken;
		//entryType is the type of symbol
		entryType = "punctuation";
	    }

	    return new Terminal(data, entryType);
    }

    /**
     * put( Symbol X, Symbol a, Production prod )
     *
     * Associate a production 'prod' with a nonterminal and input symbol.
     * 
     * @param X - A nonterminal
     * @param a - An input symbol (from the lexer)
     * @param prod - A production X --> Y0, Y1, Y2 ... Yk
     *
     * @return The production previously associated with X and a, if any.
     * */
    public Production put( Nonterminal X, Terminal a, Production prod ) {
	//check to see if the row for X already exisits
	HashMap<Terminal,Production> tableRow = get( X );
	if ( null != tableRow ) {
	    //if it does, add the new production to it
	    return get( X ).put( a, prod );
	}

	//otherwise, make a new row for X
	HashMap<Terminal,Production> newRow = new HashMap<Terminal,Production>();
	//add the production to it
	newRow.put( a, prod );
	put( X, newRow );

	return null;
    }

    /**
     * get( Symbol X, Symbol a )
     *
     * Get the production associated with a nonterminal and input symbol.
     * 
     * @param X - A nonterminal
     * @param a - An input symbol (from the lexer)
     *
     * @return - The Production (if any) associated with X and a in the parse table.
     * If there is no such production, returns null.
     * */
    public Production get( Nonterminal X, Terminal a ) {
	if ( ! (X instanceof Nonterminal) )
	    return null;

	HashMap<Terminal,Production> tableRow = get( X );
	if ( null != tableRow )
	    return get( X ).get( a );

	return null;
    }
    
    /**
     * matchingTokensFor( N )
     *
     * @param N - A Nonterminal.
     * 
     * @return - An array of Terminals that contains possible token (types)
     * that would allow us to match N and the returned Terminal with a
     * Production. This method can be used for giving some useful information
     * to the user about errors.
     * */
    public ArrayList<Terminal> matchingTokensFor( Nonterminal N ) {
	ArrayList<Terminal> ret = new ArrayList<Terminal>();

	HashMap<Terminal,Production> row = get( N );
	if ( null == row )
	    return ret;

	ret.addAll( row.keySet() );
	return ret;
    }

    /**
     * contains( Production p )
     *
     * @return - true if the Production P is anywhere in the table.
     * */
    public boolean contains( Production p ) {
	for ( HashMap<Terminal,Production> tableRow : values() )
	    for ( Production prod : tableRow.values() )
		if ( prod.equals( p ) )
		    return true;

	return false;
    }

    /**
     * contains( Symbol LHS, Symbol[] RHS )
     *
     * A more convenient way of telling whether or not a Production with a given
     * LHS and RHS is in the parse table.
     * */
    public boolean contains( Symbol LHS, Symbol[] RHS ) {
	ArrayList<Symbol> lRHS = new ArrayList<Symbol>();
	for ( Symbol s : RHS ) lRHS.add( s );
	return contains( new Production( LHS, lRHS ) );
    }

    public String toString () {
	StringBuilder ret = new StringBuilder("<ParseTable>\n");
	
	for ( Symbol nonterm : keySet() )
	    for ( Terminal t : get( nonterm ).keySet() ) {
		ret.append(String.format("[%s,%s] has %s", nonterm, t,
					 get( nonterm ).get( t ).toString()));
		ret.append('\n');
	    }

	return ret.toString();
    }
}
