int glob;

int fun1( int a ) {
    return a+4;
}

int fun2( int a, int b ) {
    return a*a+b;
}

int main() {
    int var;

    fun1(100);
    fun2(-234, glob);
    fun1(var+3);
}
