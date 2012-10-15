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

/* RAN - Relational ops don't always return int
 *	 Logical ops don't always return ints
 */

/* Relational operators (==,!=,<,>,<=,>=) may be applied to operands of
   the same type.  The result is of type int. */

int test_relops(int i3, int i4){

    /*setup*/
    f1 = 5.5;
    f2 = 10.5;
    i1 = 1;
    i2 = 2;
    pf1 = &f1;
    pf2 = &f2;
    pi1 = &i1;
    pi2 = &i2;

    /*TEST 1 - Relation operators applied to operands of the same type; the result is an int*/

    /*ACCEPT*/
    /* i1 = f1 < f2; /\* FAIL *\/ */
    /* i2 = f1 >= f2; /\* FAIL *\/ */
    /* i3 = f1 == f2; /\* FAIL *\/ */
    /* i4 = i1 != i2; /\* PASS *\/ */
    /* i1 != i2; /\* PASS *\/ */

    /*REJECT*/

    /* f1 = i1 < i2; /\* PASS *\/ */
    /* f1 = f2 < 5.5; /\* FAIL *\/ */
    /* f1 = i1 == i2; /\* PASS *\/ */
    /* i1 = i1 != f2; /\* PASS *\/ */
    /* i2 = f1 >= i1; /\* PASS *\/ */


    /* TEST 2 - Result of logical operations should be an int; operands can be of any type*/

    /*ACCEPT*/
    i1 = i2 && pv1; /*PASS*/
    i2 = i1 || f2; /*PASS*/
    i1 = !v1; /* FAIL */

    /*REJECT*/
    /* f2 = i1 || f2; /\* FAIL *\/ */
    /* f1 = i2 && pf1; /\* FAIL *\/ */
    /* v1 = !v2; /\* FAIL *\/ */
    /* f1 = i1 && i2 && pf2;	/\* FAIL *\/ */

    
    return i1;
}



