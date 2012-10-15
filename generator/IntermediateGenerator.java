package generator;
import java.io.*;
import java.util.*;
import analyzer.*;
import parser.*;

/* What's gonna happen:
 * -Read in syntax tree
 *  -bottom up traversal of the tree
 *  -when a node is visited, the code generator generates code for the children of the node
 *  -need some sort of grammar to determine what code gets generated for which type of node
 * -Intermediate code generation
 *   -3-address code generation in the codeTable
 * -Translation of 3-address code to mips
 */

public class IntermediateGenerator implements TypeConstants{

    private Analyzer analyzer;
    private CodeTable codeTable; //this represents the 3-address code table
    private HashMap<String, SymbolTableEntry> symbolTable;
    private ArrayList<String> globalVars;
    private Symbol rootNode; //root node of the syntax tree
    private int numTemps; //number of temp variables generated so far
    private Stack<Dereference> derfs;

    public static boolean DEBUG = false;

    /**
     * Constructor
     * */
    public IntermediateGenerator( FileDescriptor fd ) throws IOException {

	// Create the Analyzer
	analyzer = new Analyzer( fd );

	// Get the root node of the parse tree
	rootNode = analyzer.getRootNode();

	//Initialize 3 address code table
	codeTable = new CodeTable();

	//Initialize the table of symbols
	//		function name ---> info about function vars and params
	symbolTable = new HashMap<String, SymbolTableEntry>();
	globalVars = new ArrayList<String>();
	numTemps = 0;

	if ( analyzer.hasErrors() )
	    System.exit( 1 );

	// Initialize stack for tracking dereferences
	derfs = new Stack<Dereference>();

	// Generate intermediate 3-address-style code
	intermediateGenerate( rootNode, null);

	// For debugging purposes
	if ( DEBUG ) {
	    System.out.println(rootNode.toString());
	    System.out.println();
	    System.out.println(codeTable.toString());
	    System.out.println("");
	    System.out.println(symbolTableToString());
	    System.out.println("");
	}
    }

    public CodeTable getCodeTable() {
	return codeTable;
    }

    public HashMap<String, SymbolTableEntry> getSymbolTable() {
	return symbolTable;
    }

    public ArrayList<String> getGlobals() {
	return globalVars;
    }

/*******************************************************************************************/
/**********************    INTERMEDIATE GENERATOR    ***************************************/
/*******************************************************************************************/

    /**
     * intermediateGenerate()
     *
     * This recursively generates the intermediate code.
     *  - puts things in the codeTable and variable table?
     */
    public void intermediateGenerate( Symbol symbol, String function ){

	//ArrayList<CodeEntry> codeBlock = new ArrayList<CodeEntry>();
	HashMap<String, Integer> typesHash = new TypeHashMap();

	//index for looping through children
	int childIndex;
	//functionSymbolTable that we'll use throughout the switch statement
	SymbolTableEntry functionSymbolTable;
	//String that holds any operator value throughout the switch statement
	String op = "";
	//For condition statements:
	Label trueLabel;
	Label falseLabel;
	Label afterLabel;

	Integer switchType = typesHash.get(symbol.type());
	if( switchType == null ){
	    //pick an arbitrary number so that we go to default
	    //If the type is not in the TypeHashMap, we probably don't need to do anything for it
	    switchType = 100;
	}

	switch( switchType ) {

	    /**************/
	case INTEGER:
	    /**************/

	    // Place for integer constant = its value
	    symbol.place = symbol.data();
	    break;


	    /****************/
	case IDENTIFIER:
	    /****************/
	    //the place of this node is just its variable name (whose
	    //value can be looked up in the SymbolTable)
	    symbol.place = symbol.data();
	    break;

	    /*****************/
	case ASSIGN_EXPR:
	    /*****************/
	    /*
	      AssignmentOperatorToken --> (=|-=|\\+=|\\*=|/=|%=)
	    */
	    op = symbol.getChild(1).data();
	    
	    for ( Symbol child : symbol.getChildren() )
		intermediateGenerate( child, function );
		
	    symbol.place = symbol.getChild(0).place;

	    // Check for chained assignments
	    if ( symbol.getChildren().size() == 4 ) {
		op = symbol.getChild( 3 ).getChild( 0 ).data();
		if ( op.equals("=") ) {
		    codeTable.add( new Tuple("copy", symbol.getChild(2).place, symbol.getChild(3).place, "") );
		}
		else if ( op.equals("-=") ) {
		    codeTable.add( new Tuple("sub", symbol.getChild(2).place, symbol.getChild(2).place, symbol.getChild(3).place) );
		}
		else if ( op.equals("+=") ) {
		    codeTable.add( new Tuple("add", symbol.getChild(2).place, symbol.getChild(2).place, symbol.getChild(3).place) );
		}
		else if ( op.equals("*=") ) {
		    codeTable.add( new Tuple("mul", symbol.getChild(2).place, symbol.getChild(2).place, symbol.getChild(3).place) );
		}
		else if ( op.equals("/=") ) {
		    codeTable.add( new Tuple("div", symbol.getChild(2).place, symbol.getChild(2).place, symbol.getChild(3).place) );
		}
		else if ( op.equals("%=") ) {
		    codeTable.add( new Tuple("rem", symbol.getChild(2).place, symbol.getChild(2).place, symbol.getChild(3).place) );
		}
	    }
	    op = symbol.getChild(1).data();
	    if ( op.equals("=") ) {
		codeTable.add( new Tuple("copy", symbol.place, symbol.getChild(2).place, "") );
	    }
	    else if ( op.equals("-=") ) {
		codeTable.add( new Tuple("sub", symbol.place, symbol.place, symbol.getChild(2).place) );
	    }
	    else if ( op.equals("+=") ) {
		codeTable.add( new Tuple("add", symbol.place, symbol.place, symbol.getChild(2).place) );
	    }
	    else if ( op.equals("*=") ) {
		codeTable.add( new Tuple("mul", symbol.place, symbol.place, symbol.getChild(2).place) );
	    }
	    else if ( op.equals("/=") ) {
		codeTable.add( new Tuple("div", symbol.place, symbol.place, symbol.getChild(2).place) );
	    }
	    else if ( op.equals("%=") ) {
		codeTable.add( new Tuple("rem", symbol.place, symbol.place, symbol.getChild(2).place) );
	    }

	    // First child = postfix-expr --> we are assigning to an array bucket
	    if ( symbol.getChild(0).checkType("POSTFIX-EXPR") ) {
		functionSymbolTable = symbolTable.get(function);
		// Make sure we move assigned value back into the right spot.
		Dereference d = derfs.pop();
		// Check for an array passed by reference
		if ( functionSymbolTable.get( d.arrayName ) > 0 )
		    codeTable.add( new Tuple("putarrayref", d.tempName, d.arrayName, d.offsetName) );
		else
		    codeTable.add( new Tuple("putarray", d.tempName, d.arrayName, d.offsetName) );
	    }
	    else if ( symbol.getChild(0).checkType("UNARY-EXPR") ) {
		Symbol lhs = symbol.getChild(0);
		// Check for assigning to a pointer
		if ( lhs.getChild(0).data().equals("*") ) {
		    Dereference d = derfs.pop();
		    codeTable.add( new Tuple("putpointer", d.tempName, d.arrayName, ""));
		}
	    }
	    break;

	    /*****************/
	case ASSIGN_EXPR$:
	    /*****************/
	    if ( symbol.getChildren().size() > 2 ) {
		for ( Symbol child : symbol.getChildren() )
		    intermediateGenerate( child, function );

		symbol.place = symbol.getChild(1).place;

		op = symbol.getChild( 2 ).getChild( 0 ).data();
	
		if ( op.equals("=") ) {
		    codeTable.add( new Tuple("copy", symbol.place, symbol.getChild(2).place, "") );
		}
		else if ( op.equals("-=") ) {
		    codeTable.add( new Tuple("sub", symbol.place, symbol.place, symbol.getChild(2).place) );
		}
		else if ( op.equals("+=") ) {
		    codeTable.add( new Tuple("add", symbol.place, symbol.place, symbol.getChild(2).place) );
		}
		else if ( op.equals("*=") ) {
		    codeTable.add( new Tuple("mul", symbol.place, symbol.place, symbol.getChild(2).place) );
		}
		else if ( op.equals("/=") ) {
		    codeTable.add( new Tuple("div", symbol.place, symbol.place, symbol.getChild(2).place) );
		}
		else if ( op.equals("%=") ) {
		    codeTable.add( new Tuple("rem", symbol.place, symbol.place, symbol.getChild(2).place) );
		}
	
	    }
	    else if ( symbol.getChildren().size() == 2 ) {
		intermediateGenerate( symbol.getChild(1), function );
		symbol.place = symbol.getChild(1).place;
	    }
		    
	    break;

	    /************/
	case VAR_DEC:
	    /************/

	    if(function == null){
		//We've got a global varable
		symbol.place = symbol.getChild(1).data();
		globalVars.add( symbol.getChild(1).data() );
	    }
	    else{
		//We've got a local variable declaration
		symbol.place = symbol.getChild(1).data();
		//Get the SymbolTableEntry corresponding to this function
		functionSymbolTable = symbolTable.get(function);
		functionSymbolTable.addVar( symbol );
	    }


	    break;

	    /*************/
	case FUN_DEC:
	    /*************/


	    //Put the function label in the codeTable
	    Label decLabel = new Label( symbol.getChild(1).data(), true );
	    codeTable.add( decLabel );

	    function = symbol.getChild(1).data();

	    //create a new symbolTableEntry in the symbolTable for this function
	    functionSymbolTable = new SymbolTableEntry();
	    symbolTable.put(symbol.getChild(1).data(), functionSymbolTable);


	    childIndex = 2;
	    //we're in the body of the function now, we need to generate code
	    for( ; childIndex<symbol.getChildren().size(); childIndex++)
		intermediateGenerate(symbol.getChild( childIndex ), function );

	    // Check if there was a return statement. If not, make a blank one
	    CodeEntry lastEntry = codeTable.get( codeTable.size()-1 );
	    if ( (! (lastEntry instanceof Tuple)) || (! ((Tuple)lastEntry).op.equals("return")) )
		codeTable.add( new Tuple("return","","","") );
	    
	    break;

	    /**************/
	case NO_PARAMS:
	    /**************/

	    break;

	    /***********/
	case PARAM:
	    /***********/

	    functionSymbolTable = symbolTable.get(function);
	    functionSymbolTable.addParam( symbol );

	    // // Fix for pass-by-reference (TODO: is there a cleaner way?)
	    // String symType = symbol.getAttribute("type");
	    // if ( symType.charAt(symType.length()-1) == '*' ) {
	    // 	// Overwrite pointer-to-pointer with pointer.
	    // 	codeTable.add( new Tuple("passref", symbol.getChild(1).data(), "", "") );
	    // }
	    break;
		
	    /*********/
	case EXPR:
	    /*********/

	    //I'm thinking we don't need to do anything here
	    break;

	    /**********/
	case EXPR$:
	    /**********/

	    //Here either
	    break;

	    /*************/
	case ADD_EXPR:
	    /*************/

	    //Generate new temp to hold the value of this add expr
	    symbol.place = newTemp( function );
	    //Store the value of add, arg1, arg2 in the new temp
	    //arg1 is symbol.getChild(0)
	    //arg2 is symbol.getChild(2).place
		for ( Symbol child : symbol.getChildren() )
		    intermediateGenerate( child, function );

	    //we need to get the operator
	    op = symbol.getChild(1).getChild(0).data();
	    if( op.compareTo("+") == 0 ){
		op = "add";
	    }
	    else{
		op = "sub";
	    }
	    codeTable.add( new Tuple(op, symbol.place, symbol.getChild(0).place, symbol.getChild(1).place) );
	    break;

	    /**************/
	case ADD_EXPR$:
	    /**************/

	    if( symbol.getChildren().size() > 2 ){
		//We've got a chained expression
		symbol.place = newTemp( function );

		for ( Symbol child : symbol.getChildren() )
		    intermediateGenerate( child, function );

		op = symbol.getChild(2).getChild(0).data();
		if( op.compareTo("+") == 0 ){
		    op = "add";
		}
		else{
		    op = "sub";
		}
		codeTable.add( new Tuple(op, symbol.place, symbol.getChild(1).place, symbol.getChild(2).place) );
	    }
	    else {
		//We're at the end of the add expr
		intermediateGenerate( symbol.getChild(1), function );
		symbol.place = symbol.getChild(1).place;
	    }
	    break;

	    /*************/
	case AND_EXPR:
	    /*************/

	    //Generate new temp to hold the value of this AND_EXPR
	    symbol.place = newTemp( function );
	    //Store the value of and, arg1, arg2 in the new temp
	    //arg1 is symbol.getChild(0).data
	    //arg2 is symbol.getChild(1).place
	    for ( Symbol child : symbol.getChildren() )
		intermediateGenerate( child, function );

	    codeTable.add( new Tuple("and", symbol.place, symbol.getChild(0).place, symbol.getChild(1).place) );
	    break;

	    /***************/
	case AND_EXPR$:
	    /***************/

	    if( symbol.getChildren().size() > 2 ){
		//We've got a chained expression
		symbol.place = newTemp( function );
		for ( Symbol child : symbol.getChildren() )
		    intermediateGenerate( child, function );

		codeTable.add( new Tuple("and", symbol.place, symbol.getChild(1).place, symbol.getChild(2).place) );
	    }
	    else {
		//We're at the end of the add expr
		intermediateGenerate( symbol.getChild(1), function );
		symbol.place = symbol.getChild(1).place;
	    }
	    break;

	    /*************/
	case OR_EXPR:
	    /*************/
		
	    symbol.place = newTemp( function );
	    for ( Symbol child : symbol.getChildren() )
		intermediateGenerate( child, function );

	    codeTable.add( new Tuple("or", symbol.place, symbol.getChild(0).place, symbol.getChild(1).place) );
	    break;

	    /*************/
	case OR_EXPR$:
	    /*************/

	    if( symbol.getChildren().size() > 2 ){
		//We've got a chained expression
		symbol.place = newTemp( function );

		for ( Symbol child : symbol.getChildren() )
		    intermediateGenerate( child, function );

		codeTable.add( new Tuple("or", symbol.place, symbol.getChild(1).place, symbol.getChild(2).place) );
	    }
	    else {
		//We're at the end of the add expr
		intermediateGenerate( symbol.getChild(1), function );
		symbol.place = symbol.getChild(1).place;
	    }
	    break;

	    /**********/
	case TERM:
	    /**********/

	    //Generate new temp to hold the value of this add expr
	    symbol.place = newTemp( function );
	    //Store the value of add, arg1, arg2 in the new temp
	    //arg1 is symbol.getChild(0)
	    //arg2 is symbol.getChild(2).place
	    for ( Symbol child : symbol.getChildren() )
		intermediateGenerate( child, function );

	    //we need to get the operator
	    op = symbol.getChild(1).getChild(0).data();
	    if( op.compareTo("*") == 0 ){
		op = "mul";
	    }
	    else if( op.compareTo("/") == 0 ){
		op = "div";
	    }
	    else {
		op = "rem";
	    }
	    codeTable.add( new Tuple(op, symbol.place, symbol.getChild(0).place, symbol.getChild(1).place) );
	    break;

	    /***********/
	case TERM$:
	    /***********/

	    if( symbol.getChildren().size() > 2 ){
		//We've got a chained expression
		symbol.place = newTemp( function );
		for ( Symbol child : symbol.getChildren() )
		    intermediateGenerate( child, function );

		op = symbol.getChild(1).getChild(0).data();
		if( op.compareTo("*") == 0 ){
		    op = "mul";
		}
		else if( op.compareTo("/") == 0 ){
		    op = "div";
		}
		else {
		    op = "rem";
		}
		codeTable.add( new Tuple(op, symbol.place, symbol.getChild(1).place, symbol.getChild(2).place) );
	    }
	    else {
		//We're at the end of the expr
		for ( Symbol child : symbol.getChildren() )
		    intermediateGenerate( child, function );

		symbol.place = symbol.getChild(1).place;
	    }
	    break;

	    /****************/
	case UNARY_EXPR:
	    /****************/

	    symbol.place = newTemp( function );
	    intermediateGenerate(symbol.getChild( 0 ), function );
	    if( symbol.getChildren().size() > 1 )
		intermediateGenerate(symbol.getChild( 1 ), function );
	    if( symbol.getChild( 0 ) instanceof Terminal ){
		op = symbol.getChild( 0 ).data();
		if( op.compareTo("!") == 0 ) {
		    //not
		    codeTable.add( new Tuple("not", symbol.place, symbol.getChild(1).place, "" ) );
		}
		else if( op.compareTo("*") == 0 ){
		    /* deref	a	b
		     *
		     * lw	a, b.offset($fp)
		     * */
		    codeTable.add( new Tuple("deref", symbol.place, symbol.getChild(1).place, "") );
		    // Push a new Dereference onto the Dereference stack
		    derfs.push( new Dereference( symbol.place, symbol.getChild(1).place, "" ) ); // was 0, addrCalc
		    
		    //dereference
		    //need to get the value of whatever is stored at this address???
		}
		else if( op.compareTo("&") == 0 ){
		    /* addrof	a	b
		     *
		     * la	a, b.offset($fp)
		     * */
		    codeTable.add( new Tuple("addrof", symbol.place, symbol.getChild(1).place, "") );
		    //address of
		    //need to return the address of this thing????
		}
		else if( op.compareTo("-") == 0 ){
		    //negative
		    codeTable.add( new Tuple("neg", symbol.place, symbol.getChild(1).place, "") );
		}
		else if( op.compareTo("+") == 0 ){
		    //positive
		    //Do we need to do anything here?????
		}
	    }
	    break;

	    /******************/
	case POSTFIX_EXPR:
	    /******************/

	    symbol.place = newTemp( function );
	    for( Symbol child : symbol.getChildren() )
		intermediateGenerate(child, function );

	    if( symbol.getChildren().size() == 2 ){
		symbol.place = symbol.getChild(0).place;
		op = symbol.getChild( 1 ).data();
		if( op.equals("++") ) {
		    //symbol = symbol + 1
		    codeTable.add( new Tuple("addi", symbol.place, symbol.getChild(0).place, "1" ) );
		}
		else if( op.compareTo("--") == 0 ){
		    codeTable.add( new Tuple("subi", symbol.place, symbol.getChild(0).place, "1" ) );
		}
	    }
	    else{
		//we've got that expr[ expr ] thing
		//child 0 = some expr
		//child 1 = [
		//child 2 = some expr
		//child 3 = ]
		//
		//oh my god what does this even mean????	<-- liz, you are feaking out.
		//are we referencing some space in an array????
		String addrCalc = newTemp( function );
		codeTable.add( new Tuple("mul", addrCalc, symbol.getChild(2).place, "4") );

		// Check if array was passed by reference
		Symbol arrayRef = symbol.getChild(0);
		functionSymbolTable = symbolTable.get(function);
		if ( functionSymbolTable.get( arrayRef.data() ) > 0 )
		    codeTable.add( new Tuple("la", symbol.place, addrCalc, arrayRef.place) );
		else
		    codeTable.add( new Tuple("lw", symbol.place, addrCalc, arrayRef.place) );

		// Push a new Dereference onto the Dereference stack
		derfs.push( new Dereference( symbol.place, symbol.getChild(0).place, addrCalc ) );
	    }
	    break;


	    /**************/
	case REL_EXPR:
	    /**************/

	    /*RelationalOperatorToken --> (<=|>=|==|!=|>|<)*/

	    //symbol.place is going to hold the boolean value of this statement
	    symbol.place = newTemp( function );

	    //gen code for all the children
	    for( Symbol child : symbol.getChildren() ){
		intermediateGenerate(child, function );
	    }

	    //label for branch statements
	    trueLabel = new Label();

	    //get the operator
	    op = symbol.getChild( 1 ).getChild(0).data();

	    //set place to 1
	    codeTable.add( new Tuple("copy", symbol.place, "1", "") );
	    if( op.equals( "<=" ) ){
		//if true, go to true label
		//otherwise, set place to 0
		//branch if less than or equal
		op = "ble";
	    }
	    else if( op.equals( ">=" ) ){
		op = "bge";
	    }
	    else if( op.equals( "==" ) ){
		op = "beq";
	    }
	    else if( op.equals( "!=" ) ){
		op = "bne";
	    }
	    else if( op.equals( ">" ) ){
		op = "bgt";
	    }
	    else if( op.equals( "<" ) ){
		op = "blt";
	    }
	    codeTable.add( new Tuple(op, symbol.getChild(0).place, symbol.getChild(1).place, trueLabel.name() ) );
	    codeTable.add( new Tuple("copy", symbol.place, "0", "") );
	    codeTable.add( trueLabel );

	    break;

	    /*************/
	case REL_EXPR$:
	    /*************/

	    for( Symbol child : symbol.getChildren() ){
		intermediateGenerate(child, function );
	    }

	    if( symbol.getChildren().size() > 2){
		//chained expression
			
		symbol.place = newTemp( function );
		trueLabel = new Label();

		//get the operator
		op = symbol.getChild( 2 ).getChild( 0 ).data();

		//set place to 1
		codeTable.add( new Tuple("copy", symbol.place, "1", "") );
		if( op.compareTo( "<=" ) == 0){
		    //if true, go to true label
		    //otherwise, set place to 0
		    //branch if less than or equal
		    op = "ble";
		}
		else if( op.equals( ">=" ) ){
		    op = "bge";
		}
		else if( op.equals( "==" ) ){
		    op = "beq";
		}
		else if( op.equals( "!=" ) ){
		    op = "bne";
		}
		else if( op.equals( ">" ) ){
		    op = "bgt";
		}
		else if( op.equals( "<" ) ){
		    op = "blt";
		}
		codeTable.add( new Tuple(op, symbol.getChild(1).place, symbol.getChild(2).place, trueLabel.toString()) );
		codeTable.add( new Tuple("copy", symbol.place, "0", "") );
		codeTable.add( trueLabel );

	    }
	    else{
		//end of the expression
		symbol.place = symbol.getChild(1).place;
	    }
	    break;
	case CALL:
	    symbol.place = newTemp( function );
	    for( Symbol child : symbol.getChildren() ){
		intermediateGenerate(child, function );
	    }
	    for ( Symbol argument : symbol.getChild(1).getChildren() ) {
		String argType = argument.getAttribute("type");
		if ( argType.charAt(argType.length()-1) == '*' &&
		     (!argument.checkType("UNARY-EXPR")) )
		    codeTable.add( new Tuple("pushaddr", argument.place, "", "") );
		else
		    codeTable.add( new Tuple("pusharg", argument.place, "", "") );
	    }
	    codeTable.add( new Tuple("jal", symbol.place, symbol.getChild(0).place, "") );

	    break;
	case ARGS:
	    for( Symbol child : symbol.getChildren() ){
		intermediateGenerate(child, function );
	    }

	    break;
	case IF_STMT:
	{
	    // test expr
	    // branch on false to FALSE
	    // (true body)
	    // branch AFTER
	    // FALSE:
	    // (false body, if one exists)
	    // AFTER:


	    // If with else
	    intermediateGenerate( symbol.getChild(1), function );
	    falseLabel = new Label();
	    afterLabel = new Label();
	    // Branch on false
	    codeTable.add( new Tuple("beqz", symbol.getChild(1).place, falseLabel.name(), "") );
	    // Generate "true" body
	    int i=2;
	    for ( ; i<symbol.getChildren().size() &&
		      (! symbol.getChild(i).data().equals("else")); i++ ) {
		intermediateGenerate( symbol.getChild(i), function );
	    }
	    // Jump to after FALSE body
	    codeTable.add( new Tuple("b", afterLabel.name(), "", ""));
	    // Generate FALSE label
	    codeTable.add( falseLabel );
	    // Generate "false" body
	    for ( ; i<symbol.getChildren().size(); i++ ) {
		intermediateGenerate( symbol.getChild(i), function );
	    }
	    // Emit AFTER label
	    codeTable.add( afterLabel );
	    // if ( symbol.getChildren().size() > 3 ) {
	    // 	falseLabel = new Label();
	    // 	codeTable.add( new Tuple("beqz", symbol.getChild(1).place, falseLabel.name(), "") );
	    // 	intermediateGenerate( symbol.getChild(2), function );
	    // 	codeTable.add( new Tuple("b", afterLabel.name(), "", "") );
	    // 	codeTable.add( falseLabel );
	    // 	intermediateGenerate( symbol.getChild(4), function );
	    // 	codeTable.add( afterLabel );
	    // }
	    // // No else statement.
	    // else {
	    // 	codeTable.add( new Tuple("beqz", symbol.getChild(1).place, afterLabel.name(), "") );
	    // 	intermediateGenerate( symbol.getChild(2), function );
	    // 	codeTable.add( afterLabel );
	    // }	
	    
	}
	    break;
	case WHILE_STMT:
	    trueLabel = new Label();
	    afterLabel = new Label();
	    
	    codeTable.add( trueLabel );
	    intermediateGenerate( symbol.getChild(1), function );
	    codeTable.add( new Tuple("beqz", symbol.getChild(1).place, afterLabel.name(), "") );
	    for ( int i=2; i<symbol.getChildren().size(); i++ )
		intermediateGenerate( symbol.getChild(i), function );

	    codeTable.add( new Tuple("b", trueLabel.name(), "", "") );
	    codeTable.add( afterLabel );
	    break;
	case FOR_STMT:
	    // Initialization of loop
	    intermediateGenerate( symbol.getChild(1), function );
	    // Top of loop
	    trueLabel = new Label();
	    afterLabel = new Label();
	    // The test
	    codeTable.add( trueLabel );
	    intermediateGenerate( symbol.getChild(2), function );
	    codeTable.add( new Tuple("beqz", symbol.getChild(2).place, afterLabel.name(), "") );	    
	    // Body of loop
	    for ( int i=4; i<symbol.getChildren().size(); i++ )
		intermediateGenerate( symbol.getChild(i), function );
	    // Update of the loop
	    intermediateGenerate( symbol.getChild(3), function );
	    codeTable.add( new Tuple("b", trueLabel.name(), "", "") );
	    // After the loop
	    codeTable.add( afterLabel );

	    break;
	case RETURN_STMT:
	    symbol.place = newTemp( function );
	    intermediateGenerate( symbol.getChild(1), function );
	    codeTable.add( new Tuple("return", symbol.getChild(1).place, "", "") );

	    break;

	default:	    /*Error condition?*/

	    //generate code for the children
	    //If it's a terminal or has no children, this loop won't run
	    childIndex = 0;
	    for( ; childIndex<symbol.getChildren().size(); childIndex++)
		intermediateGenerate(symbol.getChild( childIndex ), function );


	    /*//Throw an error
	      System.err.printf( "What is this: %s\n", symbol.toString() );
	    */
	    break;
	}

	//codeTable.add( codeBlock );

	/*I think we'll do the traversal within each case of the switch statement
	//Postorder traversal of syntax tree
	for ( Symbol child : symbol.getChildren() )
	intermediateGenerate( child );
	*/
    }

    public String newTemp(String function){
	//Temp variables are in the form @n where n is just some number that increments
	StringBuilder tempid = new StringBuilder();
	tempid.append("@"+numTemps);
	numTemps++;
	//I'm not sure if I need to put a type in here, but I don't think so...
	Symbol tempSym = new Symbol(tempid.toString(), ""); 

	SymbolTableEntry functionSymbolTable = symbolTable.get(function);
	functionSymbolTable.addTemp(tempSym);

	return tempid.toString();
    }

    public String symbolTableToString(){
	StringBuilder ret = new StringBuilder();
	ret.append("=======================================================\n");
	ret.append("SYBOL TABLE ===========================================\n");
	ret.append("=======================================================\n");
	ret.append("\n");
	ret.append(globalVarsToString());
	ret.append("\n");

	//Get the iterators
	Set<String> functions = symbolTable.keySet();
	Collection<SymbolTableEntry> symbolTableEntries = symbolTable.values();
	Iterator functionIterator = functions.iterator();
	Iterator symbolTableEntriesIterator = symbolTableEntries.iterator();

	//Print the contents
	while(functionIterator.hasNext()){
	    ret.append(functionIterator.next()+":\n");
	    SymbolTableEntry entry = (SymbolTableEntry)symbolTableEntriesIterator.next();
	    ret.append(entry.toString());

	}
	return ret.toString();
    }

    public String globalVarsToString(){
	StringBuilder ret = new StringBuilder();
	ret.append("GLOBAL VARS\n");
	for( String var : globalVars ){
	    ret.append( "id: "+var+"\n" );
	}
	return ret.toString();
    }

    /*
     * MAIN METHOD
     * OMAGAH this is what's going to output the code for our compiler
     */
    public static void main(String[] args) throws IOException {
	FileDescriptor f;

	// If there is no input file, use STDIN
	if ( args.length == 0) { f = FileDescriptor.in;	}

	// There is an input file, initialize a file object and get its file descriptor
	else {
	    f = new FileInputStream(args[0]).getFD();
	}

	IntermediateGenerator threeAddrCode = new IntermediateGenerator( f );
    }

    /**
     * class Dereference
     *
     * This class is used to store information about dereferences, used in
     * array lookups and pointers.
     * */
    class Dereference {
	public String tempName;
	public String arrayName;
	public String offsetName;

	public Dereference( String tempName, String arrayName, String offsetName ) {
	    this.tempName = tempName;
	    this.arrayName = arrayName;
	    this.offsetName = offsetName;
	}
    }
	
}


