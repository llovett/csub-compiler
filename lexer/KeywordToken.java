package lexer;

public class KeywordToken extends Token {

    private String repr;

    public KeywordToken(String repr) {
	this.repr = repr;
    }

    public String toString() {
	return "keyword("+repr+")";
    }
}