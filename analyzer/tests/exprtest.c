int main() {
    int i1;
    int i2;
    int *ip1;
    int *ip2;
    float f1;
    float f2;
    float *fp1;
    float *fp2;
    int a[10];

	/* Check arithmetic */
	i1 = i2 + (i2+ (i1 - *i2));
	/* This is wrong */
	&i1 = ip2;
/* So is this */
	*ip2 = i2 + fp2;


    &f2 = v;
}
