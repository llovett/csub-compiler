package parser;

import java.util.*;

public class Terminal extends Symbol {

    // Terminal types
    public static final int TERMINAL_PUNCTUATION = 0;
    public static final int TERMINAL_TYPEDEC = 1;
    public static final int TERMINAL_FLOAT = 2;
    public static final int TERMINAL_INTEGER = 3;
    public static final int TERMINAL_IDENTIFIER = 4;
    public static final int TERMINAL_KEYWORD = 5;

    // Class members
    private int termType;			// Integer declaring the type of this Terminal.

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
    public Terminal( String repr, String type ) {
	super( repr, type );

	// TODO: is this REALLY necessary?
	// Get the integer form of this terminal type. Must be hard-coded.
	if ( type().equals("int") ||
	     type().equals("float") ||
	     type().equals("void") ||
	     type().equals("type") )
	    termType = TERMINAL_TYPEDEC;

	else if ( type().equals("punctuation") )
	    termType = TERMINAL_PUNCTUATION;

	else if ( type().equals("floatpoint") )
	    termType = TERMINAL_FLOAT;

	else if ( type().equals("integer") )
	    termType = TERMINAL_INTEGER;

	else if ( type().equals("identifier") )
	    termType = TERMINAL_IDENTIFIER;
    
    }

    public boolean matches( Terminal other ) {
	if( ! type.equals(other.type()) ){
	    return false;
	}
	else if ( type.equals("punctuation") || type.equals("keyword") ) {
	    return repr.equals( other.data() );
	}

	return true;
    }

    /**
     * copy()
     *
     * A mechanism for copying a Terminal. Not broken like clone().
     * NEVER use clone().
     * */
    public Terminal copy() {
	Terminal dup = new Terminal( this.repr, this.type );
	dup.setContext( this.getLine(), this.getLineNumber(), this.getErrorColumn() );

	return dup;
    }

    public boolean equals( Object other ) {
	if (! ( other instanceof Terminal ) )
	    return false;

	Terminal t = (Terminal)other;
	if (! ( t.type().equals( type() ) ) )
	    return false;

	if ( type().equals("punctuation") ||
	     type().equals("keyword") )
	    return t.data().equals( data() );

	// Only type has to match for "type" or "number"
	return true;
    }

    public int hashCode() {
	int hc = 0;
	if ( type().equals("punctuation") ||
	     type().equals("keyword") )
	    hc = data().hashCode();
	else
	    hc = type().hashCode();

	return hc;
    }

    /**
     * simpleString()
     *
     * Returns an easy-to-read representation of this Terminal.
     * Used in printing errors.
     * */
    public String simpleString() {
	if ( type().equals("integer") || type().equals("floatpoint") )
	    return "number";
	else if ( type().equals("plusorminus") )
	    return String.format("\'%c\'/\'%c\'",'+','-');
	else if ( type().equals("divmodop") )
	    return String.format("\'%c\'/\'%c\'",'/','%');
	else if ( type().equals("postfixop") )
	    return String.format("\"%s\"/\"%s\"","++","--");
	else if ( type().equals("relop") )
	    return "relational operator";
	else if ( type().equals("while") || type().equals("for") ||
		  type().equals("if") )
	    return "statement";
	else if ( type().equals("return") )
	    return "return";
	else if ( type().equals("assignop") )
	    return "assignment expression";
	else if ( type().equals("identifier") )
	    return "identifier";

	return String.format( "\'%s\'", data() );
    }

    /**
     * toString()
     *
     * @return - A String that represents the entire syntax tree rooted
     * at this Symbol.
     * */
    public String toString() {
	String dat = repr;
	if ( repr.equals("(") )
	    dat = "leftparen";
	else if ( repr.equals(")") )
	    dat = "rightparen";
	else
	    dat = String.format("\"%s\"",repr);
	return String.format("<%s : %s>", type, dat );
    }

}
