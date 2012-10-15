package lexer;

public class FloatToken extends NumberToken {

    public static final int REAL_CONSTANT = 2;

    private String repr;
    private int type;

    public FloatToken(String repr) throws lexer.InvalidTokenException {
	super( repr );
	this.repr = repr;

	if (-1 != repr.indexOf('.') ||
		 -1 != repr.indexOf('e') ||
		 -1 != repr.indexOf('E'))
	    type = REAL_CONSTANT;
    }

    public String toString() {
	return "floatpoint("+repr+")";
    }
}
