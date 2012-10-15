int i1;
int i2;
int ia1[10];
int* pi1;
int* pi2;
int* pia1[10];
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

/*RUN - chained arithmetic expressions don't seem to be catching errors that happen after the first two operands **FIXED
 */

int test_arithmetic(int i3, int i4){
    /*setup*/
    f1 = 5.5;
    f2 = 10.5;
    i1 = 1;
    i2 = 2;
    pf1 = &f1;
    pf2 = &f2;
    pi1 = &i1;
    pi2 = &i2;

    /*TEST 0 Check types of numbers and assignment of the constant zero*/

    /*ACCEPT*/
    i1 = 5; /*PASS*/
    f1 = -5.5; /*PASS*/
    f2 = 5.5; /*PASS*/
    i1 = 0; /*PASS*/
    pv1 = 0; /*PASS*/
    pf1 = 0; /*PASS*/
    pi1 = 0; /*PASS*/
    
    /*REJECT*/
    
    /* i1 = 5.5; */ /*PASS*/
    /* i1 = -5.5; */ /*PASS*/
    /* f1 = 1; */ /*PASS*/
    /* f2 = -1; */ /*PASS*/
    /* f1 = 0; */ /*PASS*/
    


    /*TEST 1 Check arithmetic between pointers and non-pointers*/

    /*ACCEPT*/
    
    pi1 = pi2 + i2;	/* int pointer plus int */ /*PASS*/
    pf1 = i1 + pf2;	/* int plust float pointer */ /*PASS*/
    pv1 = pf2;		/* any pointer can be assigned to void* */ /*PASS*/
    i1 = i3+i4; /*PASS*/
    f1 = f2 + 3.5; /*PASS*/
    pi1 = pi2 + 4; /*PASS*/
    pi1 = i1 + pi2;  /*PASS*/
    pf1 = pf2 + 5; /*PASS*/
    pf1 = pf2 - 5; /*PASS*/
    pf2 = i3 + pf1; /*PASS*/
    
    /*REJECT*/
    /* cannot assign void pointer */
    /* pf1 = pv1;		 */ /*PASS*/
    /* cannot add float to pointer to get pointer */
    /* pi1 = f1 + pi2; */ /*PASS*/
    /*i1 = i3 + i4 + f2;*/  /*PASS*/
    /* i1 = i3 + i4 + pi2; */ /*PASS*/
    /* pi1 = pi2 + 4.5; */ /*PASS*/
    /* i2 = i1 + pi2;  */ /*PASS*/
    /* pf1 = 5 - pf2; */ /*PASS*/
    /* pi2 = pf1 + i1; */ /*PASS*/
    /* pi1 = i3 - pi2; */ /*PASS*/


    /*TEST 2 - Mod operator only applied to ints*/
    
    /*ACCEPT*/
    i1 = 10; /*PASS*/
    i2 = i1%5; /*PASS*/

    /*REJECT*/
    /* i1 = 10.5; */ /*PASS*/
    /* f2 = 10.5%2; */ /*PASS*/
    /* f2 = i1%5; */ /*PASS*/

    
    /*TEST 3 - For each operator: what are the data types it accepts? */
    
    /*ACCEPT*/
    /*      int         int     int - int   */
    i1 = (i1 + *pi1 ) / (*pi1 / ia1[2] - i1); /*PASS*/
    f2 = (fa1[1] + fa1[5]) / (*pf1 - f2); /*PASS*/
    ia1[9] = i1; /*PASS*/
    pi1 = (pi2 + 4); /*PASS*/
    pf1 = i2 + 5 + pf2; /*PASS*/
    pf1 = pf2 - (i2 + i1); /*PASS*/

    /*REJECT*/
    /* pi1 = i2 + 5 + pf2; */ /*PASS*/
    /* f1 = (i1 + *pi1 ) / (*pi1 / ia1[2] - i1); */ /*PASS*/
    /* i1 = (i1 + pi1 ) / 5; */ /*PASS*/
    /* i1 = i2 / f2; */ /*PASS*/
    /* f2 = i2 + f1; */ /*PASS*/
    /* f2 = i2 * f1; */ /*PASS*/
    /* f2 = i2 - f1; */ /*PASS*/
    /* v2 = f2 * f1 * 3.5; */ /*PASS*/
    /* i2 = i2 - i1 - pv1; */  /*PASS*/
    /* i2 = i2 - i1 - f2 - 5.5; */ /*PASS*/
    /* f2 = f2 * f1 * v1; /\* PASS *\/ */
    /* v2 = f2 * f1 * v1; /\* PASS *\/ */
    /* v2 = v2 * v1 * v1; /\* PASS *\/ */

    /* REJECT */
    /* i1 = i1 + f2 + *pi1 + i2; */

    

    return i1 == (*pi1++);
}


