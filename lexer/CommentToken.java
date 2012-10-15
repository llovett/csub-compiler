package lexer;

public class CommentToken extends Token {

    private String repr;

    public CommentToken( String repr ) {
	this.repr = repr;
    }

    public String toString() {
	return "comment("+repr+")";
    }

    public int getLines() {
	int count = 0;
	for ( int i=0; i<repr.length(); i++ )
	    if ( repr.charAt( i ) == '\n' )
		count++;

	return count;
    }
}
