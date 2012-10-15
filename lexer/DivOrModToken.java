package lexer;

public class DivOrModToken extends Token {

    private String repr;

    public DivOrModToken ( String repr ) {
	this.repr = repr;
    }

    public String toString() {
	return "divmodop("+repr+")";
    }
}

