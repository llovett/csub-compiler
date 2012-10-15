package generator;

public class Label extends CodeEntry {
    
    private String name;
    private static int count;
    private int myCount;
    private boolean function;

    public Label() {
	this("", false);
    }

    public Label( String name ) {
	this( name, false );
    }

    public Label( String name, boolean function ) {
	myCount = count++;
	this.name = name;
	this.function = function;
    }

    public boolean isFunction() {
	return function;
    }

    public String name() {
	if ( name.equals("") )
	    return "L"+myCount;
	return name;
    }

    public String toString() {
	return name()+":";
    }

}
