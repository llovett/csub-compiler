int i1;
int i2;
int ia[10];
int* pi1;
int* pi2;
int* pia[10];
float f1;
float f2;
float fa1[10];
float* pf1;
float* pf2;
float* pfa1[10];
void v1;
void v2;
void va1[10];
void* pv1;
void* pv2;
void* pva1[10];

/* RAN - Expr in if statment doesn't seem to be being checked for voidocity*/

void test_func8(){
    i2 = 5;
}

int* test_func9(int word[]){
    i2 = 5;
}

int test_ifs(int i3, int i4){

    i1 = 3;
    i2 = 4;

    /*TEST 1 - EXPR in an if statement cannot be void*/

    /*ACCEPT*/ /*PASS*/
    if (i1 < i2) {
	i1++;
	i2++;
	if( (i1 < i2) && (i1 > i2) || (i1 == i2) ){
	    test_func8();
	}
	while( i1 < i2 ){
	    i1++;
	}
    }
    else {
	int i;
	for(i = 0; i< 45; i++){
	    i2 = 2*i + 5;
	}
    }

    /*REJECT*/ /*PASS? our parser takes care of this */
    /* if( void ) { */
    /* 	i1++; */
    /* } */

    if( test_func8() ) { /* FAIL */
	int qwert;
    	i1++;
    }
    /* while ( test_func8() ) {	/\* FAIL *\/ */
    /* 	i1++; */
    /* } */
    
    return i1;
}





