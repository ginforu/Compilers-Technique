package scanner;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static String[] kt = {"program", "begin", "end", "var", "while", "do", "repeat", "until", "for", "to", "if", "then", "else"};//关键字表
    static String[] dt = {";", ":", "(", ")", ",", ":=", "+", "-", "*", "/", ">", ">=", "==", "<", "<="};//界符表
    static ArrayList<String> st = new ArrayList<String>();//标识符表
    static ArrayList<Double> ct = new ArrayList<Double>();//常数表
    static int [][] sct = {{2, 0, 0, 0, 8, 9, 15},{2, 3, 5, 11, 0, 0, 11},{4, 0, 0, 0, 0, 0, 0},{4, 0, 5, 11, 0, 0, 11},{7, 0, 0, 6, 0, 0, 0},{7, 0, 0, 0, 0, 0, 0},{7, 0, 0, 11, 0, 0, 11},{8, 0, 0, 0, 8, 0, 12},{0, 0, 0, 0, 0, 10, 14},{0, 0, 0, 0, 0, 0, 13}};//状态转换表
    static int n, m, p, t, e;//尾数值，指数值，小数位数，指数符号，类型
    static double num;//常数值
    static String str;//当前单词
    static String string;//缓冲区
    static int i;//当前字符位置
    static int flag;//初步标记当前字符的类型
    static Token token;
    static ArrayList<Token> tokens = new ArrayList<Token>();

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(new FileReader("src/scanner/exa.txt"));
        while (sc.hasNextLine()) {
            string = sc.nextLine();
            i = 0;
            do {//处理一行，每次处理一个单词
                while (string.charAt(i) == ' ') {//滤空格
                    i++;
                }
                flag = recognize(string, i);  //识别单词
                if (flag == 4) {  //过滤非法字符
                    System.out.println("Illegal character:" + string.charAt(i));
                    i++;
//                    tokens.remove(tokens.size() - 1);
                    continue;
                }
                int state = 1;
                if (flag == 5) i++;
                while (state != 0 && i < string.length()) {
                    char c = string.charAt(i);
                    act(state, c);
                    if (state >= 11 && state <= 14) break;
                    i++;
                    state = nextState(state, c);
                    if (i == string.length() - 1) {
                        c = string.charAt(i);
                        act(state, c);
                        if (flag == 2) state = 11;
                        else if (flag == 3) state = 13;
                        else state = 12;
                    }
                }
                if (state == 0) {
                    System.out.println("Lexical error：" + str);
                }
            } while (i < string.length());
        }
        print();  //输出结果
        sc.close();
    }

    private static void print() {
    	System.out.println("***************************");
    	System.out.println("Token序列：");
        for (int i = 0; i < tokens.size(); i++) {
            System.out.print(tokens.get(i));
        }
        System.out.println();
        System.out.println("关键字表：");
        for (String s : kt) {
            System.out.print(s + " ");
        }
        System.out.println();
        System.out.println("界符表：");
        for (String s : dt) {
            System.out.print(s + " ");
        }
        System.out.println();
        System.out.println("标识符表：");
        for (String s : st) {
            System.out.print(s + " ");
        }
        System.out.println();
        System.out.println("常数表：");
        for (Double s : ct) {
            System.out.print(s + " ");
        }
        System.out.println();
        System.out.println("***************************");
    }


    private static void act(int state, char c) {
        int code;
        switch (state) {
            case 1://初始化
                n = c - '0';
                m = 0;
                p = 0;
                t = 0;
                e = 1;
                num = 0;
                str = "";
                str += c;
                token = new Token();
                break;
            case 2://常数拼接
                if (c == ' ' || c == 'e' || c == 'E' || c == '.') break;
                n = 10 * n + c - '0';
                break;
            case 3://确定常数类型
                t = 1;
                i--;
                break;
            case 4://常数拼接，记录小数点位数
                if (c == ' ') break;
                n = 10 * n + c - '0';
                m++;
                break;
            case 5://确定常数类型
                t = 1;
                break;
            case 6://判断e的正负
                c = string.charAt(--i);
                if (c == '-') e = -1;
                break;
            case 7://指数拼接
                if (c == ' ') break;
                p = 10 * p + c - '0';
                break;
            case 8://字符串拼接
                str += c;
                break;
            case 9://字符串拼接
                str += c;
                break;
            case 10://字符串拼接
                str += c;
                break;
            case 11://常数计算、查添常数表、生成token序列
                num = n * Math.pow(10, e * p - m);
                if (flag == 5) {
                    num = 0 - num;
                    flag = 2;
                }
                token.code = 'c';
                token.value = InsertCt(num);
                tokens.add(token);
                break;
            case 12://查关键字表、变量表，生成token序列
                code = reserveKt(str.trim());//查关键字表
                if (code != -1) {
                    token.code = 'k';
                    token.value = code;
                } else {
                    token.code = 'i';
                    token.value = insertST(str.trim());
                }
                tokens.add(token);
                break;
            case 13://查界符表、生成token序列
                code = reserve(str.trim());//查界符表
                if (code != -1) {
                    token.code = 'p';
                    token.value = code;
                } else {
                    str = str.substring(0, str.length() - 1);
                    i--;
                    code = reserve(str.trim());
                    token.code = 'p';
                    token.value = code;
                }
                tokens.add(token);
                break;
            case 14://查界符表、生成token序列
                code = reserve(str.trim());
                if (code == -1) break;
                token.code = 'p';
                token.value = code;
                tokens.add(token);
                break;
        }
    }

    static int insertST(String str) {
        for (int i = 0; i < st.size(); i++) {  //查符号表
            if (str.equals(st.get(i))) {
                return i;
            }
        }
        st.add(str);  //填符号表
        return st.size() - 1;
    }

    static int reserveKt(String str) {
        for (int i = 0; i < kt.length; i++) {
            if (str.equals(kt[i])) return i;
        }
        return -1;
    }

    static int reserve(String str) {
        for (int i = 0; i < dt.length; i++) {
            if (str.equals(dt[i])) return i;
        }
        return -1;
    }

    static int InsertCt(double num) {
        for (int i = 0; i < ct.size(); i++) {
            if (num == ct.get(i)) return i;
        }
        ct.add(num);
        return ct.size() - 1;
    }
    //进行状态转换
    static int nextState(int state, char c) {
        if (c >= '0' && c <= '9') return sct[state - 1][0];
        if (c == '.') return sct[state - 1][1];
        if (c == 'E' || c == 'e') {
            if (flag == 2) return sct[state - 1][2];
            else return sct[state - 1][4];
        } else if (c >= 'a' && c <= 'z') return sct[state - 1][4];
        if (c == '+' || c == '-') return sct[state - 1][3];
        for (String s : dt) {
            if (c == s.charAt(0)) return sct[state - 1][5];
        }
        return sct[state - 1][6];
    }
//判定单词类别
    @SuppressWarnings("unlikely-arg-type")
    static int recognize(String str, int i) {
        if (str.charAt(i) >= 'a' && str.charAt(i) <= 'z') {
            return 1;
        } else if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
            return 2;
        } else {
            if (str.charAt(i) == '-' && (str.charAt(i + 1) >= '0' && str.charAt(i + 1) <= '9')) return 5;
            for (String s : dt) {
                if (s.charAt(0) == str.charAt(i))
                    return 3;
            }
            return 4;
        }
    }
}
