//
// Created by zander on 2022/10/12.
//

class book {
public:
    int count;
};

int main() {
    book b1 = *new book();
    book *b2 = new book();

    b1.count;
    b2->count;
    return 0;
}
