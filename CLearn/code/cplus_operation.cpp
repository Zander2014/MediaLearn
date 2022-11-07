//
// Created by zander on 2022/10/14.
//
#include <iostream>

using namespace std;
class P{
private:
    int x;
public:
    P(int x){
        this->x = x;
    }
    int getX(){
        return this->x;
    }
    P operator+(P &p){//重载运算符+
        P temp(this->x + p.x);
        return temp;
    }

    int operator+(int x){
        this->x += x;
    }

    friend int operator+(int x, P& p);
};
//重载全局+，使得5 + p3;可以正常运行
int operator+(int x, P& p){
    return p.x+x;
}
//int operator+(int x, P& p){
//    return p+x;
//}

int main(){
    P p1(10);
    P p2(10);
    P p3 = p1 + p2;
    cout << p3.getX() <<endl;

    int t1 = p3 + 5;
    int t2 = 5 + p3;
    return 0;
}
