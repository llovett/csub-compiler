package lexer;

public class AssignmentOperatorToken extends Token {

    private String repr;

    public AssignmentOperatorToken( String repr ) {
	this.repr = repr;
    }

    public String toString() {
	return "assignop("+repr+")";
    }
}
