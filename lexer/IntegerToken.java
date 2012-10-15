package lexer;

public class IntegerToken extends NumberToken {

    public static final int INTEGER_CONSTANT = 0;
    public static final int HEX_CONSTANT = 1;

    private String repr;
    private int type;

    public IntegerToken(String repr) throws lexer.InvalidTokenException {
	super( repr );
	this.repr = repr;

	if (repr.length() > 2 && (repr.substring(0,2).equals("0x") ||
				  repr.substring(0,2).equals("0X"))) {
	    type = HEX_CONSTANT;
	}
	else
	    type = INTEGER_CONSTANT;
    }

    public String toString() {
	return "integer("+repr+")";
    }
}
