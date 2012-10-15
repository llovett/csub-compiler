package generator;

import java.io.*;
import java.util.*;

public class Generator implements TypeConstants{

    private IntermediateGenerator threeAddrCode;
    private CodeTable codeTable; //this represents the 3-address code table
    private HashMap<String, SymbolTableEntry> symbolTable;
    private ArrayList<String> globalVars;
    private FileWriter output;

    private static boolean DEBUG = false;
	
    /**
     * Constructor
     * */
    public Generator( FileDescriptor fin, FileWriter output ) throws IOException {
	if ( DEBUG ) IntermediateGenerator.DEBUG = true;

	threeAddrCode = new IntermediateGenerator( fin );

	this.output = output;

	//Initialize 3 address code table
	codeTable = threeAddrCode.getCodeTable();

	//Initialize the table of symbols
	symbolTable = threeAddrCode.getSymbolTable();

	// Initialize list of global variables
	globalVars = threeAddrCode.getGlobals();

	// Generate the assembly
	generate();

	// Flush & close the output file
	output.flush();
	output.close();
    }

    /**
     * generate()
     * This will read through the codeTable and spit out real assembly code!
     *
     * This might just end up being its own class too
     */
    public void generate(){
	// Write constants/static vars section
	writeln(".data");
	for ( String global : globalVars ) {
	    // Initialized to zero. Why not?
	    writeln(global+":\t.word 0");
	}
	writeln("nl:\t.asciiz \"\\n\"");
        writeln("\t.align\t4");

	// Write the prefix
	writeln(".text");
	writeln("entry:");
        writeln("\tjal     main");
        writeln("\tli      $v0, 10");
        writeln("\tsyscall");
	writeln("printint:");
        writeln("\tli      $v0, 1");
        writeln("\tsyscall");
        writeln("\tla      $a0, nl");
        writeln("\tli      $v0, 4");
        writeln("\tsyscall");
        writeln("\tjr      $ra");

	String defun = "";	// Holds the place of the current function
	int spDisplacement=0;	// Stores any displacement we do with $sp

	for ( int i=0; i<codeTable.size(); i++ ) {
	    CodeEntry line = codeTable.get(i);

	    if ( line instanceof Label ) {
		Label label = (Label)line;
		writeln( line.toString() );
		if ( label.isFunction() )
		    makeFrame( defun = label.name() );
	    }
	    else if ( line instanceof Tuple ) {
		Tuple tuple = (Tuple)line;
		// 
		// Pushing arguments onto the stack (op = "pusharg"|"pushaddr")
		// 
		if ( tuple.op.equals("pusharg") || tuple.op.equals("pushaddr") ) {
		    ArrayList<Tuple> argLines = new ArrayList<Tuple>();
		    ArrayList<String> lvOrAddr = new ArrayList<String>();
		    while ( (tuple.op.equals("pusharg") || tuple.op.equals("pushaddr") )
			    && i < codeTable.size() ) {
			argLines.add( tuple );
			lvOrAddr.add( tuple.op );
			line = codeTable.get( ++i );
			if ( line instanceof Tuple )
			    tuple = (Tuple)line;
		    }
		    // Move the stack pointer to accomodate args
		    writeInst("subi	$sp, $sp, "+(4*argLines.size()));
		    spDisplacement = 4;
		    for ( int j=0; j < argLines.size(); j++ ) {
			Tuple argLine = argLines.get(j);
			String theOp = lvOrAddr.get(j);
			// Pass a copy of the argument
			if ( theOp.equals("pusharg") ) {
			    if ( isNumber(argLine.place) )
				writeInst("li	$t0, "+argLine.place);
			    else
				writeInst("lw	$t0, "+printOffset( defun, argLine.place ));
			}
			// Pass-by-reference
			else {
			    writeInst("la	$t0, "+printOffset( defun, argLine.place));
			}
			writeInst("sw	$t0, "+spDisplacement+"($sp)");
			spDisplacement+=4;
		    }
		    spDisplacement-=4;

		    // Reset counter, put back instruction we didn't use.
		    i--;
		    continue;
		}
		// 
		// Calling a function
		// 
		else if ( tuple.op.equals("jal") ) {
		    writeInst("jal	"+tuple.arg1);
		    if ( ! tuple.place.equals("") )
			writeInst("sw	$v0, "+printOffset( defun, tuple.place ));
		    // Move back the $sp from all the "pushargs" we probably did
		    if ( spDisplacement > 0 )
			writeInst("addi	$sp, $sp, "+spDisplacement);
		}
		//
		// Returning from a function ("return")
		//
		else if ( tuple.op.equals("return") ) {
		    if ( ! tuple.place.equals("") ) {
			writeInst("lw	$t0, "+printOffset( defun, tuple.place ));
			writeInst("move	$v0, $t0");
		    }
		    writeInst("move	$sp, $fp");
		    writeInst("lw	$ra, -4($sp)");
		    writeInst("lw	$fp, 0($fp)");
		    writeInst("jr	$ra");
		    
		}
		//
		// Arithmetic operations requiring two registers for operands
		//
		else if ( tuple.op.equals("sub") ||
			  tuple.op.equals("mul") ||
			  tuple.op.equals("div") ||
			  tuple.op.equals("rem") ) {

		    if ( isNumber(tuple.arg1) )
			writeInst("li	$t1, "+tuple.arg1);
		    else
			writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    if ( tuple.op.equals("sub") && isNumber(tuple.arg2) ) {
			writeInst("subi	$t0, $t1, "+tuple.arg2);
		    }
		    else {
			if ( isNumber(tuple.arg2) )
			    writeInst("li	$t2, "+tuple.arg2);
			else
			    writeInst("lw	$t2, "+printOffset( defun, tuple.arg2 ));
			writeInst(tuple.op+"\t$t0, $t1, $t2");
		    }
		    writeInst("sw	$t0, "+printOffset( defun, tuple.place ));
		}
		// 
		// Arithmetic operations that have a separate 'immediate' function,
		// and where we can reduce # of instructions
		//
		else if ( tuple.op.equals("add") ||
			  tuple.op.equals("and") ||
			  tuple.op.equals("or") ) {
		    if ( isNumber(tuple.arg2) ) {
			if ( isNumber(tuple.arg1) )
			    writeInst("li	$t1, "+tuple.arg1);
			else
			    writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
			writeInst(tuple.op+"i	$t0, $t1, "+tuple.arg2);
		    }
		    else if ( isNumber(tuple.arg1) ) {
			if ( isNumber(tuple.arg2) )
			    writeInst("li	$t1, "+tuple.arg2);
			else
			    writeInst("lw	$t1, "+printOffset( defun, tuple.arg2 ));
			writeInst(tuple.op+"i	$t0, $t1, "+tuple.arg1);
		    }
		    else {
			writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
			writeInst("lw	$t2, "+printOffset( defun, tuple.arg2 ));
			writeInst(tuple.op+"	$t0, $t1, $t2");
		    }
		    writeInst("sw	$t0, "+printOffset( defun, tuple.place ));
		}
		//
		// Arithmetic operations requiring only one register for an operand
		// 
		else if ( tuple.op.equals("not") ||
			  tuple.op.equals("neg") ) {
		    if ( isNumber(tuple.arg1) )
			writeInst("li	$t1, "+tuple.arg1);
		    else
			writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    writeInst(tuple.op+"\t$t0, $t1");
		    writeInst("sw	$t0, "+printOffset( defun, tuple.place ));
		}
		//
		// Immediate arithmetic expressions
		//
		else if ( tuple.op.equals("addi") ||
			  tuple.op.equals("subi") ) {
		    writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    writeInst(tuple.op+"\t$t0, $t1, "+tuple.arg2);
		    writeInst("sw	$t0, "+printOffset( defun, tuple.place ));
		}
		//
		// Assignment and other stuff that does '='
		//
		else if ( tuple.op.equals("copy") ) {
		    if ( isNumber(tuple.arg1) )
			writeInst("li	$t0, "+tuple.arg1);
		    else
			writeInst("lw	$t0, "+printOffset( defun, tuple.arg1 ));
		    writeInst("sw	$t0, "+printOffset( defun, tuple.place ));
		}
		//
		// Loading arrays
		//
		else if ( tuple.op.equals("lw") ) {
		    // Find the location of the base address, put it in t0
		    // writeInst("lw	$t0, "+printOffset( defun, tuple.arg2 ));

		    // The base address of the array gets loaded into $t0
		    writeInst("la	$t0, "+printOffset( defun, tuple.arg2 ));
		    // Add to it the precalculated address offset
		    writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    writeInst("sub	$t0, $t0, $t1");
		    // Store a[n] into a temp
		    writeInst("lw	$t0, 0($t0)");
		    writeInst("sw	$t0, "+printOffset( defun, tuple.place ));
		}
		//
		// Loading arrays that are passed by reference
		//
		else if ( tuple.op.equals("la") ) {
		    // The base address of the array gets loaded into $t0
		    writeInst("lw	$t0, "+printOffset( defun, tuple.arg2 ));
		    // Add to it the precalculated address offset
		    writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    writeInst("sub	$t0, $t0, $t1");
		    // Store a[n] into a temp
		    writeInst("lw	$t0, 0($t0)");
		    writeInst("sw	$t0, "+printOffset( defun, tuple.place ));
		}
		//
		// Writing to arrays
		//
		else if ( tuple.op.equals("putarray") || tuple.op.equals("putarrayref") ) {
		    // tuple.place = thing to be stored
		    // tuple.arg1 = base address
		    // tuple.arg2 = offset

		    writeInst("lw	$t0, "+printOffset( defun, tuple.place ));
		    if ( tuple.op.equals("putarray") )
			writeInst("la	$t1, "+printOffset( defun, tuple.arg1 ));
		    else
			writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    writeInst("lw	$t2, "+printOffset( defun, tuple.arg2 ));
		    writeInst("sub	$t1, $t1, $t2");
		    writeInst("sw	$t0, 0($t1)");
		}
		//
		// Writing to pointers
		//
		else if ( tuple.op.equals("putpointer") ) {
		    writeInst("lw	$t0, "+printOffset( defun, tuple.place ));
		    writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    writeInst("sw	$t0, ($t1)");
		}
		//
		// Performing conditional branches
		// 
		else if ( tuple.op.equals("ble") ||
			  tuple.op.equals("bge") ||
			  tuple.op.equals("beq") ||
			  tuple.op.equals("bne") ||
			  tuple.op.equals("bgt") ||
			  tuple.op.equals("blt") ||
			  tuple.op.equals("beq") ) {
		    writeInst("lw	$t0, "+printOffset( defun, tuple.place ));
		    if ( isNumber(tuple.arg1) )
			writeInst("li	$t1, "+tuple.arg1);
		    else
			writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    writeInst(tuple.op+"\t$t0, $t1, "+tuple.arg2);
		}
		//
		// Unconditional branch
		//
		else if ( tuple.op.equals("b") ) {
		    writeInst("b	"+tuple.place);
		}
		//
		// Branch equal to zero
		//
		else if ( tuple.op.equals("beqz") ) {
		    writeInst("lw	$t0, "+printOffset( defun, tuple.place ));
		    writeInst("beqz	$t0, "+tuple.arg1);
		}
		//
		// Dereferences
		//
		else if ( tuple.op.equals("deref") ) {
		    writeInst("lw	$t0, "+printOffset( defun, tuple.place ));
		    writeInst("lw	$t1, "+printOffset( defun, tuple.arg1 ));
		    writeInst("lw	$t0, ($t1)");
		}
		//
		// Address-of (&)
		//
		else if ( tuple.op.equals("addrof") ) {
		    writeInst("la	$t0, "+printOffset( defun, tuple.arg1 ));
		    writeInst("sw	$t0, "+printOffset( defun, tuple.place ));
		}
	    }
	}

    }
    // TODO:
    // ------------------------------
    // deref
    // addrof
    // 
    // ble - debugging
    // bge |
    // beq |
    // bne v
    // bgt
    // blt
    // beqz ---- 
    // b


    private boolean isNumber( String test ) {
	return test.matches("\\d+");
    }

    private void makeFrame( String defun ) {
	writeInst("sw	$fp, 0($sp)");
	writeInst("sw	$ra, -4($sp)");
	writeInst("move	$fp, $sp");
	writeInst("subi	$sp, $sp, "+funcSize(defun));
    }

    /**
     * varOffset( String defun, String varname )
     *
     * Get the offset of a variable
     *
     * @param defun Name of current function
     * @param varname Name of the variable
     *
     * @return The offset of the variable from the frame pointer.
     *
     * N.B. This function will cause a NPE if called on a static var.
     * Use printOffset instead, to print where you should store your data.
     * */
    private Integer varOffset( String defun, String varname ) {
	return symbolTable.get( defun ).get( varname );
    }

    private String printOffset( String defun, String varname ) {
	Integer result = varOffset( defun, varname );
	if ( null != result )
	    return String.format("%d($fp)",result);
	// Assume we have a static/global variable
	return varname;
    }

    /**
     * int funcsize( String defun )
     *
     * @param defun The name of the function
     *
     * @return The amount of space it requires for a stack frame,
     * all-inclusive.
     * */
    private int funcSize( String defun ) {
	// Justification:
	// All types require 4 bytes. (the "4*")
	// We need space for the saved ra and maybe pushing our current fp (the "+8")
	return 4*symbolTable.get( defun ).size()+8;
    }

    /**
     * write( String what )
     *
     * Write 'what' to output file/stream/whatever.
     * */
    private void write( String what ) {
	try {
	    output.write( what );
	} catch ( IOException e ) {
	    e.printStackTrace();
	}
    }

    private void writeln( String what ) {
	write( what+"\n" );
    }

    private void writeInst( String instruction ) {
	writeln("\t"+instruction);
    }
	
    /*
     * MAIN METHOD
     * OMAGAH this is what's going to output the code for our compiler
     */
    public static void main(String[] args) throws IOException {
	FileDescriptor fin;
	FileWriter output;

	// Default: use STDIN and STDOUT
	fin = FileDescriptor.in;
	output = new FileWriter( FileDescriptor.out );

	// Parse command-line options
	String inputFileName = "";
	String outputFileName = "";
	int argCounter = 0;
	for ( int i=0; i<args.length; i++ ) {
	    String arg = args[ i ];
	    if ( arg.equals( "--debug" ) )
		Generator.DEBUG = true;
	    else if ( arg.equals( "-h" ) || arg.equals( "--help") || arg.equals( "-?" ) )
		usage( 0 );
	    else {
		if ( 0 == argCounter ) {
		    inputFileName = arg;
		    argCounter++;
		}
		else if ( 1 == argCounter ) {
		    outputFileName = arg;
		    argCounter++;
		}
		else
		    usage( 1 );
	    }
	}

	if ( ! inputFileName.equals ("") )
	    fin = new FileInputStream( inputFileName ).getFD();
	if ( ! outputFileName.equals("") ) {
	    File outputFile = new File( outputFileName );
	    if ( outputFile.exists() ) {
		if ( ! outputFile.isFile() ) {
		    System.err.println("Object exists, but is not a file: "+outputFileName);
		} else if ( ! outputFile.canWrite() ) {
		    System.err.println("Cannot write to file: "+outputFileName);
		}
	    } else {
		outputFile.createNewFile();
	    }
	    output = new FileWriter( outputFile );
	}

	Generator generator = new Generator( fin, output );
    }

    private static void usage( int status ) {
	System.err.println("USAGE: generator [ --debug ] [inputfile] [outputfile]");
	System.exit( status );
    }
}


