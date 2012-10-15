int pbr2( int * pointer ) {
    *pointer = 4;
}

int main() {
    int num;
    num = 100;
    pbr2( &num );
}
