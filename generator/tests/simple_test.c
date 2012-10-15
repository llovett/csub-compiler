int x;
int a;
int b;
int foo(int f, int* blarg){
    /*a = 3;
    b = 6;
    f = 16;


    x = a + b;
    x = a + b + f + x;
    */

    /*x = a && b && x;
    x = a || b || x;
    a = x || f || 0;
    */
    /*a = b + x;*/
    /*a = b * -x++;*/
	/*a = -x;*/

    return a;
}

int *ret_arr() {
    int c[5];
    return c;
}

int drink(int beers, int winez) {
    return beers * winez;
}

int main(int f) {

    int c;
    int b;

    /* c = 16; */
    /* x = a + b + f + x + c; */
 
    c = ret_arr()[2];
    c += drink(100*2, 234235);
    ret_arr();

	/*a = ip[a+x];*/
	/*a = b++;*/
	/*a = x--;*/
	a = b <= x == f;
	/*a = (x + b);*/
    /* return foo( 2, ip ); */
}
