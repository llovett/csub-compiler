package lexer;

public class InvalidToken extends Token {

    private String repr;

    public InvalidToken ( String repr ) throws lexer.InvalidTokenException {
	this.repr = repr;
	throw new InvalidTokenException("Encountered an invalid token: "+repr );
    }

    public String toString() {
	return repr;
    }

}
