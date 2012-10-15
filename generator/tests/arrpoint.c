int doubleit(int *arr, int len) {
    int i;
    for ( i=0; i<len; i++ ) {
	arr[i] *= 2;
    }
}

int main() {
    int a[5];

    int i;
    for ( i=0; i<5; i++ )
	a[i] = i+1;

    doubleit( a, 5 );
    
}
    
