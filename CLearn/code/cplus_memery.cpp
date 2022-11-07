//
// Created by zander on 2022/10/14.
//

#include <cstdlib>
#include <iostream>

using namespace std;

class U{
    int a;
public:
    U(){
        cout<< "U()" <<endl;
    }
    U(U& u){
        cout<< "U(U& u)" <<endl;
    }
    ~U(){
        cout<< "~U" <<endl;
    }
};

int main(){
    int a;//在栈上申请4个字节
//    U u;//在栈上申请4个字节
    char c[10];//在栈上申请10个字节
    char* c1[10];//在栈上申请80个字节

//    U u2 = *new U();//u2在栈上，占4个字节，new U()在堆上，占4个字节，把堆中的U对象直接复制一份给栈
    U* u3 = new U();//栈上是一个指针，占8个字节，把堆中U对象的地址给栈
    int* c2 = (int*)malloc(10);//c2在栈上，占8个字节，指向申请地址的首地址
    cout<< &c2 <<endl;

    //c2，u3都需要手动释放
    delete c2;
    delete u3;
    //u2申请的那块堆区由于没有引用，无法释放，无法管理，尽量不要这样用

    return 0;
}