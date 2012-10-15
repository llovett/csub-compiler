package lexer;

public abstract class Token {
 
    public String line;		// line of code associated with this Token
    public int	lineNumber;	// line number of this Token
    public String errorColumn;	// used to point to this Token in an error

    public abstract String toString();

    public void setContext( String line, int lineNumber, String errorColumn ) {
	this.line = line;
	this.lineNumber = lineNumber;
	this.errorColumn = errorColumn;
    }

    public String getLine() {
	return line;
    }

    public int getLineNumber() {
	return lineNumber;
    }

    public String getErrorColumn() {
	return errorColumn;
    }

}