package lexer;

public class TypeToken extends Token {

    private String repr;

    public TypeToken ( String repr ) {
	this.repr = repr;
    }

    public String toString() {
	return "type("+repr+")";
    }
}
