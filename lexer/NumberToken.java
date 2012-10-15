package lexer;

public class NumberToken extends Token {

    public static final int INTEGER_CONSTANT = 0;
    public static final int HEX_CONSTANT = 1;
    public static final int REAL_CONSTANT = 2;

    private String repr;
    private int type;

    public NumberToken(String repr) throws lexer.InvalidTokenException {
	this.repr = repr;

	if (repr.length() > 2 && (repr.substring(0,2).equals("0x") ||
				  repr.substring(0,2).equals("0X"))) {
	    type = HEX_CONSTANT;
	}
	else if (-1 != repr.indexOf('.') ||
		 -1 != repr.indexOf('e') ||
		 -1 != repr.indexOf('E'))
	    type = REAL_CONSTANT;
	else
	    type = INTEGER_CONSTANT;
    }

    public void validate( Token t ) throws lexer.InvalidTokenException {
	long longval = 0;
	double flval = 0;

	/* TRY TO GET THE NUMERICAL VALUE */
	try {
	    if ( HEX_CONSTANT == getType() )
		// Parse as hex number. Gets rid of the "0x" part
		longval = Long.parseLong( repr.substring(2) , 16 );
	    else if ( INTEGER_CONSTANT == getType() )
		// Parse as decimal value
		longval = Long.parseLong( repr );
	    else if ( REAL_CONSTANT == getType() )
		// Parse as a float value
		flval = Double.parseDouble( repr );
	}

	/* NOT A VALID NUMBER AT ALL */
	catch ( NumberFormatException e ) {
	    throw new lexer.InvalidTokenException("Not a valid number: "+repr );
	}

	/* CHECK SIGN (for bounds testing) */
	if ( null != t && t instanceof PlusOrMinusToken ) {
	    longval *= ((PlusOrMinusToken)t).getSign();
	    flval *= ((PlusOrMinusToken)t).getSign();
	}

	/* BOUNDS TESTING (float values) */
	if ( REAL_CONSTANT == getType() ) {
	    if ( (flval > 0 && flval < Float.MIN_VALUE) ||
		 flval > Float.MAX_VALUE ||
		 (flval < 0 && -flval > Float.MAX_VALUE) )
		throw new lexer.InvalidTokenException("Number unrepresentable: "+repr+" ("+flval+")" );
}

	/* BOUNDS TESTING (integer values) */
	else  {
	    if ( longval < Integer.MIN_VALUE || longval > Integer.MAX_VALUE )
		throw new lexer.InvalidTokenException("Number unrepresentable: "+repr+" ("+longval+")" );
	}
    }

    public int getType() {
	return type;
    }

    public String toString() {
	return "number("+repr+")";
    }
}
