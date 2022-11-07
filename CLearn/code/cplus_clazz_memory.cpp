//
// Created by zander on 2022/10/12.
//
#include <iostream>

using namespace std;
class b{

};

int main(){
    b b1;
    b b2 = *new b();

    cout<< &b1 << "\n" << &b2 << "\n" << new b()<< "\n" << new b() <<endl;
    return 0;
}