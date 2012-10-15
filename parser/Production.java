package parser;

import java.util.*;

public class Production {

    private Symbol LHS;
    private List<Symbol> RHS;

    public Production( Symbol LHS, List<Symbol> RHS ) {
	this.LHS = LHS;
	this.RHS = RHS;
    }

    public Symbol getLHS() {
	return LHS;
    }

    public List<Symbol> getRHS() {
	return RHS;
    }

    public boolean equals( Object o ) {
	if (! ( o instanceof Production ) ) return false;
	
	Production p = (Production)o;
	if ( getRHS().size() != p.getRHS().size() ) return false;

	for ( int i = 0; i < getRHS().size(); i++ ) {
	    Symbol myS = getRHS().get( i );
	    Symbol otS = p.getRHS().get( i );
	    if ( ! myS.equals( otS ) ) return false;
	}

	return getLHS().equals( p.getLHS() );
    }

    public String toString() {
	String ret = "Production"+LHS.toString()+" --> ";
	for ( Symbol s : getRHS() )
	    ret += s.toString()+ " ";
	return ret;
    }

}
