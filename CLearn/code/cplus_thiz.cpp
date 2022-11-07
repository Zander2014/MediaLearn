//
// Created by zander on 2022/10/13.
//

class stu{
public:
    void setName(char *name);
private:
    char *name;
};
//这个方法编译后会成为void stu::setName(stu* this, char* name)
void stu::setName(char* name){
    this->name = name;//所以当前this就是传进来的那个stu对象
}
int main(){
    stu s;
    s.setName("zhang");//实际上，s被当做第一个参数this传进去
}