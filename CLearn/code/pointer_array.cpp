#include <iostream>
//
// Created by zander on 2022/10/9.
//
int main() {
    int a[5] = {1,2,3,4,5};
    int b[5] = {1,2,3,4,5};
    int c[5] = {1,2,3,4,5};
    int* p[] = {a,b,c};//指针数组
    for(int i = 0; i < 3; i++){
        for(int j = 0; j < 5; j++){
            printf("%d", *(p[i]+j));//p[i][j]
        }
        printf("\n");
    }
    return 0;
}
