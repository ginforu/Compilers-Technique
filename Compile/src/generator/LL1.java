package generator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class LL1 {
	static String str;
	public static void main(String[] args) throws FileNotFoundException {
		readString("src/generator/era.txt");
	}

	static String[] VN = {"E", "E1", "T", "T1", "F"};  //非终结符
    static String[] VT = {"i", "w1", "w2", "(", ")", "#"};  //终结符
    static int[][] table = {  //LL(1)分析表，0填充空
            {1, 0, 0, 1, 0, 0},  //E
            {0, 2, 0, 0, 3, 3},  //E'
            {4, 0, 0, 4, 0, 0},  //T
            {0, 6, 5, 0, 6, 6},  //T'
            {7, 0, 0, 8, 0, 0}   //F
    };
    static String[][] production = {//属性翻译文法，逆序压栈,"&"表示ε
            {"T", "E1"}, {"w1", "T", "GEQ", "E1"}, {"&"},
            {"F", "T1"}, {"w2", "F", "GEQ", "T1"}, {"&"},
            {"i"}, {"(", "E", ")"}
    };
    
    static Stack<String> symbols = new Stack<String>();//分析栈
    static Stack<String> words = new Stack<String>();//字符串转单词后，存放在这里
    static ArrayList<String> qtrs = new ArrayList<String>();//四元式
    static Stack<String> sem = new Stack<String>();//语义栈
    static Stack<String> syn = new Stack<String>();//语法栈
    static String word = "";
    static String symbol = "";
    static int cnt=0;
    public static void readString(String fileName) throws FileNotFoundException {
    	Scanner sc=new Scanner(new FileReader(fileName));
    	 while (sc.hasNext()) {
             str = sc.nextLine();
             System.out.println(str);
             analysis(str);
         }
    	 
    	
    }

	public static void analysis(String str) {
		// TODO Auto-generated method stub
		proc(str);//将字符串转为单词序列
		symbols.push("#");
        symbols.push("E");
        word = words.pop();
        while(!words.isEmpty()||!symbols.isEmpty()) {//从语法栈中弹出单词
        	 if (symbols.isEmpty()) {
                 err();
                 System.out.println(":丢失'('符号");
                 System.out.println();
                 clear();
                 return;
             }
             symbol = symbols.pop();
             int flag;
             switch (symbol) {
                 case "GEQ":
                     GEQ();
                     break;
                 case "i":
                 case "w1":
                 case "w2":
                 case "(":
                 case ")":
                     flag = doVT();//处理终结符
                     if (flag == 1) {
                         System.out.println();
                         return;
                     }
                     break;
                 case "E":
                 case "E1":
                 case "T":
                 case "T1":
                 case "F":
                     flag = doVN();//处理非终结符
                     if (flag == 1) {
                         System.out.println();
                         return;
                     }
                     break;
             }
         }
         if (word.equals("#")) {
        	 System.out.println("得到的四元式为：");
             for (String string : qtrs) {
                 System.out.println(string);
             }
         } else {
             err();
             if (word.equals(")")) {
                 System.out.println(":丢失'('符号");
             }
             return;
         }
         System.out.println();
         clear();
        }
	
	 static int doVT() {
	        switch (symbol) {//判断终结符类型
	            case "i"://标识符或常数
	                for (int i = 0; i < word.length(); i++) {
	                    if (!((word.charAt(i) >= '0' && word.charAt(i) <= '9')
	                            || (word.charAt(i) >= 'a' && word.charAt(i) <= 'z'))) {
	                        err();
	                        return 1;
	                    }
	                }
	                sem.push(word);//压入语义栈
	                break;
	            case "w1"://加减
	                if (!(word.equals("+") || word.equals("-"))) {
	                    err();
	                    return 1;
	                }
	                syn.push(word);
	                break;
	            case "w2"://乘除
	                if (!(word.equals("*") || word.equals("/"))) {
	                    err();
	                    return 1;
	                }
	                syn.push(word);
	                break;
	            case "(":
	                if (!(word.equals("("))) {
	                    err();
	                    return 1;
	                }
	                break;
	            case ")":
	                if (!(word.equals(")"))) {
	                    err();
	                    System.out.println(":丢失')'符号");
	                    return 1;
	                }
	                break;
	            default:
	                err();
	                System.out.println(":找不到终止符'#'");
	                return 1;
	        }
	        if (words.isEmpty()) {
	            err();
	            System.out.println(":找不到终止符'#'");
	            return 1;
	        }
	        word = words.pop();
	        return 0;
	    }

	private static int doVN() {
		// TODO Auto-generated method stub
		int row=getRow();
		int col=getCol();
		int index=table[row][col]-1;//查LL（1）表
		if(index==-1) {//ERROR
			 err();
		        if (word.equals("(")) {
		            System.out.println(":丢失操作符");
		        } else {
		            System.out.println("两个运算符不能直接相连");
		        }
		        return 1;   
		}
		//FOUND,逆序压栈
	    for (int i = production[index].length - 1; i >= 0; i--) {
	        if (production[index][i].equals("&")) {
	            continue;
	        }
	        symbols.push(production[index][i]);
	    }
	    return 0;
	}
	
    
	private static int getCol() {
		// TODO Auto-generated method stub
		String w = word;
        if ((word.charAt(0) >= '0' && word.charAt(0) <= '9') || (word.charAt(0) >= 'a' && word.charAt(0) <= 'z')) {
            w = "i";
        } else if (word.equals("+") || word.equals("-")) {
            w = "w1";
        } else if (word.equals("*") || word.equals("/")) {
            w = "w2";
        }
        for (int i = 0; i < VT.length; i++) {
            if (w.equals(VT[i])) {
                return i;
            }
        }
        return 0;
	}

	private static int getRow() {
		// TODO Auto-generated method stub
		 for (int i = 0; i < VN.length; i++) {
	            if (symbol.equals(VN[i])) {
	                return i;
	            }
	        }
	     return 0;
	}

	static void proc (String string) {//字符串拆成单词并压栈
		int i;
		for (i=str.length()-1;i>=0;i--) {
			 String s = "";
	            char ch = string.charAt(i);
	            boolean flag = false;
	            while (ch >= '0' && ch <= '9') {
	                flag = true;
	                s = ch + s;
	                i--;
	                if (i < 0) {
	                    break;
	                }
	                ch = string.charAt(i);
	            }
	            if (flag) {
	                i++;
	                words.push(s);
	            } else {
	                words.push(ch + s);
	            }
		}
	}
	
	static void GEQ() {//四元式子生成
		 cnt++;
	        String word1 = sem.pop();
	        String word0 = sem.pop();
	        String op = syn.pop();
	        String qtr = "(" + op + "," + word0 + "," + word1 + ",t" + cnt + ")";
	        sem.push("t" + cnt);
	        qtrs.add(qtr);
	}
	
	static void err() {
	        System.out.print("错误");
	        clear();
	}
	
	 static void clear() {
	        symbols.clear();
	        syn.clear();
	        sem.clear();
	        words.clear();
	        qtrs.clear();
	        cnt = 0;
	    }
	
}
