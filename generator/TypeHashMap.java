package generator;
import java.util.*;
public class TypeHashMap extends HashMap<String, Integer> implements TypeConstants{

    static final long serialVersionUID = -3329144915473981636L;

    public TypeHashMap(){

	//
	//Note that all *'s in the integer constants have been replaces with $'s
	//

	put("FUN-DEC", FUN_DEC);
	put("VAR-DEC", VAR_DEC);
	put("VAR-DEC*", VAR_DEC$);
	put("VAR-DEC**", VAR_DEC$$);
	put("ASSIGN-EXPR", ASSIGN_EXPR);	
	put("ASSIGN-EXPR*", ASSIGN_EXPR$);	
	put("integer", INTEGER);
	put("identifier", IDENTIFIER);
	put("NO-PARAMS", NO_PARAMS);
	put("PARAM", PARAM);
	put("EXPR", EXPR);
	put("EXPR*", EXPR$);
	put("ADD-EXPR", ADD_EXPR);
	put("ADD-EXPR*", ADD_EXPR$);
	put("AND-EXPR", AND_EXPR);
	put("AND-EXPR*", AND_EXPR$);
	put("OR-EXPR", OR_EXPR);
	put("OR-EXPR*", OR_EXPR$);
	put("TERM", TERM);
	put("TERM*", TERM$);
	put("UNARY-EXPR", UNARY_EXPR);
	put("POSTFIX-EXPR", POSTFIX_EXPR);
	put("POSTFIX-EXPR*", POSTFIX_EXPR$);
	put("PRIMARY-EXPR", PRIMARY_EXPR);
	put("REL-EXPR", REL_EXPR);
	put("REL-EXPR*", REL_EXPR$);
	put("CALL", CALL);
	put("ARGS", ARGS);
	put("IF-STMT", IF_STMT);
	put("WHILE-STMT", WHILE_STMT);
	put("FOR-STMT", FOR_STMT);
	put("RETURN-STMT", RETURN_STMT);
    }

    public void put(int num, String type){
	this.put(type, num);

    }

    public int get(String type){
	
	return this.get(type);

    }

}
