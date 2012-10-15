package generator;

public class Tuple extends CodeEntry {
    
    public String place;
    public String arg1;
    public String arg2;
    public String op;

    /**
     * CONSTRUCTOR
     *
     * */
    public Tuple( String op, String place, String arg1, String arg2 ) {
	this.place = place;
	this.arg1 = arg1;
	this.arg2 = arg2;
	this.op = op;
    }

    public String toString() {
	return String.format("%s\t%s\t%s\t%s", op,  place, arg1, arg2 );
    }

}
