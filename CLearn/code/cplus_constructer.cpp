//
// Created by zander on 2022/10/13.
//
#include <iostream>

using namespace std;
class Test{
public:
    Test(){
        cout<<"无参构造"<<endl;
    }

    Test(const Test& t){
        cout<<"拷贝构造"<<endl;
    }

    ~Test(){
        cout<<"析构"<<endl;
    }
    int a;
    int *p;
};

Test retFunc1(){
    Test t;
    return t;
}

Test& retFunc2(){
    Test t;
    return t;
}

Test* retFunc3(){
    Test t;
    return &t;
}

int main(){
//    int b = 10;
//    Test t1;
//    t1.p = &b;
//    Test t2 = t1;
//    t1.a = 1;
//    t2.a = 10;
//    *t2.p = 20;
//
//    cout<< t1.a <<  "|" << t2.a <<endl;
//    cout<< t1.p <<  "|" << t2.p <<endl;
//    cout<< *t1.p <<  "|" << *t2.p <<endl;

//    Test tf1 = retFunc1();
//    tf1.a = 12;
//    cout<< tf1.a << "\n" <<endl;
//
    Test tf2 = retFunc2();
    tf2.a = 11;
    cout<< tf2.a << "\n" <<endl;

//    Test* tf3 = retFunc3();
//    tf3->a = 10;
//    cout<< tf3->a << "\n" <<endl;
    return 0;
}
