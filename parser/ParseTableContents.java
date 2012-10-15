package parser;

public interface ParseTableContents {

    public static final String[][] TABLE_CONTENTS = {

	{ "PROGRAM",		"type",		"DEC-LIST" },
	{ "PROGRAM",		"",		"" },

	{ "DEC-LIST",		"type",		"DEC DEC-LIST*" },

	{ "DEC-LIST*",		"type",		"DEC DEC-LIST*" },
	{ "DEC-LIST*",		"",		"" },	// This is an effective epsilon transition.

	{ "DEC",		"type",		"type DEC*" },

	{ "DEC*",		"identifier",	"identifier DEC**" },
	{ "DEC*",		"*",		"* identifier DEC**" },

	{ "DEC**",		"(",		"FUN-DEC" },
	{ "DEC**",		"void",		"FUN-DEC" },
	{ "DEC**",		"[",		"VAR-DEC**" },
	{ "DEC**",		";",		"VAR-DEC**" },

	// { "VAR-DEC",		"type",		"type identifier VAR-DEC**" },
	{ "VAR-DEC",		"type",		"type VAR-DEC*" },

	// { "VAR-DEC*",		"[",		"[ integer ] ;" },
	// { "VAR-DEC*",		";",		";" },
	{ "VAR-DEC*",		"*",		"* identifier ;" },
	{ "VAR-DEC*",		"identifier",	"identifier VAR-DEC**" },

	{ "VAR-DEC**",		"[",		"[ integer ] ;" },
	{ "VAR-DEC**",		";",		";" },

	{ "FUN-DEC",		"(",		"( PARAMS ) COMPOUND-STMT" },
	{ "FUN-DEC",		"void",		"void COMPOUND-STMT" },

	{ "PARAMS",		"type",		"PARAM-LIST" },
	{ "PARAMS",		")",		"" },

	{ "PARAM-LIST",		"type",		"PARAM PARAM-LIST*" },

	{ "PARAM-LIST*",	",",		", PARAM PARAM-LIST*" },
	{ "PARAM-LIST*",	")",		"" },

	{ "PARAM",		"type",		"type PARAM*" },

	{ "PARAM*",		"*",		"* identifier" },
	{ "PARAM*",		"identifier",	"identifier PARAM**" },

	{ "PARAM**",		"[",		"[ ]" },
	// { "PARAM**",		",",		", PARAM PARAM-LIST*" }, // old
	{ "PARAM**",		",",		"" },
	{ "PARAM**",		")",		"" },

	{ "COMPOUND-STMT",	"{",		"{ LOCAL-DECS STMT-LIST }" },

	{ "LOCAL-DECS",		"type",		"VAR-DEC LOCAL-DECS" },
	{ "LOCAL-DECS",		"(",		"" },
	{ "LOCAL-DECS",		"identifier",	"" },
	{ "LOCAL-DECS",		"integer",	"" },
	{ "LOCAL-DECS",		"floatpoint",	"" },
	{ "LOCAL-DECS",		"plusorminus",	"" },
	{ "LOCAL-DECS",		"!",		"" },
	{ "LOCAL-DECS",		"&",		"" },
	{ "LOCAL-DECS",		"*",		"" },
	{ "LOCAL-DECS",		"{",		"" },
	{ "LOCAL-DECS",		"while",	"" },
	{ "LOCAL-DECS",		"for",		"" },
	{ "LOCAL-DECS",		"return",	"" },
	{ "LOCAL-DECS",		"if",		"" },
	{ "LOCAL-DECS",		";",		"" },
	{ "LOCAL-DECS",		"}",		"" },	// old

	{ "STMT-LIST",		"(",		"STMT STMT-LIST" },
	{ "STMT-LIST",		"identifier",	"STMT STMT-LIST" },
	{ "STMT-LIST",		"integer",	"STMT STMT-LIST" },
	{ "STMT-LIST",		"floatpoint",	"STMT STMT-LIST" },
	{ "STMT-LIST",		"plusorminus",	"STMT STMT-LIST" },
	{ "STMT-LIST",		"!",		"STMT STMT-LIST" },
	{ "STMT-LIST",		"&",		"STMT STMT-LIST" },
	{ "STMT-LIST",		"*",		"STMT STMT-LIST" },
	{ "STMT-LIST",		"{",		"STMT STMT-LIST" },
	{ "STMT-LIST",		"while",	"STMT STMT-LIST" },
	{ "STMT-LIST",		"for",		"STMT STMT-LIST" },
	{ "STMT-LIST",		"return",	"STMT STMT-LIST" },
	{ "STMT-LIST",		"if",		"STMT STMT-LIST" },
	{ "STMT-LIST",		"}",		"" },	// new

	{ "STMT",		"if",		"IF-STMT" },
	{ "STMT",		"(",		"EXPR-STMT" },
	{ "STMT",		"identifier",	"EXPR-STMT" },
	{ "STMT",		"integer",	"EXPR-STMT" },
	{ "STMT",		"floatpoint",	"EXPR-STMT" },
	{ "STMT",		"plusorminus",	"EXPR-STMT" },
	{ "STMT",		"!",		"EXPR-STMT" },
	{ "STMT",		"&",		"EXPR-STMT" },
	{ "STMT",		"*",		"EXPR-STMT" },
	// { "STMT",		"{",		"NOT-IF-STMT" },
	{ "STMT",		"{",		"COMPOUND-STMT" },	// just added
	{ "STMT",		"while",	"WHILE-STMT" },
	{ "STMT",		"for",		"FOR-STMT" },
	{ "STMT",		"return",	"RETURN-STMT" },
	{ "STMT",		";",		"NOT-IF-STMT" },

	{ "IF-STMT",		"if",		"if ( EXPR ) IF-STMT*" },

	{ "IF-STMT*",		"(",		"STMT AFTER-IF" },
	{ "IF-STMT*",		"identifier",	"STMT AFTER-IF" },
	{ "IF-STMT*",		"integer",	"STMT AFTER-IF" },
	{ "IF-STMT*",		"floatpoint",	"STMT AFTER-IF" },
	{ "IF-STMT*",		"plusorminus",	"STMT AFTER-IF" },
	{ "IF-STMT*",		"!",		"STMT AFTER-IF" },
	{ "IF-STMT*",		"&",		"STMT AFTER-IF" },
	{ "IF-STMT*",		"*",		"STMT AFTER-IF" },
	{ "IF-STMT*",		"{",		"COMPOUND-STMT AFTER-IF" },
	{ "IF-STMT*",		"while",	"STMT AFTER-IF" },
	{ "IF-STMT*",		"for",		"STMT AFTER-IF" },
	{ "IF-STMT*",		"return",	"STMT AFTER-IF" },
	{ "IF-STMT*",		";",		"STMT AFTER-IF" },
	// { "IF-STMT*",		"if",		"CLOSED-IF-STMT else STMT" },	// this is pointless.
	{ "IF-STMT*",		"if",		"IF-STMT AFTER-IF" },	// just added

	{ "AFTER-IF",		"else",		"else STMT" },
	{ "AFTER-IF",		"(",		"" },
	{ "AFTER-IF",		"identifier",	"" },
	{ "AFTER-IF",		"integer",	"" },
	{ "AFTER-IF",		"floatpoint",	"" },
	{ "AFTER-IF",		"plusorminus",	"" },
	{ "AFTER-IF",		"!",		"" },
	{ "AFTER-IF",		"&",		"" },
	{ "AFTER-IF",		"*",		"" },
	{ "AFTER-IF",		"{",		"" },
	{ "AFTER-IF",		"while",	"" },
	{ "AFTER-IF",		"for",		"" },
	{ "AFTER-IF",		"return",	"" },
	{ "AFTER-IF",		";",		"" },
	{ "AFTER-IF",		"if",		"" },
	{ "AFTER-IF",		"}",		"" },

	{ "CLOSED-IF-STMT",	"if",		"if ( EXPR ) IF-STMT*" },	// identical to if-stmt?

	{ "NOT-IF-STMT",	"(",		"EXPR-STMT" },
	{ "NOT-IF-STMT",	"identifier",	"EXPR-STMT" },
	{ "NOT-IF-STMT",	"integer",	"EXPR-STMT" },
	{ "NOT-IF-STMT",	"floatpoint",	"EXPR-STMT" },
	{ "NOT-IF-STMT",	"plusorminus",	"EXPR-STMT" },
	{ "NOT-IF-STMT",	"!",		"EXPR-STMT" },
	{ "NOT-IF-STMT",	"&",		"EXPR-STMT" },
	{ "NOT-IF-STMT",	"*",		"EXPR-STMT" },
	{ "NOT-IF-STMT",	";",		"EXPR-STMT" },
	// { "NOT-IF-STMT",	"{",		"COMPOUND-STMT" },
	{ "NOT-IF-STMT",	"while",	"WHILE-STMT" },
	{ "NOT-IF-STMT",	"for",		"FOR-STMT" },
	{ "NOT-IF-STMT",	"return",	"RETURN-STMT" },

	{ "EXPR-STMT",		"(",		"OPTIONAL-EXPR ;" },
	{ "EXPR-STMT",		"identifier",	"OPTIONAL-EXPR ;" },
	{ "EXPR-STMT",		"integer",	"OPTIONAL-EXPR ;" },
	{ "EXPR-STMT",		"floatpoint",	"OPTIONAL-EXPR ;" },
	{ "EXPR-STMT",		"plusorminus",	"OPTIONAL-EXPR ;" },
	{ "EXPR-STMT",		"!",		"OPTIONAL-EXPR ;" },
	{ "EXPR-STMT",		"&",		"OPTIONAL-EXPR ;" },
	{ "EXPR-STMT",		"*",		"OPTIONAL-EXPR ;" },
	{ "EXPR-STMT",		";",		"OPTIONAL-EXPR ;" },

	{ "OPTIONAL-EXPR",	"(",		"EXPR" },
	{ "OPTIONAL-EXPR",	"identifier",	"EXPR" },
	{ "OPTIONAL-EXPR",	"integer",	"EXPR" },
	{ "OPTIONAL-EXPR",	"floatpoint",	"EXPR" },
	{ "OPTIONAL-EXPR",	"plusorminus",	"EXPR" },
	{ "OPTIONAL-EXPR",	"!",		"EXPR" },
	{ "OPTIONAL-EXPR",	"&",		"EXPR" },
	{ "OPTIONAL-EXPR",	"*",		"EXPR" },
	{ "OPTIONAL-EXPR",	")",		"" },	// new
	{ "OPTIONAL-EXPR",	";",		"" },

	// ------------------------------------------------------------
	{ "EXPR",		"(",		"OR-EXPR EXPR*" },
	{ "EXPR",		"identifier",	"OR-EXPR EXPR*" },
	{ "EXPR",		"integer",	"OR-EXPR EXPR*" },
	{ "EXPR",		"floatpoint",	"OR-EXPR EXPR*" },
	{ "EXPR",		"plusorminus",	"OR-EXPR EXPR*" },
	{ "EXPR",		"!",		"OR-EXPR EXPR*" },
	{ "EXPR",		"&",		"OR-EXPR EXPR*" },
	{ "EXPR",		"*",		"OR-EXPR EXPR*" },
	{ "EXPR",		"]",		"" },	// new
	// { "EXPR",		")",		"" },	// old

	{ "EXPR*",		"assignop",	"assignop OR-EXPR EXPR*" },
	{ "EXPR*",		")",		"" },
	{ "EXPR*",		";",		"" },	// new
	{ "EXPR*",		"]",		"" },	// new
	{ "EXPR*",		",",		"" },	// new

	{ "OR-EXPR",		"(",		"AND-EXPR OR-EXPR*" },
	{ "OR-EXPR",		"identifier",	"AND-EXPR OR-EXPR*" },
	{ "OR-EXPR",		"integer",	"AND-EXPR OR-EXPR*" },
	{ "OR-EXPR",		"floatpoint",	"AND-EXPR OR-EXPR*" },
	{ "OR-EXPR",		"plusorminus",	"AND-EXPR OR-EXPR*" },
	{ "OR-EXPR",		"!",		"AND-EXPR OR-EXPR*" },
	{ "OR-EXPR",		"&",		"AND-EXPR OR-EXPR*" },
	{ "OR-EXPR",		"*",		"AND-EXPR OR-EXPR*" },

	{ "OR-EXPR*",		"||",		"|| AND-EXPR OR-EXPR*" },
	{ "OR-EXPR*",		"assignop",	"" },
	{ "OR-EXPR*",		")",		"" },
	{ "OR-EXPR*",		";",		"" },	// new
	{ "OR-EXPR*",		"]",		"" },	// new
	{ "OR-EXPR*",		",",		"" },	// new

	{ "AND-EXPR",		"(",		"REL-EXPR AND-EXPR*" },
	{ "AND-EXPR",		"identifier",	"REL-EXPR AND-EXPR*" },
	{ "AND-EXPR",		"integer",	"REL-EXPR AND-EXPR*" },
	{ "AND-EXPR",		"floatpoint",	"REL-EXPR AND-EXPR*" },
	{ "AND-EXPR",		"plusorminus",	"REL-EXPR AND-EXPR*" },
	{ "AND-EXPR",		"!",		"REL-EXPR AND-EXPR*" },
	{ "AND-EXPR",		"&",		"REL-EXPR AND-EXPR*" },
	{ "AND-EXPR",		"*",		"REL-EXPR AND-EXPR*" },

	{ "AND-EXPR*",		"&&",		"&& REL-EXPR AND-EXPR*" },
	{ "AND-EXPR*",		"||",		"" },
	{ "AND-EXPR*",		"assignop",	"" },
	{ "AND-EXPR*",		")",		"" },
	{ "AND-EXPR*",		";",		"" },	// new
	{ "AND-EXPR*",		"]",		"" },	// new
	{ "AND-EXPR*",		",",		"" },	// new

	{ "REL-EXPR",		"(",		"ADD-EXPR REL-EXPR*" },
	{ "REL-EXPR",		"identifier",	"ADD-EXPR REL-EXPR*" },
	{ "REL-EXPR",		"integer",	"ADD-EXPR REL-EXPR*" },
	{ "REL-EXPR",		"floatpoint",	"ADD-EXPR REL-EXPR*" },
	{ "REL-EXPR",		"plusorminus",	"ADD-EXPR REL-EXPR*" },
	{ "REL-EXPR",		"!",		"ADD-EXPR REL-EXPR*" },
	{ "REL-EXPR",		"&",		"ADD-EXPR REL-EXPR*" },
	{ "REL-EXPR",		"*",		"ADD-EXPR REL-EXPR*" },

	{ "REL-EXPR*",		"relop",	"relop ADD-EXPR REL-EXPR*" },
	{ "REL-EXPR*",		"&&"	,	"" },
	{ "REL-EXPR*",		"||",		"" },
	{ "REL-EXPR*",		"assignop",	"" },
	{ "REL-EXPR*",		")",		"" },
	{ "REL-EXPR*",		";",		"" },	// new
	{ "REL-EXPR*",		"]",		"" },	// new
	{ "REL-EXPR*",		",",		"" },	// new

	{ "ADD-EXPR",		"(",		"TERM ADD-EXPR*" },
	{ "ADD-EXPR",		"identifier",	"TERM ADD-EXPR*" },
	{ "ADD-EXPR",		"integer",	"TERM ADD-EXPR*" },
	{ "ADD-EXPR",		"floatpoint",	"TERM ADD-EXPR*" },
	{ "ADD-EXPR",		"plusorminus",	"TERM ADD-EXPR*" },
	{ "ADD-EXPR",		"!",		"TERM ADD-EXPR*" },
	{ "ADD-EXPR",		"&",		"TERM ADD-EXPR*" },
	{ "ADD-EXPR",		"*",		"TERM ADD-EXPR*" },

	{ "ADD-EXPR*",		"plusorminus",	"plusorminus TERM ADD-EXPR*" },
	{ "ADD-EXPR*",		"relop",	"" },
	{ "ADD-EXPR*",		"&&"	,	"" },
	{ "ADD-EXPR*",		"||",		"" },
	{ "ADD-EXPR*",		"assignop",	"" },
	{ "ADD-EXPR*",		")",		"" },
	{ "ADD-EXPR*",		";",		"" },	// new
	{ "ADD-EXPR*",		"]",		"" },	// new
	{ "ADD-EXPR*",		",",		"" },	// new

	{ "TERM",		"(",		"UNARY-EXPR TERM*" },
	{ "TERM",		"identifier",	"UNARY-EXPR TERM*" },
	{ "TERM",		"integer",	"UNARY-EXPR TERM*" },
	{ "TERM",		"floatpoint",	"UNARY-EXPR TERM*" },
	{ "TERM",		"plusorminus",	"UNARY-EXPR TERM*" },
	{ "TERM",		"!",		"UNARY-EXPR TERM*" },
	{ "TERM",		"&",		"UNARY-EXPR TERM*" },
	{ "TERM",		"*",		"UNARY-EXPR TERM*" },

	{ "TERM*",		"*",		"* UNARY-EXPR TERM*" },
	{ "TERM*",		"divmodop",	"divmodop UNARY-EXPR TERM*" },
	{ "TERM*",		"plusorminus",	"" },
	{ "TERM*",		"relop",	"" },
	{ "TERM*",		"&&"	,	"" },
	{ "TERM*",		"||",		"" },
	{ "TERM*",		"assignop",	"" },
	{ "TERM*",		")",		"" },
	{ "TERM*",		";",		"" },	// new
	{ "TERM*",		"]",		"" },	// new
	{ "TERM*",		",",		"" },	// new, to appease function call parameters

	{ "UNARY-EXPR",		"(",		"POSTFIX-EXPR" },
	{ "UNARY-EXPR",		"identifier",	"POSTFIX-EXPR" },
	{ "UNARY-EXPR",		"integer",	"POSTFIX-EXPR" },
	{ "UNARY-EXPR",		"floatpoint",	"POSTFIX-EXPR" },
	{ "UNARY-EXPR",		"plusorminus",	"plusorminus UNARY-EXPR" },
	{ "UNARY-EXPR",		"!",		"! UNARY-EXPR" },
	{ "UNARY-EXPR",		"&",		"& UNARY-EXPR" },
	{ "UNARY-EXPR",		"*",		"* UNARY-EXPR" },

	{ "POSTFIX-EXPR",	"identifier",	"PRIMARY-EXPR POSTFIX-EXPR*" },
	{ "POSTFIX-EXPR",	"(",		"PRIMARY-EXPR POSTFIX-EXPR*" },
	{ "POSTFIX-EXPR",	"integer",	"PRIMARY-EXPR POSTFIX-EXPR*" },
	{ "POSTFIX-EXPR",	"floatpoint",	"PRIMARY-EXPR POSTFIX-EXPR*" },

	{ "POSTFIX-EXPR*",	"[",		"[ EXPR ]" },
	{ "POSTFIX-EXPR*",	"postfixop",	"postfixop" },
	{ "POSTFIX-EXPR*",	"*",		"" },
	{ "POSTFIX-EXPR*",	"divmodop",	"" },
	{ "POSTFIX-EXPR*",	"plusorminus",	"" },
	{ "POSTFIX-EXPR*",	"relop",	"" },
	{ "POSTFIX-EXPR*",	"&&"	,	"" },
	{ "POSTFIX-EXPR*",	"||",		"" },
	{ "POSTFIX-EXPR*",	"assignop",	"" },
	{ "POSTFIX-EXPR*",	")",		"" },
	{ "POSTFIX-EXPR*",	";",		"" },	// new
	{ "POSTFIX-EXPR*",	"]",		"" },	// new
	{ "POSTFIX-EXPR*",	",",		"" },	// new, to appease function call parameters

	{ "PRIMARY-EXPR",	"identifier",	"identifier PRIMARY-EXPR*" },
	{ "PRIMARY-EXPR",	"(",		"( EXPR )" },
	{ "PRIMARY-EXPR",	"integer",	"integer" },
	{ "PRIMARY-EXPR",	"floatpoint",	"floatpoint" },
	{ "PRIMARY-EXPR",	"[",		"" }, // all new
	{ "PRIMARY-EXPR",	"postfixop",	"" },
	{ "PRIMARY-EXPR",	"*",		"" },
	{ "PRIMARY-EXPR",	"divmodop",	"" },
	{ "PRIMARY-EXPR",	"plusorminus",	"" },
	{ "PRIMARY-EXPR",	"relop",	"" },
	{ "PRIMARY-EXPR",	"&&"	,	"" },
	{ "PRIMARY-EXPR",	"||",		"" },
	{ "PRIMARY-EXPR",	"assignop",	"" },
	{ "PRIMARY-EXPR",	")",		"" },
	{ "PRIMARY-EXPR",	";",		"" },

	{ "PRIMARY-EXPR*",	"(",		"CALL" },
	{ "PRIMARY-EXPR*",	"postfixop",	"" },
	{ "PRIMARY-EXPR*",	"*",		"" },
	{ "PRIMARY-EXPR*",	"divmodop",	"" },
	{ "PRIMARY-EXPR*",	"plusorminus",	"" },
	{ "PRIMARY-EXPR*",	"relop",	"" },
	{ "PRIMARY-EXPR*",	"&&"	,	"" },
	{ "PRIMARY-EXPR*",	"||",		"" },
	{ "PRIMARY-EXPR*",	"assignop",	"" },
	{ "PRIMARY-EXPR*",	")",		"" },
	{ "PRIMARY-EXPR*",	";",		"" },	// new
	{ "PRIMARY-EXPR*",	"[",		"" },
	{ "PRIMARY-EXPR*",	"]",		"" },
	// { "PRIMARY-EXPR*",	",",		", PRIMARY-EXPR" }, //new, to appease function call parameters...OKAY this isn't right....
	{ "PRIMARY-EXPR*",	",",		"" },

	{ "CALL",		"(",		"( ARGS )" },

	{ "ARGS",		"identifier",	"ARG-LIST" },
	{ "ARGS",		"floatpoint",	"ARG-LIST" },
	{ "ARGS",		"integer",	"ARG-LIST" },
	{ "ARGS",		"(",		"ARG-LIST" },
	{ "ARGS",		"plusorminus",	"ARG-LIST" },
	{ "ARGS",		"!",		"ARG-LIST" },
	{ "ARGS",		"&",		"ARG-LIST" },
	{ "ARGS",		"*",		"ARG-LIST" },
	{ "ARGS",		")",		"" },

	{ "ARG-LIST",		"identifier",	"EXPR ARG-LIST*" },
	{ "ARG-LIST",		"floatpoint",	"EXPR ARG-LIST*" },
	{ "ARG-LIST",		"integer",	"EXPR ARG-LIST*" },
	{ "ARG-LIST",		"(",		"EXPR ARG-LIST*" },
	{ "ARG-LIST",		"plusorminus",	"EXPR ARG-LIST*" },
	{ "ARG-LIST",		"!",		"EXPR ARG-LIST*" },
	{ "ARG-LIST",		"&",		"EXPR ARG-LIST*" },
	{ "ARG-LIST",		"*",		"EXPR ARG-LIST*" },

	{ "ARG-LIST*",		",",		", EXPR ARG-LIST*" },
	{ "ARG-LIST*",		")",		"" },

	// ------------------------------------------------------------

	{ "WHILE-STMT",		"while",	"while ( EXPR ) STMT" },

	{ "FOR-STMT",		"for",		"for ( OPTIONAL-EXPR ; OPTIONAL-EXPR ; OPTIONAL-EXPR ) STMT" },

	{ "RETURN-STMT",	"return",	"return RETURN-STMT*" },

	{ "RETURN-STMT*",	";",		";" },
	{ "RETURN-STMT*",	"identifier",	"EXPR ;" },
	{ "RETURN-STMT*",	"integer",	"EXPR ;" },
	{ "RETURN-STMT*",	"floatpoint",	"EXPR ;" },
	{ "RETURN-STMT*",	"(",		"EXPR ;" },
	{ "RETURN-STMT*",	"plusorminus",	"EXPR ;" },
	{ "RETURN-STMT*",	"!",		"EXPR ;" },
	{ "RETURN-STMT*",	"&",		"EXPR ;" },
	{ "RETURN-STMT*",	"*",		"EXPR ;" },

    };

}
