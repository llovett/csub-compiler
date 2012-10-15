package analyzer;

public class UndeclaredIdentifierException extends Exception {

    static final long serialVersionUID = 1271606635995375062L;

    public UndeclaredIdentifierException( String message ) {
	super( message );
    }

}