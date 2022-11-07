//
// Created by zander on 2022/10/12.
//
using namespace std;
#include <stdio.h>
#include <iostream>

namespace {
    int a = 1;
}

namespace abc{
    namespace abc2{
        int a = 3;
    }
    int a = 2;
}

int main(){
    printf("%d \n", a);
    printf("%d \n", ::a);
    printf("%d \n", abc::a);
    printf("%d \n", abc::abc2::a);
    cout<<" "<<endl;
    return 0;
}