package lexer;

public class PostfixOperatorToken extends Token {

    private String repr;

    public PostfixOperatorToken( String repr ) {
	this.repr = repr;
    }

    public String toString() {
	return "postfixop("+repr+")";
    }
}
