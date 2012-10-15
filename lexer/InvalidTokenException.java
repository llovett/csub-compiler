package lexer;

public class InvalidTokenException extends Exception {
    static final long serialVersionUID = 8070952653423906519L;
    
    private String message;

    public InvalidTokenException( String message ) {
	super( message );
	this.message = message;
    }

    public String toString() {
	return message;
    }
}