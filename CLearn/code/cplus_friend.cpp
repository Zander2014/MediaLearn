//
// Created by zander on 2022/10/14.
//
#include <iostream>

using namespace std;
class A;

class B{
private:
    int b;
public:
    void funcB(A* a);//不可访问A的私有变量
    void accessA(A* a);
};

class A{
private:
    int a;
public:
    void funcA(A a);
    friend void B::accessA(A* a);//将B的accessA设为自己的友元函数
    friend void outAccessA(A* a);//提供全局的友元函数
    friend class C;//将C设为自己的友元类
};

class C{
private:
    int c;
public:
    void funC(A* a);//自己是A的友元类，所有函数可以访问A的私有变量
};

//自己调用自己，可以访问自己的成员
void A::funcA(A a){
    a.a;
}
//外包调用，不可用访问A的私有成员
void outAccessA(A a){
    //a.a;//获取不到私有属性
}
//A将B的accessA添加为友元函数，B可以访问A的私有成员
void B::accessA(A* a) {
    a->a;
}
//A提供了全局友元函数，外部可以访问A的私有成员
void outAccessA(A* a){
    a->a;
}

void B::funcB(A* a) {
    //a.a;//
}

void C::funC(A *a) {
    a->a;
}

int main(){
    A a;
    B b;
    b.accessA(&a);
    outAccessA(&a);
    return 0;
}
