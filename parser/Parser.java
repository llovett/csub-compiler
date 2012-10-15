package parser;

import java.io.*;
import lexer.*;
import java.util.ArrayList;

public class Parser {

    // Switch this on to see pedantic debugging information,
    // including the parse table, all input symbols, and where
    // we are in the parse tree during the parse.
    public static boolean DEBUG = false;

    public static String[] USEFUL_NONTERMINALS = { "PROGRAM",
						   "VAR-DEC**",
						   "DEC",
						   //"DEC*",
						   //"DEC**",
						   "PARAM",
						   "PARAM*",
						   "VAR-DEC",
						   "FUN-DEC",
						   "IF-STMT",
						   "WHILE-STMT",
						   "FOR-STMT",
						   "RETURN-STMT",
						   "ADD-EXPR",
						   "ADD-EXPR*",
						   "CALL", //don't remove
						   "ARGS", //don't remove
						   "TERM",
						   "TERM*",
						   "UNARY-EXPR",
						   "OPTIONAL-EXPR",
						   "AND-EXPR",
						   "AND-EXPR*",
						   "OR-EXPR",
						   "OR-EXPR*",
						   "REL-EXPR",
						   "REL-EXPR*",
						   "POSTFIX-EXPR",
						   //"POSTFIX-EXPR*", //don't need
						   // "PRIMARY-EXPR",
						   //"ARG-LIST",
						   "EXPR*",
						   "ASSIGN-EXPR",	// These get created in prune()
						   "ASSIGN-EXPR*",	// ^-- ditto.
						   "EXPR" };

    // public static String[] USELESS_NONTERMINALS = { "PROGRAM",
    // 						    "TYPE-SPEC",
    // 						    "STMT",
    // 						    "LOCAL-DECS",
    // 						    "NOT-IF-STMT",
    // 						    "PARAM-LIST",
    // 						    "PARAM*",
    // 						    "PARAM**",
    // 						    "IF-STMT*",
    // 						    "PARAM-LIST*",
    // 						    "STMT-LIST",
    // 						    "DEC-LIST*",
    // 						    "DEC*",
    // 						    "DEC**",
    // 						    "VAR-DEC*",
    // 						    "VAR-DEC**",
    // 						    "EXPR" };

    public static String[] USELESS_TERMINALS = { "{", "}", ",", "(", ")", ";" };


    private Lexer lexer;		// Lexical analyzer
    private Symbol topLevelNode;	// Node at the top of the syntax tree.
    private static ParseTable table;	// The parse table.
    private static ArrayList<String> errors;   // A list to keep the errors that were encountered

    public Parser( FileDescriptor fd ) throws IOException {
	lexer = null;
	errors = new ArrayList<String>();

	try {
	    lexer = new Lexer( fd );
	} catch ( IOException e ) {
	    error( "Could not open file descriptor! Exiting..." );
	    System.exit( 1 );
	}

	// Construct top level syntax tree node (i.e., the "program" node)
	topLevelNode = new Nonterminal( "", "PROGRAM" );

	// Construct parsing table
	table = new ParseTable();

	if ( DEBUG ) {
	    System.out.println("WATCH OUT!!!!!!!");
	    System.out.println( table.toString());
	}

	// Get the parse tree.
	getParseTree();
    }

    public boolean hasErrors() {
	return errors.size() > 0;
    }

    /**
     * getSyntaxTree()
     *
     * @return - The syntax tree for the CSUB grammar.
     * */
    public Symbol getParseTree() {
	try {
	    parse( topLevelNode );
	} catch ( TerminalDoesNotMatchException e ) {
	    error( e.toString() );
	    return null;
	}
	return topLevelNode;
    }

    public Symbol getSyntaxTree() {
	Symbol parseTree = getParseTree();
	if ( null == parseTree ) return null;
	ArrayList<Symbol> AST = parseTree.prune();
	if ( AST.isEmpty() ) return null;
	return AST.get( 0 );
    }

    public static ParseTable getParseTable() {
	return table;
    }

    public void parse( Symbol S ) throws TerminalDoesNotMatchException {

	// Have we reached "$" ?
	if(!lexer.hasNext()) {
	    // see if there is a production involving S and epsilon,
	    // if not, throw a TerminalDoesNotMatchException

	    if ( ! table.contains( S, new Symbol[] { } ) )
		throw new TerminalDoesNotMatchException("Unexpected end of stream!");
	    return;
	}

	Terminal inputSymbol = peekInputSymbol();
	if ( DEBUG )
	    System.out.println("current input: "+inputSymbol+", symbol: "+S);

	if ( S instanceof Terminal ){

	    Terminal T = (Terminal) S;
	    if(T.matches(inputSymbol)){
		S.setData(inputSymbol.data());
		// // Clone the context
		S.setContext( inputSymbol.getLine(), inputSymbol.getLineNumber(), inputSymbol.getErrorColumn() );

		// S = inputSymbol.copy();
		nextInputSymbol();
		return;
	    }
	    else {
		System.err.println( "Invalid symbol: "+S.data() );
		// TODO: What should we do here? Is this enough? Is this
		// what causes the message "What is this GARBAGE .... "?
	    }

	}
    
	Nonterminal N = new Nonterminal(S.data(), S.type()); 

	// See what's in the parse table, given our "stack symbol" and input symbol.
	Production production = table.get( N, inputSymbol );

	// No entry in the parse table means an error.
	if ( null == production ) {

	    parseErrorCondition( inputSymbol, N );
	    
	}

	// Apply the production.
	else {
	    for ( Symbol X : production.getRHS() )
		S.add( X.copy() );

	    // Get the syntax tree starting from each X
	    for ( Symbol child : S.getChildren() ) {
		boolean panicMode = false;
		do {
		    try {
			// check for more tokens. if not, throw an error for first child.
			parse( child );
			panicMode = false;
		    }
		    catch ( TerminalDoesNotMatchException e ){
			// Panic mode error recovery.
			panicMode = true;
			Symbol s = nextInputSymbol();
			if ( null == s ) {
			    // We have reached the end of the stream!
			    // this is ALWAYS an error.
			    error("Unexpected end of file!");
			    // parseErrorCondition( inputSymbol, (Nonterminal)S );
			    return;
			}
		    }
		} while ( panicMode );
	    }
	}
    }

    private void parseErrorCondition( Terminal S, Nonterminal N ) throws TerminalDoesNotMatchException {
	// Construct an error message telling the user what garbage was found,
	// and what sorts of input might have made sense at that point.
	StringBuilder expectedString = new StringBuilder();
	ArrayList<Terminal> expected = table.matchingTokensFor( N );
	if ( expected.size() > 0 ) {
	    if ( expected.size() > 2 )
		for ( int i = 0; i<expected.size()-1; i++ )
		    expectedString.append( expected.get( i ).simpleString()+", " );
	    else if ( expected.size() == 2 )
		expectedString.append( expected.get( 0 ).simpleString()+" " );
	    if ( expected.size() > 1 )
		expectedString.append( "or "+expected.get( expected.size()-1 ).simpleString() );
	    else
		expectedString.append( expected.get( expected.size()-1 ).simpleString() );
	    error( String.format( "Expected %s, but found \"%s\".",
				  expectedString.toString(),
				  S.data() ), S );

	    throw new TerminalDoesNotMatchException( String.format( "Expected %s, but found \"%s\".",
								    expectedString.toString(),
								    S.data() ) );
	}
	else {
	    error( "What is this GARBAGE you\'ve given me???\n"+S.toString(), S );
	    throw new TerminalDoesNotMatchException( String.format( "Expected %s, but found \"%s\".",
								    expectedString.toString(),
								    S.data() ) );
	}
    }
    
    /**
     * peekInputSymbol
     *
     * @return - The next input Symbol from the stream, by creating a new
     * Symbol from the Token returned from the lexer upon a call to peek().
     * */
    public Terminal peekInputSymbol() {
	Token token = null;
	if ( lexer.hasNext() )
	    token = lexer.peek();
	else
	    return null;

	return tokenToNextTerminal( token );
    }

    /**
     * nextInputSymbol
     *
     * @return - The next input Symbol from the stream, by creating a new
     * Symbol from the Token returned from the lexer upon a call to next().
     * */
    public Terminal nextInputSymbol() {
	Token token = null;
	if ( lexer.hasNext() )
	    token = lexer.next();
	else
	    return null;
	
	return tokenToNextTerminal( token );
    }

    /**
     * tokenToNextTerminal( token )
     *
     * Advances the token to the next meaningful Token, then
     * converts this into a Symbol for parsing.
     *
     * @param token - the Token to convert to a Symbol
     *
     * @return - A Symbol from the given Token.
     * */
    private Terminal tokenToNextTerminal( Token token ) {
	// Scan past comments.
	while ( lexer.hasNext() && lexer.peek() instanceof CommentToken )
	    token = lexer.next();
	if ( lexer.hasNext() )
	    token = lexer.peek();
	if ( token instanceof CommentToken )
	    return null;

	String tokenType, tokenInfo;

	if ( ! ( token instanceof PunctuationToken ) ) {
	    // This takes advantage of the fact that the String representation of
	    // any Token is always of the form:
	    // [a-z]+\((a-z]+)\)
	    // where GROUP1 is the token type and GROUP2 is the token information.
	    int s = token.toString().indexOf( '(' );
	    tokenType = token.toString().substring( 0, s );
	    tokenInfo = token.toString().substring( s+1, token.toString().length() - 1 );
	}
	// SPECIAL CASE: punctuation tokens
	else {
	    tokenType = "punctuation";
	    tokenInfo = ""+((PunctuationToken)token).getPunctuation();
	}

	// Build the Terminal
	Terminal term = new Terminal( tokenInfo, tokenType );
	// Set the context from the Token
	term.setContext( token.getLine(), token.getLineNumber(), token.getErrorColumn() );

	return term;
    }

    /**
     * getErrors()
     *
     * @return - A list of all parse errors caught while parsing.
     * */
    public ArrayList<String> getErrors() {
	return errors;
    }

    public String toString(){
	return topLevelNode.toString();
    }

    /////////////////
    // MAIN METHOD //
    /////////////////

    public static void main(String[] args) throws IOException {
	FileDescriptor f = null;

	// Parse command-line options
	boolean parseTree = false;
	String fileName = "";
	for ( int i=0; i<args.length; i++ ) {
	    String arg = args[ i ];
	    if ( arg.equals( "--parse-tree" ) )
		parseTree = true;
	    else if ( arg.equals( "--syntax-tree" ) );
	    else if ( arg.equals( "--debug" ) )
		Parser.DEBUG = true;
	    else if ( arg.equals( "-h" ) || arg.equals( "--help") || arg.equals( "-?" ) )
		usage( 0 );
	    else if ( i < args.length-1 )
		usage( 1 );
	    else
		fileName = arg;
	}

	// Create the Parser.
	if ( fileName.equals("") ) f = FileDescriptor.in;
	else f = new FileInputStream( fileName ).getFD();
	Parser parser = new Parser( f );

	// Print one of the parse tree or the syntax tree
	System.out.println("");
	if ( parseTree )
	    System.out.println( parser.getParseTree() );
	else
	    System.out.println( parser.getSyntaxTree().toString() );
    }

    private static void usage( int status ) {
	System.err.println("USAGE: parser [ --syntax-tree | --parse-tree ] [ --debug ] [filename]");
	System.exit( status );
    }
	



    ///////////////////////////
    // CONVENIENCE FUNCTIONS //
    ///////////////////////////

    public void error( String message ) {
	lexer.contextError( message );
	errors.add( message );
    }

    public void error( String message, Symbol sym ) {
	errors.add( message );
	System.err.printf( "parse error: line %d: %s\n", sym.getLineNumber(), message );
	System.err.printf( "%s\n%s^\n", sym.getLine(), sym.getErrorColumn() );
    }

}
