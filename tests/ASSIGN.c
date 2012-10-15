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

/*RAN - Chained assignments need some debugging
 *    - numbers are able to be lvalues
 */

int test_assign(int i3, int i4){

    /* TEST 1 - checking for compatible types, lvalues, and rvalues */

    /*ACCEPT*/
    i1 = 3; /*PASS*/
    i2 = 4; /*PASS*/
    i1 = i2; /*PASS*/
    pf1 = pf2; /*PASS*/

    pf1 = &f1; /*PASS*/
    f2 = *pf2; /*PASS*/
    *pf1 = f1; /*PASS*/

    /*REJECT*/
    /* i1 = pf1; */ /*PASS*/
    
    /* pf1 = f1; */ /*PASS*/
    /* f2 = pf2; */ /*PASS*/

    /* 5 = i1; */ /*PASS*/



    /* TEST 2 - chained assignments */

    /*ACCEPT*/
    i1 = 5; /*PASS*/
    i1 = i2 = 6; /*PASS*/
    i1 = i2 = i3 = i4; /*PASS*/
    i1 = i2 = i3 = i4 = 1337; /*PASS*/
    i1 = i2 += i4;

    /*REJECT*/

    /* pf1 = pf2 = &v1; /\*PASS*\/ */
    /* i1 = i2 = pf1; /\***FAIL***\/ */
    /* i1 = i2 = &v1; /\* FAIL *\/ */
    /* i1 = pf1 = pf2; /\* PASS *\/ */
    /* 1337 = i1 = i2 = i3 = i4; /\* FAIL *\/ */
    /* i1 = i2 = i3 = i4 = 3.5; /\* FAIL *\/ */

    /*TEST 3 - pointers cannot be l-values except void* */

    /*ACCEPT*/
    pv1 = pi1; /*PASS*/

    /*REJECT*/
    /* pf1 = pf2 = 4; /\* FAIL *\/ */
    /* pf1 = pv1; */ /*PASS*/
    /* ia = 10; */ /*PASS*/
    /* *pfa1 = 4.5; */ /*PASS*/
    
    return i1;
}
