package scanner;

//Token
public class Token {
    char code;
    int value;

    public String toString(){
        return "(" + code + "," + value + ")  ";
    }
}