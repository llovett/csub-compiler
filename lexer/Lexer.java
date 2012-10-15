package lexer;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Lexer implements Iterator {

    /**
     * REGULAR EXPRESSIONS DEFINING TOKENS
     *
     * Just put new Token types here on the LHS, and
     * the regexps they correspond to on the RHS. That's it!!
     * */
    public final static String[][] typesAndPatterns = {

	/** TOKEN TYPE , -----------					REGEXP **/
	{"CommentToken",						"(/\\*.*?\\*/)"},
	{"",								"(/\\*)"},	/* comment begin */
	{"",								"(\\*/)"},	/* comment end ... 
											   do not move these! */
	{"VoidToken",							"(\\(\\s*void\\s*\\))"},
	{"TypeToken",							"(int|float|void)"},
	{"KeywordToken",						"(if|else|while|for|return)"},
	{"FloatToken",							"(\\d*\\.?\\d+[eE][+-]?\\d+)"},	/* floating points w/ optional integer part w/ exponent */
	{"FloatToken",							"(\\d+\\.\\d*|"+	/* real, fraction potentially missing */
									"\\d+\\.?\\d*[eE][+-]?\\d+|"+ /*floating points w/ optional decimal w/ exponent*/
									"\\d*\\.\\d+)"},	/* real, integer part potentially missing */
	{"IntegerToken",						"(0[xX][0-9A-Fa-f]+)"},	/* hex constant... hack to give hex consts priority */
	{"IntegerToken",						"(\\d+)"},		/* regular ol' integer */
	{"RelationalOperatorToken",					"(<=|>=|==|!=|>|<)"},
	{"AssignmentOperatorToken",					"(=|-=|\\+=|\\*=|/=|%=)"},
	{"PostfixOperatorToken",					"(\\+\\+|--)"},
	{"DivOrModToken",						"(/|%)"},
	{"PlusOrMinusToken",						"(\\+|-)"},
	{"PunctuationToken",						"(&&|\\|\\|)"},
	{"PunctuationToken",						"([!&{}();,*]|\\[|\\])"},
	{"IdentifierToken",						"([a-zA-Z_][a-zA-Z0-9_]*)"},
	{"InvalidToken",						"([^\\w\\s;]+)"}
    };

    // Where in the table above we define a comment start and end. For later clarity of code.
    final static int COMMENT_START_REGEXP_IND = 2;
    final static int COMMENT_END_REGEXP_IND = 3;
    
    // Where to store the input stream
    private StringBuilder input;
    // Pointer to where we are in the input stream
    int streamIndex = 0;

    // Pattern object built out of the above regular expressions.
    private Pattern pattern;
    // Matcher object, which does the work of finding/matching expressions.
    private Matcher matcher;

    // Keeps track of whether or not there is a next Token.
    private boolean tokenExists = false;

    // Our current token
    private Token currentToken;

    //Keep track of what line number we're on--for error reporting purposes
    private int lineNumber = 1;
    // Build an "error column" (for printing error context) as we run the Lexer.
    private StringBuilder errorColumn;
    // Current line of input being analyzed.
    private String currentLine = "";

    /**
     * Lexer CONSTRUCTOR
     *
     * @param f - What FileDescriptor to analyze lexically.
     **/
    public Lexer( FileDescriptor f ) throws IOException { 

	/** READ IN THE FILE **/
	input = new StringBuilder();
	FileReader reader = new FileReader( f );

	// Read in input file
	int x = reader.read();
	while ( x != -1 ) {
	    input.append( (char)x );
	    x = reader.read();
	}

	// Initialize error column
	errorColumn = new StringBuilder();

	// Build the pattern
	StringBuilder patternString = new StringBuilder();
	for ( String[] tokenPattern : typesAndPatterns )
	    patternString.append( tokenPattern[1]+"|" );
	// Take off the final '|'
	patternString.deleteCharAt(patternString.length() - 1);
	// Build Pattern object to feed to the Matcher
	pattern = Pattern.compile( patternString.toString() );
	// Matcher object... this does all the work for us, really...
	matcher = pattern.matcher( input );
	// Initialize the current token
	currentToken = getNewToken();
	// Initialize current line
	int lineEnd = 0;
	while ( lineEnd < input.length() && input.charAt(lineEnd) != '\n' )
	    lineEnd++;
	currentLine = input.substring(0, lineEnd);

    }

    /**
     * getNewToken()
     *
     * private method for getting the next (next next) Token from the
     * input stream. This token is held in "currentToken" until next()
     * is called. This method also updates the value of tokenExists,
     * which will inform us whether or not there is another Token to be
     * gotten.
     *
     * Also, this method uses a sweet new trick I found in the Java API reference
     * for java.lang.Class. It allows you to instantiate an arbitrary class using
     * its constructor, based on the contents of a String. Even though I have to
     * catch a kwadjrillion Exceptions, it still saves having to do stupid
     * if-else statements for EVERY SINGLE Token type we can think up.
     *
     * @return the Token in the input stream after the current one.
     * */

    private Token getNewToken() {

	// Advance the matcher
	try {
	    tokenExists = findNextMatch();


	    ////////////////////////
            // check for comments //
            ////////////////////////

	    // Check for end comment w/o begin comment:
	    if ( tokenExists && null != matcher.group( COMMENT_END_REGEXP_IND ) )
		contextError( "Invalid comment encountered!" );
	    // Check for begin comment w/o end (infinite comment):
	    if ( tokenExists && null != matcher.group( COMMENT_START_REGEXP_IND ) ) {
		boolean foundComment = true;
		StringBuilder newComment = new StringBuilder();

		// Skip over the comment completely.
		while ( foundComment ) {
		    while ( streamIndex < input.length() && input.charAt( streamIndex ) != '*' )
			newComment.append( input.charAt( streamIndex++ ) );
		    if ( streamIndex == input.length() )
			contextError( "Invalid comment encountered!" );

		    // We are looking at '*' now
		    // newComment.append('*');

		    if ( streamIndex+1 < input.length() && input.charAt( streamIndex+1 ) == '/' ) {
			// throw away end-of-comment token
			findNextMatch( streamIndex );

			newComment.append("*/");
			currentToken = new CommentToken( newComment.toString() );
			currentToken.setContext( currentLine, lineNumber, errorColumn.toString() );

			// Update line number
			lineNumber += ((CommentToken)currentToken).getLines();

			// Update the matcher start() position.
			// tokenExists = findNextMatch( streamIndex+2 );
			streamIndex += 2;

			return currentToken;
			// foundComment = false;
		    }
		    else
			newComment.append( input.charAt( streamIndex++ ) );
		}
		// // Update the matcher start() position.
		// tokenExists = findNextMatch( streamIndex+2 );
	    }
	} catch (IndexOutOfBoundsException e) {
	    tokenExists = false;
	    return currentToken;
	}


	////////////////////////////
        // did we find something? //
        ////////////////////////////

	if ( tokenExists  ) {
	    Token t = null;
	    for ( int g = 1; g <= typesAndPatterns.length; g++ ) {
		String group = matcher.group(g);
		if ( group != null ) {
		    try {
			// Sick.
			t = (Token)Class.forName( "lexer."+typesAndPatterns[g-1][0] )
			    .getConstructor( String.class )
			    .newInstance( group );
			t.setContext( currentLine, lineNumber, errorColumn.toString() );
		    } catch ( ClassNotFoundException e ) {
			contextError("No such Token class: "+typesAndPatterns[g-1][0] );
		    } catch ( NoSuchMethodError e ) {
			error("Could not instantiate Token class: "+typesAndPatterns[g-1][0]);
		    } catch ( InstantiationException e ) {
			error("Could not instantiate Token class: "+typesAndPatterns[g-1][0]);
		    } catch ( NoSuchMethodException e ) {
			error("Could not instantiate Token class: "+typesAndPatterns[g-1][0]);
		    } catch ( IllegalAccessException e ) {
			error("Could not instantiate Token class: "+typesAndPatterns[g-1][0]);
		    } catch ( java.lang.reflect.InvocationTargetException e ) {
			Throwable except = e.getCause();
			if ( except instanceof lexer.InvalidTokenException ) {
			    contextError(((lexer.InvalidTokenException)except).toString() );
			    t = getNewToken();
			}
			else error("Could not instantiate Token class: "+typesAndPatterns[g-1][0] );
		    }

		    // Bounds checking on NumberTokens.
		    if ( t instanceof NumberToken )
			try {
			    ((NumberToken)t).validate( currentToken );
			} catch ( lexer.InvalidTokenException e ) {
			    contextError( e.toString() );
			    t = getNewToken();
			}
		    // Count line numbers on multi-line comments
		    else if ( t instanceof CommentToken )
			lineNumber += ((CommentToken)t).getLines();

		    return t;
		}
	    } 
	}

	return currentToken;
    }

    /**
     * findNextMatch()
     *
     * Advance the Matcher to the next match of its regular expression.
     * 
     * @return - whether or not there was another match to be found. 
     * */
    private boolean findNextMatch() {
	// int index;

	try {
	    streamIndex = matcher.end();
	} catch (IllegalStateException e) {
	    //If the matcher hasn't matched anything yet
	    streamIndex = 0;
	}

	return findNextMatch( streamIndex );
    }

    /**
     * findNextMatch( index )
     *
     * Advance the Matcher to the next match of its regular expression,
     * starting at position `index` in the input stream.
     *
     * @param where - where to start looking for a match
     * 
     * @return - whether or not there was another match to be found. 
     * */
    private boolean findNextMatch( int where ) {
	streamIndex = where;
	int beginLine = streamIndex;
	while ( beginLine >= 0 && input.charAt( beginLine ) != '\n' )
	    beginLine--;

	// Eat up whitespace and count line numbers.
	while( (streamIndex < input.length()) && Character.isWhitespace(input.charAt(streamIndex)) ) { 
	    if ( input.charAt(streamIndex) == '\n' ) {
		int endLine = streamIndex+1;
		while ( endLine < input.length() &&
			input.charAt( endLine ) != '\n' )
		    endLine++;
		currentLine = input.toString().substring( streamIndex+1, endLine );
		lineNumber++;
	    }
	    streamIndex++;
	}

	boolean found = matcher.find(streamIndex);
	if ( found ) {
	    errorColumn = new StringBuilder();
	    for ( int i = matcher.start()-1; i >= 0 && input.charAt( i ) != '\n'; i-- ) {
		if ( input.charAt( i ) == '\t' )
		    errorColumn.append('\t');
		else
		    errorColumn.append(' ');
	    }
	    errorColumn.reverse();
	}
	return found;
    }
    
    /**
     * getLineNumber()
     *
     * @return - the current line number being analyzed by the Lexer.
     * */
    public int getLineNumber() {
	return lineNumber;
    }

    /**
     * next()
     *
     * @return the next Token in the input stream
     * */
    public Token next() {
	if ( tokenExists ) {
	    Token oldToken = currentToken;
	    currentToken = getNewToken();
	    return oldToken;
	} else throw new NoSuchElementException("No token to be gotten!");
    }

    /**
     * peek()
     *
     * @return the next Token in the input stream. Does not advance the Lexer.
     * */
    public Token peek() {
	return currentToken;
    }

    /**
     * hasNext()
     *
     * @return a boolean, indicating whether or not there is another Token to
     * be read.
     * */
    public boolean hasNext() {
	return tokenExists;
    }

    /**
     * remove()
     *
     * This method was added to comply with the Iterator interface.
     * DO NOT USE this method. It will just throw an Exception.
     * */
    public void remove() {
	throw new UnsupportedOperationException("Cannot remove a Token from the input stream!");
    }

    /**
     * error( message, errorCode )
     *
     * A convenient way to display an error.
     *
     * @param message - message to display
     * */
    public void error( String message ) {
	System.err.println( message );
    }

    /**
     * contextError( message )
     *
     * Reports an error with code context.
     *
     * @param message - message to display.
     * */
    public void contextError( String message ) {
	System.err.printf("\nline %d: %s", getLineNumber(), message);
	System.err.println( currentLine );
	errorColumn.append('^');
	System.err.println( errorColumn );
    }

    public static void main(String[] args) throws IOException {

	FileDescriptor f;

	// If there is no input file, use STDIN
	if ( args.length == 0) { f = FileDescriptor.in;	}

	// There is an input file, initialize a file object and get its file descriptor
	else { f = new FileInputStream(args[0]).getFD();}

	Lexer lexer = new Lexer(f);

	while (lexer.hasNext()) {
	    System.out.println(lexer.next());
	}
    }

}
