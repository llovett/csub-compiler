int main() {
    if ( a < b ) {
	if ( b < c )
	    b = c = a = 4;
	else if ( b == c )
	    c = 2;
	else
	    c = b;
    }
    else if ( b < a ) {
	b = a;
    }
    else if ( b == a )
	if ( b == 2 )
	    b = 4;
	else
	    b = 2;
    else
	c = 9;
}
	
	
