#include "iostream"
int main(){
    int a[3][4] = {0,1,2,3,4,5,6,7,8,9,10,11};
    int(*p1)[3][4] = &a;
    int(*p)[4];//定义指针步长为int类型 * 4
    p = a;
    for(int i = 0; i < 3; i++){
        for(int j = 0; j < 4; j++){
            printf("%2d ", *(*(p+i)+j));//p[i][j]
        }
        printf("\n");
    }
};