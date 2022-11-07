#include <iostream>

int info(int arr[]){//传递进来的arr，是一个新指针，指向原始arr数组的首地址
    printf("arr的内存空间总大小：%d \n", sizeof(arr));//指针的大小是8位
    printf("arr的首个元素内存空间大小：%d \n", sizeof(arr[0]));//指针实际指向的数据类型还是int，4位
    printf("arr的元素个数：%d \n", sizeof(arr) / sizeof(arr[0]));
    printf("arr的首个元素地址：%p \n", arr); //%p是打印地址（指针地址）的，是十六进制的形式，但是会全部打完
    printf("arr指针的地址：%p \n", &arr); //实际指针地址
    int* arr0 = arr;
    int** arr01 = &arr;
    int* arr1 = arr+1;
    int** arr2 = &arr + 1;
    printf("arr的下一个元素地址：%d \n", (int)(size_t)(arr+1) - (int)(size_t)arr);//步长是一个数据类型，arr还是原始数组的首地址
    printf("arr的下一个元素地址：%d \n", (int)(size_t)(&arr+1) - (int)(size_t)&arr);//步长是一个指针类型 = 8，
    printf("&arr的下一个元素地址：%d\n", (int)(size_t)(&arr + 1) - (int)(size_t)arr);//步长不可控，arr是另一个指针，&arr取的是新指针的地址，这个地址分配位置不一定
    printf("&arr的下一个元素地址：%d\n", (int)(size_t)(arr + 1) - (int)(size_t)&arr);//步长不可控，arr是另一个指针，指向原始数组，&arr取的是新指针的地址，这个地址分配位置不一定
    printf("&arr的下一个元素地址：%p\n", &arr);//arr已经是一个新地址了，这个值不可控
}

int main() {
//    int a = 100;
//    int *b = &a;
//    printf("a 地址位置 %d\n", b);
//    printf("a 的值 %d\n", *b);
//    *b = 10; //可以操作指针指向地址的值
//    //b = 10;//不可用改变地址本身的值
//
//    int *x;
//    char *y;
//    long *z;
//    printf("指针大小：%d %d %d", sizeof(*x), sizeof(*y), sizeof(*z));
//    printf("指针大小：%d %d %d", sizeof(x), sizeof(y), sizeof(z));
//    printf("指针大小：%d %d %d", sizeof(int), sizeof(char), sizeof(long));
//    printf("指针大小：%d %d %d", sizeof(int *), sizeof(char *), sizeof(long*));
//    //std::cout << "Hello, World!" << std::endl;
//
//    int m = 10;
//    int n = 100;
//    int* p1 = &m;
//    int* p2 = &n;
//    int r = p1 - p2;
//    printf("\nr = %d", r);
//    char o = 1000;
//    char * p3 = &o;
//    printf("\nm ,n , o 的地址为：%x, %x, %x", p1, p2, p3);
//    int r2 = (int*)p3 - p1;
//    printf("\nr2 = %d", r2);
//
//    int r3 = p3 - (char*)p1;
//    printf("\nr3 = %d", r3);

//    int a = 1;
//    int b = 2;
//    int c = 3;
//    char d = 4;
//    int e = 5;
//
//    printf("a的地址：%x,b的地址：%x,c的地址：%x,d的地址：%x,e的地址：%x,", &a, &b, &c, &d, &e);

    int arr[10] = {1,2,3,4,5,6,7,8,9,10};
    printf("arr的内存空间总大小：%d \n", sizeof(arr));
    printf("arr的首个元素内存空间大小：%d \n", sizeof(arr[0]));
    printf("arr的元素个数：%d \n", sizeof(arr) / sizeof(arr[0]));
    printf("arr的首个元素地址：%p \n", arr); //%p是打印地址（指针地址）的，是十六进制的形式，但是会全部打完
    printf("arr指针的地址：%p \n", &arr); //实际指针地址
    int* arr0 = arr;
    int(*arr01)[10] = &arr;
    int* arr1 = arr+1;
    printf("arr的下一个元素地址：%d \n", (int)(size_t)(arr+1) - (int)(size_t)arr);//步长是一个数据类型
    int(*arr2)[10] = &arr + 1;
    printf("&arr的下一个元素地址：%d\n", (int)(size_t)(&arr + 1) - (int)(size_t)arr);//步长是当前数组的大小

    printf("***********************************\n");
    info(arr);
    return 0;
}
