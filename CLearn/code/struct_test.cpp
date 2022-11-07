#include <iostream>

//
// Created by zander on 2022/10/11.
//
typedef struct Stu{
    char name[20];
    int age;
}Stud;
struct Point{
    int x;
    int y;
    int c[10];
}Point;

typedef struct N{
    int c;
    char a;
    char b;
}N;

int main(){
    Stu s;
    Stud s1;
    printf("Point struct size = %d \n", (int)sizeof(Point));
    printf("N struct size = %d \n", (int)sizeof(N));

    return 0;
}
