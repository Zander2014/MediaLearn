//
// Created by zander on 2022/10/17.
//
#include "iostream"
using namespace std;
int main(){
    std::unique_ptr<int> up3 = std::make_unique<int>(123);
    cout<< (up3.get() == nullptr) <<endl;
    std::unique_ptr<int> up4 = std::move(up3);
    cout<< (up3.get() == nullptr) <<endl;
    cout<< (up4.get() == nullptr) <<endl;
    std::unique_ptr<int> up5;
    up5 = std::move(up4);
    cout<< (up4.get() == nullptr) <<endl;
    cout<< (up5.get() == nullptr) <<endl;
}
