int dub(int *a, int size) {
    int i;
    int sum;
    sum=0;
    for ( i=0; i<size; i++) {
	sum += a[i];
	a[i] *= 2;
    }

    return sum;
    
}

void assignpoint(int *p) {
    *p = 8000;
}

int main() {
    int arr[5];
    int arr2[5];
    int point;
    int i;
    int sum;
    for ( i=0; i<5; i++ ) {
	arr2[i] = i;
	assignpoint(&point);
	arr[i] = 3*i;
    }

    sum = dub(arr,5);

    sum = 2-3;
}
