package lexer;

public class PlusOrMinusToken extends Token {

    private String repr;

    public PlusOrMinusToken ( String repr ) {
	this.repr = repr;
    }

    public int getSign() {
	if ( repr.equals("-"))
	    return -1;
	return 1;
    }

    public String toString() {
	return "plusorminus("+repr+")";
    }
}
