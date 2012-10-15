int pbr(int *one, int two) {
    one[0] += two;
    return one[1];
}

int main() {
    int b;
    int arr[2];
    arr[0] = 1;
    arr[1] = 2;
    
    b = pbr( arr, 20 );
}
