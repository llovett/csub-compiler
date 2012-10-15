package lexer;

/**
 * PunctuationToken.java
 *
 * A Token class for miscellaneous punctuation. Please note that you
 * SHOULD NOT use this class to isolate tokens that are not
 * "miscellaneous" (e.g. '+', '-', '*', and other clear OPERATORS).
 * */

public class PunctuationToken extends Token {

    private String repr;

    public PunctuationToken(String repr) throws lexer.InvalidTokenException {
	this.repr = repr;
    }

    public String getPunctuation(){
	return repr;
    }

    public String toString() {
	return String.format( "punctuation( %s )", repr );
    }


}
