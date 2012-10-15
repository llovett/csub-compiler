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

/*RAN - Chained assignments don't get checked after the first two terms 
 *    - it's possible to apply & to things it shouldn't be applied to, i.e. numbers
 *
 */

int test_deref(int i3, int i4){

    /*TEST 1 - Only dereference int* and float*; the result is always an l-value*/

    /*ACCEPT*/
    i1 = *pi1; /*PASS*/
    f1 = *pf1; /*PASS*/
    *pi1 = i1; /*PASS*/
    i2 = *pi1 = i1; /*PASS*/
    pv1 = &v1; /*PASS*/
    pf1 = pfa1[3];
    v1 = *pv1;

    /*REJECT*/
    /* *f1 = pf1; */ /*PASS*/
    /* i1 = *f1; */ /*PASS*/
    /* i1 = *pf1; */ /*PASS*/
    /* pv1 = *v1; */ /*PASS*/
    /* pi1 = pfa1[3]; */ /*PASS*/
    /* pi1 = pi2 = pfa1[3]; /\***FAIL***\/ */



    /*TEST 2 - The address of operator only applied to l-value of ints or floats*/
    
    /*ACCEP*/
    pi1 = &i1; /*PASS*/
    pv1 = &v1; /*PASS*/
    pi1 = 5 + &i1;

    /*REJECT*/
    /* int foo () { } */ /*PASS*/
    /* pi1 = &f1; */ /*PASS*/
    /* pi1 = &pi2; */ /*PASS*/
    /* pi1 = pi2 = &pi2; /\* FAIL *\/ */
    /* &i1 = *pf1; */ /*PASS*/
    /* &i1 = pi1; */ /*PASS*/
    /* pi1 = &5; /\***FAIL***\/ */
    /* &5 = pi1; */ /*PASS*/
    /* pi1 = 5 + &f1; */ /*PASS*/
    pi1 = 5 + 7 + &f1; /***FAIL***/

    
    return i1;
}

