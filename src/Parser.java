import java.util.*;


public class Parser {

    private boolean errorFlag = false;

    private boolean waringFlag = false;
    
    private final boolean optionFlag;

    private String errorCode;

    private static boolean IdentFlag = false;
    private String charClass;
    private String lexeme;

    private String LHS;

    private static TreeSet<String> IdentList = new TreeSet<>();

    List<String> lexemes = new ArrayList<>();

    private static HashMap<String, Integer> IdentValue = new HashMap<>();
    private char nextChar;
    private int lexLen;
    private String nextToken;

    private String expr;

    private String savedExpr;




    private int[] result = new int[3]; //0 ID, 1 CONST, 2 OP

    Parser(String expr, Boolean optionFlag) {
        this.expr = expr;
        this.savedExpr = expr;
        this.optionFlag = optionFlag;
        lexLen = 0;
        nextToken = "";
        lexeme = "";
        getchar();
    }

    void setIdentValue(String ident, int value) {
        IdentValue.put(ident, value);
    }

    void updateIdentValue(String ident, int value) {
        IdentValue.replace(ident, value);
    }

    void addLexeme(String str) {
        lexemes.add(str);
    }

    void resetLexeme() {
        lexeme = "";
        lexLen = 0;
    }

    String lookup(char ch) {
        result[2]++;
        switch (ch) {
            case '(':
                addchar();
                nextToken = String.valueOf(Token.LEFT_PAREN);
                addLexeme(lexeme);
                result[2]--;
                break;
            case ')':
                addchar();
                nextToken = String.valueOf(Token.RIGHT_PAREN);
                addLexeme(lexeme);
                result[2]--;
                break;
            case '+', '-':
                addchar();
                nextToken = String.valueOf(Token.ADD_OP);
                addLexeme(lexeme);
                break;
            case '*', '/':
                addchar();
                nextToken = String.valueOf(Token.MULT_OP);
                addLexeme(lexeme);
                break;
            case ':':
                addchar();
                getchar();
                if(nextChar == '=') {
                    addchar();
                    nextToken = String.valueOf(Token.ASSIGN_OP);
                    addLexeme(lexeme);
                    result[2]--;
                    break;
                }
                else {
                    errorCode = "Unexpected operator";
                }
                result[2]--;
                break;
            /*
            case ';':
                addchar();
                nextToken = String.valueOf(Token.SEMICOLON);
                result[2]--;
                break;
             */
            default:
                addchar();
                result[2]--;
                nextToken = String.valueOf(Token.EOF);
                break;
        }
        return nextToken;
    }

    void addchar() {
        if (lexLen <= 98) {
            lexeme += nextChar;
            lexLen++;
        } else {
            errorCode = "lexeme is too long";
        }
    }

    void getchar() {
        if (!expr.isEmpty()) {
            nextChar = expr.charAt(0);
            expr = expr.substring(1);
            if (Character.isLetter(nextChar)) {
                charClass = String.valueOf(CharClass.LETTER);
            } else if (Character.isDigit(nextChar)) {
                charClass = String.valueOf(CharClass.DIGIT);
            } else {
                charClass = String.valueOf(CharClass.UNKNOWN);
            }
        } else {
            charClass = String.valueOf(CharClass.EOF);
        }
    }

    void lex() {
        lexLen = 0;
        getNonBlank();
        switch (charClass) {
            case "LETTER":
                addchar();
                getchar();
                while (charClass.equals(String.valueOf(CharClass.DIGIT)) || charClass.equals(String.valueOf(CharClass.LETTER))) {
                    addchar();
                    getchar();
                }
                result[0]++;
                addLexeme(lexeme);
                IdentList.add(lexeme);
                nextToken = String.valueOf(Token.IDENT);
                break;
            case "DIGIT":
                addchar();
                getchar();
                while (charClass.equals(String.valueOf(CharClass.DIGIT))) {
                    addchar();
                    getchar();
                }
                result[1]++;
                addLexeme(lexeme);
                nextToken = String.valueOf(Token.INT_LIT);
                break;
            case "EOF":
                nextToken = String.valueOf(Token.EOF);
                break;
            case "UNKNOWN":
                lookup(nextChar);
                getchar();
                break;
        }
        if(optionFlag) {
            System.out.println("Next token is: " + nextToken + ", Next lexeme is " + lexeme);
        }
        resetLexeme();
    }

    void getNonBlank() {
        while (Character.isWhitespace(nextChar)) {
            getchar();
        }
    }

    int statement() {
        if (nextToken.equals(String.valueOf(Token.IDENT))) {
            lex();
            if (nextToken.equals(String.valueOf(Token.ASSIGN_OP))) {
                LHS = lexemes.get(lexemes.size() - 2);
                lex();
                return expression();
            } else {
                errorCode = "Many assignment operator";
                errorFlag = true;
            }
        } else {
            if(nextToken.equals(String.valueOf(Token.ASSIGN_OP))) {
                errorCode = "Unexpected assignment operator";
                errorFlag = true;
            }
            else {
                errorCode = "Unexpected identifier";
                errorFlag = true;
            }
        }
        return 0;
    }

    int expression() {
        int num1= term();
        ArrayList<Map<String, Integer>> tmp = termTail();
        if(!tmp.isEmpty()) {
            for(Map<String, Integer> map : tmp) {
                for(String str : map.keySet()) {
                    if(str.equals("+")) {
                        num1 += map.get(str);
                    }
                    else {
                        num1 -= map.get(str);
                    }
                }
            }
        }
        return num1;
    }

    ArrayList<Map<String, Integer>> termTail() {
        if(nextToken.equals(String.valueOf(Token.ADD_OP))) {
            String op = lexemes.get(lexemes.size() - 1);
            lex();
            int num = term();
            Map<String, Integer> tmp = new HashMap<>();
            tmp.put(op, num);
            ArrayList<Map<String, Integer>> additonal = termTail();
            ArrayList<Map<String, Integer>> tmpMap = new ArrayList<>();
            tmpMap.add(tmp);
            tmpMap.addAll(additonal);
            return tmpMap;
        }
        /*
        else {
            //none
            lex();
            return new ArrayList<>();
        }
         */
        return new ArrayList<>();
    }
    int term() {
        int num1 = factor();
        ArrayList<Map<String, Integer>> tmp = factorTail();
        if(!tmp.isEmpty()) {
            for(Map<String, Integer> map : tmp) {
                for(String str : map.keySet()) {
                    if(str.equals("*")) {
                        num1 *= map.get(str);
                    }
                    else {
                        num1 /= map.get(str);
                    }
                }
            }
        }
        return num1;
    }
    ArrayList<Map<String, Integer>> factorTail() {
        if(nextToken.equals(String.valueOf(Token.MULT_OP))) {
            String op = lexemes.get(lexemes.size() - 1);
            lex();
            int num = factor();
            Map<String, Integer> tmp = new HashMap<>();
            tmp.put(op, num);
            ArrayList<Map<String, Integer>> additonal = factorTail();
            ArrayList<Map<String, Integer>> tmpMap = new ArrayList<>();
            tmpMap.add(tmp);
            tmpMap.addAll(additonal);
            return tmpMap;
        }
        /*else {
            //none
            lex();
            return new ArrayList<>();
        }
         */
        return new ArrayList<>();
    }
    int factor() {
        if(nextToken.equals(String.valueOf(Token.LEFT_PAREN))) {
            lex();
            int tmp = expression();
            if(nextToken.equals(String.valueOf(Token.RIGHT_PAREN))) {
                lex();
                return tmp;
            } else {
                errorCode = "expected right parenthesis";
                errorFlag = true;
                return 0;
            }
        } else if(nextToken.equals(String.valueOf(Token.IDENT))) {
            if(!IdentValue.containsKey(lexemes.get(lexemes.size() - 1))) {
                errorCode = "Undefined identifier";
                errorFlag = true;
                IdentFlag = true;
                if(IdentList.contains(lexemes.get(lexemes.size() - 1))) {
                    IdentValue.put(lexemes.get(lexemes.size() - 1), 0);
                }
            }
            lex();
            if(!errorFlag) {
                if (nextToken.equals(String.valueOf(Token.EOF))) {
                    return IdentValue.get(lexemes.get(lexemes.size() - 1));
                }
                return IdentValue.get(lexemes.get(lexemes.size() - 2));
            }
            return 0;
        } else if(nextToken.equals(String.valueOf(Token.INT_LIT))) {
            lex();
            if(nextToken.equals(String.valueOf(Token.EOF))) {
                return Integer.parseInt(lexemes.get(lexemes.size() - 1));
            }
            return Integer.parseInt(lexemes.get(lexemes.size() - 2));
        } else {
            errorCode = "Unexpected token";
            errorFlag = true;
            return 0;
        }
    }
    void run() {
        lex();
        int num = statement();
        System.out.println("Statement : " + savedExpr);
        System.out.println("ID : " + result[0] + " CONST : " + result[1] + " OP : " + result[2]);
        if(!errorFlag) {
            System.out.println("(OK)");
        }
        else {
            System.out.println("(Error) - " + errorCode);
        }
        if(IdentValue.containsKey(LHS)) {
            updateIdentValue(LHS, num);
        }
        else {
            setIdentValue(LHS, num);
        }
        System.out.println("Result => " + LHS + " = " + IdentValue.get(LHS));
    }

    public static void printResult() {
        System.out.print("=> Result : ");
        if(IdentFlag) {
            for(String str : IdentList) {
                System.out.print( str + " = Unknown ");
            }
        }
        else {
            for (String str : IdentValue.keySet()) {
                System.out.print(str + " = " + IdentValue.get(str) + " ");
            }
        }
    }
}
