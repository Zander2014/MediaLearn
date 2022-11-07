//
// Created by zander on 2022/10/12.
//
#include <iostream>
using namespace std;
class B1{
public:
    virtual void b1(){
        cout<<"B1 b3"<<endl;
    }
    int b11;
};
class B2{
public:
    virtual void b2(){
        cout<<"B2 b3"<<endl;
    }
    int b22;
    int b222;
    int b2222;
};
class B3{
public:
    virtual void b3(){
        cout<<"B3 b3"<<endl;
    }
    int b33;
};

class C: public B1, B2, B3{
public:
    virtual void b1(){
        cout<<"C b1"<<endl;
    }
    virtual void b2(){
        cout<<"C b2"<<endl;
    }
    virtual void b3(){
        cout<<"C b3"<<endl;
    }
    int c;
};
int main(){
    cout<<"C size = "<<sizeof(C)<<endl;
    C c;
    c.b1();
    B1 b1 = c;//如果要把子类赋值给基类，需要在继承处添加public，否则会报错
    b1.b1();
    return 0;
}
