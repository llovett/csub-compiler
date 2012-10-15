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

/*RAN - return types aren't being verified when functions are declared
 *    - return type isn't being verified after a function is called
 *    - existence of return or lack thereof is not being checked
 */


/*TEST 1 - Compare function type and return types in declaration*/

/*ACCEPT*/ /*PASS*/
int test_func(int i3, int i4){
    
    return i3;
}

/*REJECT*/
 /***FAIL***/
/* float test_func1(int i3, int i4){ */
/*     return i3; */
/* } */


 /***FAIL***/
/* int test_func2(int i3, int i4){ */

/*    f1 = 1.337; */
   
/*    return f1; */
/* } */

 /***FAIL***/
/* int test_func2(int i3, int i4){ */
/*    f1 = 1.337; */
/*    return 4.5; */
/* } */


/*TEST 2 - Compare function returns after calls*/

/*ACCEPT*/ /*PASS*/
int test_func3(int liz, int luke){
    i2 = test_func(liz, luke);
    return i2;
}

/*REJECT*/

float test_func4_helper(int i3, int i4){
    return f2;
}


 /***FAIL***/
/* int test_func4(int liz, int luke){ */
/*     f2 = test_func4_helper(liz, luke); */
/*     return f2; */
/* } */

 /***FAIL***/
/* int test_func4(int liz, int luke){ */
/*     i2 = test_func4_helper(liz, luke); */
/*     return i2; */
/* } */



/*TEST 3 - A function must be called with the correct number of args and correct type of args*/

/*REJECT*/
 /*PASS*/
/* int test_func5(int liz, int luke){ */
    /* i2 = test_func(liz); */
    /* return i2; */
/* } */

 /*PASS*/
/* float test_func6(int liz, int luke){ */
    /* f2 = 3.1415; */
    /* i2 = test_func(liz, f2); */
    /* return f2; */
/* } */


/*TEST 4 - A function with void return type must not return anything. Any other return type must return something*/

/*ACCEPT*/ /*PASS*/
void fest_tunc7(int liz, int luke){
    i2 = liz + luke;
}

void test_func8(){
}

/*REJECT*/

 /***FAIL***/
/* void test_func9(float liz, float luke){ */
/*     f2 = liz+luke; */
/*     return f2; */
/* } */

 /***FAIL***/
/* int test_func10(float liz, float luke){ */
/*     f2 = liz+luke; */
/* } */

