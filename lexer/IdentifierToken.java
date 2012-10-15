package lexer;

public class IdentifierToken extends Token {

    private String repr;

    public IdentifierToken ( String repr ) {
	this.repr = repr;
    }

    public String toString() {
	return "identifier("+repr+")";
    }
}
