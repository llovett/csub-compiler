int pbr( int *one, int two ) {
    one[0] *= two;
    return one[1];
}

int main() {
    int a[2];
    int b;
    a[0] = 1;
    a[1] = 2;

    b = pbr( a, 20 );
}
