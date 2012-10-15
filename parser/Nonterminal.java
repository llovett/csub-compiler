package parser;

import java.util.*;

public class Nonterminal extends Symbol {
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
    public Nonterminal( String repr, String type ) {
	super( repr, type );
	this.repr = repr;
	this.type = type;
	children = new ArrayList<Symbol>();
    }

    /**
     * matches( Symbol other )
     *
     * @param other - The Symbol to match against.
     * 
     * @return - Whether the other symbol is an instance
     * of this Symbol's type (i.e., this Symbol can be attached
     * as the single child of a given Symbol's syntax node
     * representation).
     * */
    public boolean matches( Terminal other ) {
	/**
	 * a nonterminal N matches a terminal T
	 * if there exists some production P such that:
	 *
	 * N --> R
	 *
	 * where R and T are of the same terminal type. 
	 * */
	return Parser.getParseTable().contains( this, new Terminal[]{ other } );
    } 

    /**
     * print()
     *
     * A helper method for toString()
     * */
    public String print( int indent ) {
	String TAB = "  ";
	StringBuilder prefix = new StringBuilder( "\n" );
	for ( int i = indent; i>0; i-- )
	    prefix.append(TAB);

	StringBuilder ret = new StringBuilder( "("+type() );
	// Check for attributes
	if ( attrs.size() > 0 ) {
	    ret.append( "\t\t--------------- { " );
	    for ( Map.Entry<String,String> entry : attrs.entrySet() ) {
		ret.append( String.format( "%s=%s, ",
					   entry.getKey(),
					   entry.getValue() ) );
	    }
	    // Remove final ', '
	    ret.delete( ret.length()-2, ret.length() );
	    ret.append( " }" );
	}

	for ( Symbol child : children )
	    if ( child instanceof Nonterminal )
		ret.append( prefix + ((Nonterminal)child).print( indent+1 ) );
	    else
		ret.append( prefix + child.toString() );
	ret.append( ")" );
	
	return ret.toString();
    }


    /**
     * copy()
     *
     * A mechanism for copying a Nonterminal. Not broken like clone().
     * NEVER use clone().
     * */
    public Nonterminal copy() {
	return new Nonterminal( this.repr, this.type );
    }

    /**
     * toString()
     *
     * @return - A String that represents the entire syntax tree rooted
     * at this Symbol.
     * */
    public String toString() {
	return print( 1 );
    }

    public int hashCode() {
	return type().hashCode();
    }
}
