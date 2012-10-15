package parser;

public class TerminalDoesNotMatchException extends Exception {
    static final long serialVersionUID = 8070952653428495319L;

    private String message;
    private int error;

    public TerminalDoesNotMatchException( String message ) { 
	super( message );
	this.message = message;
	this.error = error;
    }   

    public String toString() {
	return message;
    }   
}

