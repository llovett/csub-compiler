package analyzer;
import java.io.*;
import java.util.*;
import parser.*;


/*STILL TO-DO 
 *
 * 1. No arithmetic between ints and floats - ARITHMETIC.c	***DONE
 * 2. Check arithmetic between pointers and non-pointers - ARITHMETIC.c	***addition and subtraction
 * 3. Compare function type and return types - FUNC.c
 * 4. Only dereference int* and float*; the result is always an l-value - DEREF.c	***DONE
 * 5. The address of operator only applied to l-value of ints or floats - DEREF.c	***DONE
 * 6. In assignment expressions: E1.type = E2.type (or E1.type = void*) and E1 is l-value and E2 is r-value - ASSIGN.c	***DONE
 * 7. Mod operator only applied to ints - ARITHMETIC.c		*** DONE
 * 8. Relation operators applied to operands of the same type; the result is an int REL.c	*** DONE
 * 9. Result of logical operations should be an int REL.c	*** DONE
 * 10. Expression in an if or while cannot be void IF-STMT.c
 *
 * Other stuff:
 * -Print out line numbers in error mesages	**** DONE, may be buggy.... see log entries.
 *
 * TESTS
 * 1. ARITHMETIC.c - done --checks arithmetic between pointers and non pointers, and also other things
 * 2. FUNC.c - done --checks function types, calls, returns types etc.
 * 3. DEREF.c - done --checks dereference and address of rules
 * 4. ASSIGN.c - done --checks assignment statements, checking of types and l/r values. Chained assignments
 * 5. IF-STMT.c - done --checks that if statements do not have (void) as their expression
 * 6. REL.c - done --checks relational operator rules
 * 7. SCOPE.c - done --checks scope of variable decs and function decs
 * 7. MISC.c - we can put whatever here
 */
public class Analyzer {

    private static boolean DEBUG = false;

    private Parser parser;	// The Parser
    private Symbol rootNode;	// Root of the AST (Abstract Syntax Tree)
    private ArrayList<String> errors;	// List of error messages

    private Stack<HashMap<String,Symbol>> envStack;	// environment stack

    /**
     * CONSTRUCTOR
     * */
    public Analyzer( FileDescriptor fd ) throws IOException {
	// Initialize error list
	errors = new ArrayList<String>();

	// Create the Parser.
	parser = new Parser( fd );

	// Get the parse tree.
	rootNode = parser.getSyntaxTree();
	// Bail out if empty file (nothing to analyze)
	if ( null == rootNode )
	    System.exit( 0 );
	// Bail out if the syntax tree is garbage.
	if ( parser.hasErrors() )
	    System.exit( 1 );

	// Initialize environments
	envStack = new Stack<HashMap<String,Symbol>>();
	// Initialize global environment
	envStack.push( new HashMap<String,Symbol>() );

	// Go ahead and analyze output from the parser.
	try {
	    analyze( rootNode, null );
	} catch ( UndeclaredIdentifierException e ) {
	    // Anything to do here, really?
	    // The errors are already logged...
	}

	// Print the annotated parse tree.
	if ( DEBUG )
	    System.out.println( rootNode );

	// Print out errors
	for ( String error : errors )
	    System.err.println( error );
    }


    /**
     * analyze()
     *
     * This does everything. EVERYTHING.
     * */
    private void analyze( Symbol s, Symbol defun ) throws UndeclaredIdentifierException {

	// Used to store whether or not we put on a new environment in the
	// analysis of this symbol.
	boolean enteredBlock = false;
	// We will use this variable to indicate what the most recent functional definition
	// has been. This will only change if we enter some other function.
	Symbol thisDefun = defun;
	
	// Check for identifiers, and put then into the environment.
	if ( s.type().equals( "VAR-DEC" ) || s.type().equals( "PARAM" ) ) {
	    String vtype = s.getChildren().get( 0 ).data();
	    String vname = s.getChildren().get( 1 ).data();

	    //Is this still doing what it's supposed to be doing?? 
	    if ( s.getChildren().size() >= 3 ) {
		if ( s.getChildren().get( 2 ).checkType( "VAR-DEC**" ) ||
		     s.getChildren().get( 2 ).checkType( "PARAM**" ) ) {
		    vtype += '*';	// a pointer type!
		    vname = s.getChildren().get( 1 ).data();
		    s.setAttribute( "lvalue", "false" );	// Arrays are not l-values.
		    String width = s.getChild( 2 ).getChild(1).data();
		    s.setAttribute( "width", width );
		}
	    } else if ( vtype.length() >= 1 &&
			vtype.charAt( vtype.length()-1 ) == '*' ) //oh right..
		s.setAttribute( "lvalue", "true" );
	    // Stick this into the environment: name --> this symbol
	    s.setAttribute( "type", vtype.toString() );
	    s.tag = s.getChild( 1 );

	    // Copy over context from identifier
	    s.setContext( s.getChild( 1 ).getLine(),
			  s.getChild( 1 ).getLineNumber(),
			  s.getChild( 1 ).getErrorColumn() );
	    putSymbol( vname, s );
	}
	else if ( s.type().equals( "FUN-DEC" ) ) {
	    // Set type attribute of Symbol s
	    s.setAttribute( "type", s.getChildren().get( 0 ).data() );
	    s.tag = s.getChild( 0 );
	    s.setContext( s.getChild( 1 ).getLine(),
			  s.getChild( 1 ).getLineNumber(),
			  s.getChild( 1 ).getErrorColumn() );
	    putSymbol( s.getChildren().get( 1 ).data(), s );

	    // Enter a new code block when we see FUN-DEC
	    // ( since we entered a function )
	    thisDefun = s;
	    enterBlock();
	    enteredBlock = true;
	}
	else if ( s.checkType( "IF-STMT" )
		  || s.checkType( "WHILE-STMT" )
		  || s.checkType( "FOR-STMT" ) ) {
	    // Prepare to enter a new code block/scope.
	    enterBlock();
	    enteredBlock = true;
	}


	// Analyze each of the children ( preorder traversal )
	for ( Symbol child : s.getChildren() )
	    try {
		analyze( child, thisDefun );
	    } catch ( UndeclaredIdentifierException e ) {
		// Anything to do here?
	    }


	// ------------------------------- //
	// ANALYSIS OF CURRENT SYMBOL HERE //
	// ------------------------------- //


	// 
	// Set attributes for Terminals.
	// 
	if ( s.checkType( "integer" ) || s.checkType( "floatpoint" ) ) {
	    s.setAttribute("lvalue", "false");
	}
	//
	// Set attributes for IF-STMT and WHILE-STMT
	// 
	if ( s.checkType( "IF-STMT" ) || s.checkType( "WHILE-STMT" ) ) {
	    // No voids allowed!
	    String testType = typeof( s.getChild( 1 ) );
	    if ( testType.equals( "void" ) )
		error( "Expression within if or while cannot be void.",
		       s.getChild( 1 ) );
	}
	// 
	// Check that return statements match claimed function return values.
	// CALL.
	//
	if ( s.checkType( "CALL" ) ) {
	    // The function we're making the call to
	    Symbol function = lookupSymbol( s.getChild(0) );

	    // Gather PARAMS
	    ArrayList<Symbol> params = new ArrayList<Symbol>();
	    for ( Symbol child : function.getChildren() )
		if ( child.checkType( "PARAM" ) )
		    params.add( child );

	    int curArg = 0;	// What argument we're looking at now.
	    Symbol theArgs = s.getChild( 1 );
	    for ( Symbol child : params ) {
		try {
		    // Compare the types it needs with what types we have.
		    if ( ! typesCompare( child, theArgs.getChild( curArg ) ) )
			error( String.format( "Incompatible types: \'%s\' and \'%s\'.",
					      typeof( child ), typeof( theArgs.getChild( curArg ) ) ),
			       theArgs.getChild( curArg ) );
		    theArgs.getChild( curArg ).setAttribute("type", child.getAttribute("type"));
		} catch ( IndexOutOfBoundsException e ) {
		    // Wrong number of arguments given
		    error( String.format( "Incompatible number of arguments: %d given, %d required.",
					  curArg, params.size() ),
			   curArg >= 1? theArgs.getChild( curArg-1 ) : s.getChild( 0 ) );
		}
		curArg++;
	    }
	    
	    // Now set our type
	    s.setAttribute( "type", typeof( s.getChild( 0 ) ) );
	    s.tag = s.getChild( 0 );
	}
	//
	// Set attributes for AND-EXPR
	//
	if ( s.checkType( "AND-EXPR" ) ) {
	    // Make sure LHS exists
	    String LHSType = typeof( s.getChild( 0 ) );

	    // Set attributes
	    s.setAttribute( "type", "int" );
	    s.tag = s.getChild( 0 );
	}
	//
	// Set attributes for AND-EXPR*
	//
	if ( s.checkType( "AND-EXPR*" ) ) {
	    // Make RHS exists
	    String RHSType = typeof( s.getChild( 1 ) );

	    // Set attributes
	    s.setAttribute( "type", "int" );
	    s.tag = s.getChild( 0 );
	}
	//
	// Set attributes for OR-EXPR
	//
	if ( s.checkType( "OR-EXPR" ) ) {
	    // Make sure LHS exists
	    String LHSType = typeof( s.getChild( 0 ) );
	    
	    // Set attributes
	    s.setAttribute( "type", "int" );
	    s.tag = s.getChild( 0 );
	}
	//
	// Set attributes for OR-EXPR*
	//
	if ( s.checkType( "OR-EXPR*" ) ) {
	    // Make sure RHS exists
	    String RHSType = typeof( s.getChild( 1 ) );

	    // Set attributes
	    s.setAttribute( "type", "int" );
	    s.tag = s.getChild( 1 );
	}
	// 
	// Set attributes for REL-EXPR
	//
	if ( s.checkType( "REL-EXPR" ) ) {
	    // Both LHS and RHS of a rel-expr must have the same type.
	    // The resulting type is an integer.
	    String LHSType = typeof( s.getChild( 0 ) );
	    String RHSType = typeof( s.getChild( 1 ) );
	    if ( ! LHSType.equals( RHSType ) ) {
		error( String.format( "Relational operator cannot be applied to types \'%s\' and \'%s\'.",
				      LHSType, RHSType ), s.getChild( 0 ) );
	    }
	    else {
		s.setAttribute( "type", "int" );
		s.setAttribute( "lvalue", "false" );	// Cannot assign to relational expressions.
		s.tag = s.getChild( 0 );
	    }
	}

	if ( s.checkType( "REL-EXPR*" ) ){
	    // Make sure RHS exists
	    String RHSType = typeof( s.getChild( 1 ) );

	    // Set attributes
	    s.setAttribute( "type", "int" );
		s.setAttribute( "lvalue", "false" );	// Cannot assign to relational expressions.
	    s.tag = s.getChild( 1 );
	}
	//
	// Set attributes for RETURN-STMT
	//
	if ( s.checkType( "RETURN-STMT" ) ) {
	    // Return type is "void" by default.
	    s.setAttribute( "type", "void" );
	    if ( s.getChild( 1 ).checkType( "identifier" ) )
		s.setAttribute( "type", typeof( lookupSymbol( s.getChild( 1 ) ) ) );
	    else
		s.setAttribute( "type", typeof( s.getChild( 1 ) ) );
	    
	    s.tag = s.getChild( 1 );

	    // Check our return type against function definition type.
	    if ( !s.getAttribute( "type" ).equals( typeof( defun ) ) )
		error( String.format( "Cannot return \'%s\' in a function of type \'%s\'.",
				      s.getAttribute( "type" ), typeof( defun ) ),
		       s );
	}
	// 
	// Set attributes for POSTFIX-EXPR
	// 
	if ( s.checkType( "POSTFIX-EXPR" ) ) {
	    if ( s.getChild( 0 ).checkType( "identifier" ) ) {
		String idType = typeof( s.getChild( 0 ) );
		// Check for array subscript
		if ( s.getChild( 1 ).checkData( "[" ) ) {
		    if ( idType.length() >= 1 &&
			 idType.charAt( idType.length()-1 ) == '*' ) {
			s.setAttribute( "type", idType.substring(0, idType.length()-1) );
			s.tag = s.getChild(0);
		    }
		    else
			error( "Cannot dereference type "+idType, s.getChild(0) );
		}
		// Check for a formal postfix operator (++ / --)
		else if ( s.getChild( 1 ).checkType( "postfixop" ) ) {
		    s.setAttribute( "type", idType );
		    s.setAttribute( "lvalue", "false" );
		    s.tag = s.getChild( 0 );
		}
	    }
	}
	// 
	// Set attributes for ASSIGN-EXPR (Should we do something about ASSIGN-EXPR*?)
	// 
	if ( s.checkType( "ASSIGN-EXPR" ) ) {
	    // Check chained assignment expression, if we are the last link in
	    // an assignment chain (that is, we have 4 children)
	    if ( s.getChildren().size() == 4 ) {
		// Symbol LHS = s.getChild( 0 );
		Symbol RHS = s.getChild( 2 );
		Symbol chained = s.getChild( 3 );
		
		// Look at the type of chained assignment.
		String chainedType = typeof( chained );

		// Verify that RHS of THIS assignment is an lvalue
		if ( null != RHS.getAttribute( "lvalue" ) &&
		     RHS.getAttribute( "lvalue" ).equals( "false" ) )
		    error( "Assignments can only be made to l-values.", RHS );

		// Verify that this type is compatible with RHS of THIS assignment.
		if ( ! typesCompare( RHS, chained ) )
		    error( String.format( "Cannot assign \'%s\' to \'%s\'.",
					  chainedType, typeof( RHS ) ), RHS );
	    }

	    Symbol LHS = s.getChild( 0 );
	    if ( null == LHS.getAttribute( "lvalue" ) ||	// If LHS is an lvalue
		 !LHS.getAttribute( "lvalue" ).equals( "false" ) ) {

		String LHStype = typeof( LHS );
		String RHStype;
		if ( null != s.getChild( 2 ).getAttribute( "type" ) ) {
		    RHStype = s.getChild( 2 ).getAttribute( "type" ); }
		else { RHStype = typeof( s.getChild( 2 ) ); } //this is null for CALLs 
		// Num constants have data type int, with one exception:  the constant 0 can have type int, int*, float* or void*.
		if(RHStype.equals("int") && s.getChild(2).data().equals("0")) { 
		    if(LHStype.equals( "int"  ) ||
		       LHStype.equals( "int*" ) ||
		       LHStype.equals( "float*" ) ||
		       LHStype.equals( "void*" ) ) {
			s.setAttribute( "type", typeof( s.getChild(0) ));
			s.setAttribute( "lvalue", "false" );
			s.tag = s.getChild(0);
		    }
		    else{
			error( String.format( "Cannot assign '0' to \'%s\'.",
					      LHStype ), s.getChild(0) );
		    }
		}
		else if ( LHStype.equals(  RHStype ) ||
			  ( LHStype.equals( "void*" ) && RHStype.charAt( RHStype.length()-1 ) == '*' ) ) {
		    // Type is that of LHS of expression
		    s.setAttribute( "type", typeof( s.getChild( 0 ) ));
		    // Never an l-value
		    s.setAttribute( "lvalue", "false" );
		    s.tag = s.getChild( 0 );
		} 
		else {
		    error( String.format( "Cannot assign \'%s\' to \'%s\'.",
					  RHStype, LHStype ), s.getChild(0) );
		}
	    } else { // If LHS is not an lvalue
		error( "Assignments can only be made to l-values.", LHS );
	    }
	}
	// 
	// Set attributes for ASSIGN-EXPR*
	// 
	if ( s.checkType( "ASSIGN-EXPR*" ) ) {
	    //There are two children, first child is assignop and second child is expression of some sort

	    Symbol RHS = s.getChild(1);
	    String RHSType = typeof( RHS );

	    // If we are in the middle of an assignment expression chain...
	    if ( s.getChildren().size() == 3 ) {
		// Check types.
		if ( ! typesCompare( s.getChild( 1 ), s.getChild( 2 ) ) )
		    error( String.format( "Cannot assign \'%s\' to \'%s\'.",
					  RHSType, typeof( s.getChild( 2 ) )), RHS );
	    }
		
	    s.setAttribute("type", RHSType );
	    s.tag = RHS;
	    s.setAttribute("rvalue", "true");
	    
	}
	// 
	// Set attributes for ADD-EXPR*
	// 
	if ( s.checkType( "ADD-EXPR*" ) ) {
	    // The code below may be extremely redundant. Let's write
	    // code that takes more risks (and is easier to read and debug).
	    // if ( s.getChildren().size() >= 2 &&
	    // 	 ( s.getChild( 1 ).checkType( "UNARY-EXPR" ) ||
	    // 	   s.getChild( 1 ).checkType( "ADD-EXPR" ) ) &&
	    // 	 s.getChildren().get( 0 ).checkType( "plusorminus" ) ) {
	    // 	s.setAttribute( "operator", s.getChildren().get( 0 ).data() );
	    // 	s.setAttribute( "type", s.getChild( 1 ).getAttribute( "type" ) );
	    // 	s.tag = s.getChild( 1 );
	    // }
	    
	    // Get operand types
	    Symbol LHS = s.getChild( 1 );
	    String LHSType = typeof( LHS );
	    // Get the operator
	    String operator = s.getChild( 0 ).data();

	    if ( s.getChildren().size() == 3 ) { //if there are exactly 3 children, we have a chained assingment
		// Get operand types
		Symbol RHS = s.getChild( 2 );
		String RHSType = RHS.getAttribute( "type" ); 
		operator = s.getChild( 2 ).getAttribute("operator");

		// Apply rules for non-pointers
		if ( ! ( LHSType.charAt( LHSType.length()-1 )=='*' ||
			    RHSType.charAt( RHSType.length()-1)=='*') ) {

		    // Non-pointer rules here, with both + and -

		    if ( ! ( LHSType.equals( "int" ) || LHSType.equals( "float" ) ) )
			error( "Invalid term operand type: "+LHSType, LHS );
		    else if ( ! LHSType.equals( RHSType ) )
			error( String.format( "Cannot apply \'%s\' to types %s and %s.",
					      operator, LHSType, RHSType ), s.getChild( 2 ) );

		    s.setAttribute( "type", LHSType );
		    s.tag = s.getChild( 1 );
		}

		// Apply rules for pointer arithmetic
		else {
		    // No arithmetic with a void pointer.
		    if ( LHSType.startsWith( "void" ) )
			error( String.format( "Cannot perform pointer arithmetic with type \'%s\'.",
				    LHSType ),
				LHS );
		    else if ( RHSType.startsWith( "void" ) )
			error( String.format( "Cannot perform pointer arithmetic with type \'%s\'.",
				    RHSType ),
				RHS );

		    // Apply rules for pointer addition
		    if ( operator.equals( "+" ) ) {
			// Exactly one of the operands must be of type 'int'.
			String resType = "";
			if ( LHSType.equals( "int" ) ) {
			    resType = RHSType;
			    s.setAttribute( "type", resType );
			    s.tag = RHS;
			}
			else if ( RHSType.equals( "int" ) ) {
			    resType = LHSType;
			    s.setAttribute( "type", resType );
			    s.tag =  LHS;
			}
			else {
			    error( String.format( "Cannot perform pointer arithmetic with types \'%s\' and \'%s\'.",
					LHSType, RHSType ),
				    LHS );
			    s.setAttribute( "type", typeof( LHS ) );
			    s.tag = LHS;
			}
		    }
		    // Apply rules for pointer subtraction
		    else if ( operator.equals( "-" ) ) {
			// Two legal cases of pointer subtraction:
			//
			// CASE 1: LHS and RHS are the same type.
			if ( ! LHSType.equals( RHSType ) ) {

			    // CASE 2: RHS is of type "int"
			    String resType = "";
			    if ( RHSType.equals( "int" ) ) {
				resType = LHSType;
				s.setAttribute( "type", resType );
				s.setAttribute( "lvalue", "false" );
				s.tag = resType.equals( LHSType ) ? LHS : RHS;
			    }
			    else {
				error( String.format( "Cannot subtract \'%s\' from \'%s\'.",
					    RHSType, LHSType ),
					LHS );
				s.setAttribute( "type", typeof( LHS ) );
				s.tag = LHS;
			    }
			}
			else {
			    s.setAttribute( "type", "int" );	// Difference of two pointers = int
			    s.setAttribute( "lvalue", "false" );
			}
		    }
		}
	    }
	    else{
		//It's the end of the chain
		s.setAttribute( "type", typeof( LHS ) );
	    }
	    s.tag = s.getChild( 0 );
	    s.setAttribute( "operator", operator );
	}
	// 
	// Set attributes for ADD-EXPR
	// 
	if ( s.checkType( "ADD-EXPR" ) ) {
	    if ( s.getChildren().size() == 2 ) { //if there are exactly 2 children
		// Get operand types
		Symbol LHS = s.getChild( 0 );
		String LHSType = typeof( LHS );
		Symbol RHS = s.getChild( 1 );
		String RHSType;
		if ( null != RHS.getAttribute( "type" ) ) {
		    RHSType = RHS.getAttribute( "type" ); }
		else { RHSType = typeof( RHS ); } //this is null for CALLs 
		// Get the operator
		String operator = RHS.getAttribute( "operator" );
		
		// Apply rules for non-pointers
		if ( ! ( LHSType.charAt( LHSType.length()-1 )=='*' ||
			 RHSType.charAt( RHSType.length()-1)=='*') ) {

		    // Non-pointer rules here, with both + and -

		    if ( ! ( LHSType.equals( "int" ) || LHSType.equals( "float" ) ) )
			error( "Invalid term operand type: "+LHSType, s.getChild( 0 ) );
		    else if ( ! LHSType.equals( RHSType ) )
			error( String.format( "Cannot apply \'%s\' to types %s and %s.",
					      operator, LHSType, RHSType ), s.getChild( 1 ) );
		    else {
			s.setAttribute( "type", LHSType );
			s.tag = s.getChild( 0 );
		    }
		}

		// Apply rules for pointer arithmetic
		else {
		    // No arithmetic with a void pointer.
		    if ( LHSType.startsWith( "void" ) )
			error( String.format( "Cannot perform pointer arithmetic with type \'%s\'.",
					      LHSType ),
			       LHS );
		    else if ( RHSType.startsWith( "void" ) )
			error( String.format( "Cannot perform pointer arithmetic with type \'%s\'.",
					      RHSType ),
			       RHS );

		    // Apply rules for pointer addition
		    if ( operator.equals( "+" ) ) {
			// Exactly one of the operands must be of type 'int'.
			String resType = "";
			if ( LHSType.equals( "int" ) ) {
			    resType = RHSType;
			    s.setAttribute( "type", resType );
			    s.tag = resType.equals( LHSType ) ? LHS : RHS;
			}
			else if ( RHSType.equals( "int" ) ) {
			    resType = LHSType;
			    s.setAttribute( "type", resType );
			    s.tag = resType.equals( LHSType ) ? LHS : RHS;
			}
			else
			    error( String.format( "Cannot perform pointer arithmetic with types \'%s\' and \'%s\'.",
						  LHSType, RHSType ),
				   LHS );
		    }
		    // Apply rules for pointer subtraction
		    else if ( operator.equals( "-" ) ) {
			// Two legal cases of pointer subtraction:
			//
			// CASE 1: LHS and RHS are the same type.
			if ( ! LHSType.equals( RHSType ) ) {

			    // CASE 2: RHS is of type "int"
			    String resType = "";
			    if ( RHSType.equals( "int" ) ) {
				resType = LHSType;
				s.setAttribute( "type", resType );
				s.setAttribute( "lvalue", "false" );
				s.tag = resType.equals( LHSType ) ? LHS : RHS;
			    }
			    else
				error( String.format( "Cannot subtract \'%s\' from \'%s\'.",
						      RHSType, LHSType ),
				       LHS );
			}
			else {
			    s.setAttribute( "type", "int" );	// Difference of two pointers = int
			    s.setAttribute( "lvalue", "false" );
			}
		    }
		}
	    }
	}

	// Set attributes for TERM*
	// 
	if ( s.checkType( "TERM*" ) ) {
	    //We have a chained expression
	    if ( s.getChildren().size() > 2 ){
		String op = s.getChild( 2 ).getAttribute( "operator" );
		if ( null != op ) {
		    s.setAttribute( "operator", op);
		    if ( op.equals( "*" ) || op.equals( "/" ) ) {
			// Multiplication is only valid for ints and floats.
			// Operands MUST be of identical types.

			String leftOpType = typeof( s.getChild( 1 ) );
			if ( ! ( leftOpType.equals( "int" ) || leftOpType.equals( "float" ) ) )
			    error( "Invalid term operand type: "+leftOpType, s.getChild(1) );
			String rightOpType = s.getChild( 2 ).getAttribute( "type" );
			//if ( ! ( rightOpType.equals( "int" ) || rightOpType.equals( "float" ) ) )
			    //error( "Invalid term operand type: "+rightOpType, s.getChild(2) );
			if ( ! leftOpType.equals( rightOpType ) )
			    error( String.format( "Cannot apply \'%s\' to types %s and %s.",
						  op, leftOpType, rightOpType ), s.getChild(2) );
			s.setAttribute( "type", leftOpType );
			s.tag = s.getChild(0);

		    }
		    else if ( op.equals( "%" ) ) {
			// Mod operator is only valid for ints.
			// Operands MUST be of identical types.

			String leftOpType = typeof( s.getChild( 1 ) );
			if ( ! leftOpType.equals( "int" ) )
			    error( "Invalid term operand type: "+leftOpType, s.getChild(0) );
			String rightOpType = s.getChild( 2 ).getAttribute( "type" );
			if ( ! leftOpType.equals( rightOpType ) )
			    error( String.format( "Cannot apply \'%s\' to types %s and %s.",
						  op, leftOpType, rightOpType ), s.getChild(0) );
			s.setAttribute( "type", leftOpType );
			s.tag = s.getChild(0);

		    }
		} else {
		    error( String.format( "Bad operator \'%s\'.", op), s.getChild( 2 ).getChild(0) );
		}
	    }
	    else {

		if ( s.getChildren().size() >= 2 &&
			s.getChild( 1 ).checkType( "UNARY-EXPR" ) ) {
		    if ( s.getChildren().get( 0 ).checkData( "*" ) ||
			    s.getChildren().get( 0 ).checkData( "/" ) ||
			    s.getChildren().get( 0 ).checkData( "%" ) ) {
			s.setAttribute( "operator", s.getChildren().get( 0 ).data() );
			s.setAttribute( "type", s.getChild( 1 ).getAttribute( "type" ) );
			s.tag = s.getChild( 1 );
			    }
			}
		else if ( s.getChild( 0 ).checkType( "divmodop" ) ||
			s.getChild( 0 ).checkData( "*" ) ) {
		    String termType = typeof( s.getChild( 1 ) );
		    s.setAttribute( "operator", s.getChild( 0 ).data() );
		    s.setAttribute( "type", termType );
		    s.tag = s.getChild(1);

			}
	    }
	}
	// 
	// Set attributes for TERM
	// 
	if ( s.type().equals( "TERM" ) ) {
	    if ( s.getChildren().size() == 2 ) {
		String op = s.getChild( 1 ).getAttribute( "operator" );
		if ( null != op ) {
		    if ( op.equals( "*" ) || op.equals( "/" ) ) {
			// Multiplication is only valid for ints and floats.
			// Operands MUST be of identical types.

			String leftOpType = typeof( s.getChild( 0 ) );
			if ( ! ( leftOpType.equals( "int" ) || leftOpType.equals( "float" ) ) )
			    error( "Invalid term operand type: "+leftOpType, s.getChild(0) );
			String rightOpType = typeof( s.getChild( 1 ) );
			if ( ! leftOpType.equals( rightOpType ) )
			    error( String.format( "Cannot apply \'%s\' to types %s and %s.",
						  op, leftOpType, rightOpType ), s.getChild(0) );
			s.setAttribute( "type", leftOpType );
			s.tag = s.getChild(0);

		    }
		    else if ( op.equals( "%" ) ) {
			// Multiplication is only valid for ints and floats.
			// Operands MUST be of identical types.

			String leftOpType = typeof( s.getChild( 0 ) );
			if ( ! leftOpType.equals( "int" ) )
			    error( "Invalid term operand type: "+leftOpType, s.getChild(0) );
			String rightOpType = typeof( s.getChild( 1 ) );
			if ( ! leftOpType.equals( rightOpType ) )
			    error( String.format( "Cannot apply \'%s\' to types %s and %s.",
						  op, leftOpType, rightOpType ), s.getChild(0) );
			s.setAttribute( "type", leftOpType );
			s.tag = s.getChild(0);

		    }
		} else {
		    System.err.println( "Bad operator: "+s );
		}
	    }
	}
	// 
	// Set attributes for UNARY-EXPR
	// 
	else if ( s.checkType( "UNARY-EXPR" ) ) {

	    Symbol term = null;
	    if ( s.getChild( 1 ).checkType( "identifier" ) )
		term = lookupSymbol( s.getChild( 1 ) );
	    else
		term = s.getChild( 1 );

	    String idType = typeof( term );
	    // Check for dereference operator with pointer-type
	    if ( s.getChild( 0 ).checkData( "*" ) ) {
		if ( idType.length() >= 1 &&
		     idType.charAt( idType.length()-1 ) == '*' ) {
		    s.setAttribute( "type", idType.substring(0, idType.length()-1) );
		    s.tag = term;
		}
		else
		    error( "Cannot dereference type "+idType, term );
	    }
	    // Check for address-of operator
	    else if ( s.getChild( 0 ).checkData( "&" ) ) {
		String lval = term.getAttribute( "lvalue" );
		if ( null == lval || !lval.equals( "false" ) ) {
		    s.setAttribute( "type", idType+"*" );
		    s.tag = term;
		    s.setAttribute( "lvalue", "false" );
		}
		else
		    error( "\'&\' can only be applied to l-values.", term );
	    }
	    else if ( s.getChild( 0 ).checkData( "!" ) ) {
		s.setAttribute( "type", "int" );
		s.tag = s.getChild( 1 );
	    }
	    else {
		s.setAttribute( "type", idType );
		s.tag = term;
	    }

	}
	// //
	// // Set attributes for CALL
	// // 
	// else if ( s.checkType( "CALL" ) ) {
	//     String idName = s.getChild(0).data();
	//     Symbol args = s.getChild(1);
	//     try{
	// 	Symbol func = lookupSymbol( idName );
	// 	s.setAttribute( "type", func.getAttribute("type"));
	//     }
	//     catch(UndeclaredIdentifierException e){
	// 	//Do something
	//     }
	// }

	// After we have analyzed all the nodes in our branch, exit
	// the block and get back our old (outer) scope.
	if ( enteredBlock )
	    exitBlock();
    }

    /**
     * typesCompare( Symbol left, Symbol right )
     *
     * @return - true if right can be assigned to left. False otherwise.
     */
    private boolean typesCompare( Symbol s1, Symbol s2 ) throws UndeclaredIdentifierException {
	String leftType = typeof( s1 );
	String rightType = typeof( s2 );

	// A lot of things can be assigned to void* ...
	if ( leftType.startsWith( "void*" ) ) {
	    // We must have the same number of *'s
	    for ( int i=1; i<Math.min( leftType.length(), rightType.length() ); i++ ) {
		if ( ( leftType.charAt( leftType.length()-i ) == '*' &&
		       rightType.charAt( rightType.length()-i ) != '*' ) ||
		     ( leftType.charAt( leftType.length()-i ) != '*' &&
		       rightType.charAt( rightType.length()-i ) == '*' ) )
		    return false;
	    }
	    return true;
	}

	if ( leftType.equals( "void" ) )
	    return false;

	return leftType.equals( rightType );
    }
    
    /**
     * enterBlock()
     *
     * Call this method when we enter a new block of code. Provides the
     * correct environment for when this happens on top of the envStack.
     * */
    private void enterBlock() {
	if ( DEBUG )
	    System.out.println("-- entering block --");

	HashMap<String,Symbol> newEnv = new HashMap<String,Symbol>( envStack.peek() );
	envStack.push( newEnv );

	if ( DEBUG )
	    printEnvironment();
    }

    /**
     * exitBlock()
     *
     * Call this method when we exit a block of code. Provides the
     * correct environment for when this happens on top of the envStack.
     * */
    private void exitBlock() {
	if ( DEBUG ) {
	    System.out.println("-- exiting block --");
	    printEnvironment();
	}

	envStack.pop();
    }

    /**
     * putSymbol( name, type )
     *
     * Put a symbol into the environment with given name and type. // 
     *
     * @param name - Symbol name
     * @param type - Symbol type
     * */
    private void putSymbol( String name, Symbol sym ) {
	envStack.peek().put( name, sym );
    }

    /**
     * lookupSymbol( symbolName )
     *
     * Lookup a symbol in the current environment with given name.
     *
     * @param name - Symbol name
     * */
    private Symbol lookupSymbol( Symbol sym ) throws UndeclaredIdentifierException {
	String symbolName = sym.data();
	Symbol ret = null;
	if ( ! envStack.isEmpty() )
	    ret = envStack.peek().get( symbolName );

	if ( null != ret )
	    return ret;

	error( "Undeclared identifier: "+sym.data(), sym );
	throw new UndeclaredIdentifierException( "Undeclared identifier: "+sym );
    }
    
    /**
     * typeof( Symbol )
     *
     * @return - A String designating the type of the given Symbol.
     * null if this can't be done.
     * */
    public String typeof( Symbol sym ) throws UndeclaredIdentifierException {
	if ( sym.checkType( "identifier" ) ) {
	    Symbol lookup=null;
	    try {
		lookup = lookupSymbol( sym );
	    } catch ( UndeclaredIdentifierException e ) {
		error( "Undeclared identifier: "+sym, sym );
	    }
	    if ( null == lookup ) {
		error( "Undeclared identifier: "+sym, sym );
		throw new UndeclaredIdentifierException( "Undeclared identifier: "+sym );
	    }
	    return lookup.getAttribute( "type" );
	}
	else if ( sym.checkType( "integer" ) )
	    return "int";
	else if ( sym.checkType( "floatpoint" ) )
	    return "float";

	String NTType = sym.getAttribute( "type" );
	if ( null == NTType )
	    throw new UndeclaredIdentifierException( "This is not an identifier." );

	return NTType;
    }

    /**
     * printEnvironment()
     *
     * Prints out the entire environment stack.
     * */
    private void printEnvironment() {
	StringBuilder result = new StringBuilder();

	for ( HashMap<String,Symbol> map : envStack ) {
	    result.append("------------------------------\n");

	    for ( String key : map.keySet() ) {
		result.append( key+"\t{" );
		for ( String attr : map.get( key ).getAttributes().keySet() ) {
		    result.append( attr+"="+ map.get( key ).getAttribute( attr )+", " );
		}
		result.delete( result.length()-2, result.length() );
		result.append( "}" );
		result.append( "\n" );
	    }
	    if ( result.charAt( result.length() - 1 ) == '\n' )
		result.deleteCharAt( result.length() - 1 );

	    result.append("\n------------------------------\n");
	}

	System.out.println( result.toString() );
    }

    /**
     * error( message )
     *
     * Handle an error message.
     *
     * @param message - The error message.
     * @param sym - The Symbol that caused the error. This will be used
     * to print the error context. N.B., for some reason, Symbols retrived
     * via lookupSymbol() will always have a null error context. I have no
     * idea why this might be.
     * */
    public void error( String message, Symbol sym ) {
	StringBuilder errorMessage = new StringBuilder();

	// Find a Terminal to give us info
	while ( sym.tag != null )
	    sym = sym.tag;

	errorMessage.append( String.format( "analysis error: line %d: %s\n%s\n",
					    sym.getLineNumber(),
					    message,
					    sym.getLine() ) );
	errorMessage.append( sym.getErrorColumn() + "^" );
	errors.add( errorMessage.toString() );
    }

    // Methods for the generator //
    
    public boolean hasErrors() {
	return errors.size() > 0;
    }

    public Symbol getRootNode(){
	return rootNode;
    }



    // ============================================================ //
    /**
     * MAIN METHOD.
     *
     * For testing the semantic analyzer.
     * */
    public static void main(String[] args) throws IOException {
	FileDescriptor f;

	// Parse command-line options
	String fileName = "";
	for ( int i=0; i<args.length; i++ ) {
	    String arg = args[ i ];
	    if ( arg.equals( "--debug" ) )
		Analyzer.DEBUG = true;
	    else if ( arg.equals( "-h" ) || arg.equals( "--help") || arg.equals( "-?" ) )
		usage( 0 );
	    else if ( i < args.length-1 )
		usage( 1 );
	    else
		fileName = arg;
	}

	// Create the Analyzer
	if ( fileName.equals("") ) f = FileDescriptor.in;
	else f = new FileInputStream( fileName ).getFD();
	Analyzer analyzer = new Analyzer( f );
    }

    private static void usage( int status ) {
	System.err.println("USAGE: analyzer [ --debug ] [filename]");
	System.exit( status );
    }

    // ============================================================ //
    
}
