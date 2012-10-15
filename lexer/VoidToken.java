package lexer;

public class VoidToken extends Token {

    private String repr;

    public VoidToken ( String repr ) {
	this.repr = "(void)";
    }

    public String toString() {
	return "no-params("+repr+")";
    }
}
