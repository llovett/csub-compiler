package lexer;

public class RelationalOperatorToken extends Token {

    private String repr;

    public RelationalOperatorToken( String repr ) {
	/* repr is inherited from Token */
	this.repr = repr;
    }

    public String toString() {
	return "relop("+repr+")";
    }
}