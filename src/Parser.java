import java.util.*;


public class Parser {

    private boolean errorFlag = false;
    private static boolean errorFlag2 = false;
    private boolean warningFlag = false;
    private String warningCode;
    private final boolean optionFlag;
    private String errorCode;
    private String charClass;
    private String lexeme;
    private String LHS;
    private static final TreeSet<String> IdentList = new TreeSet<>();
    List<String> lexemes = new ArrayList<>();
    private static final HashMap<String, Integer> IdentValue = new HashMap<>();
    private char nextChar;
    private int lexLen;
    private String nextToken;
    private String expr;
    private final int[] result = new int[3]; //0 ID, 1 CONST, 2 OP

    Parser(String expr, Boolean optionFlag) {
        this.expr = expr;
        this.optionFlag = optionFlag;
        lexLen = 0;
        nextToken = "";
        lexeme = "";
        getchar();
    }

    void setIdentValue(String ident, int value) {
        IdentValue.put(ident, value);
    }

    void updateIdentValue(String ident, Integer value) {
        IdentValue.replace(ident, value);
    }

    void addLexeme(String str) {
        lexemes.add(str);
    }


    void resetLexeme() {
        lexeme = "";
        lexLen = 0;
    }

    void lookup(char ch) {
        result[2]++;
        switch (ch) {
            case '(' -> {
                addchar();
                nextToken = String.valueOf(Token.LEFT_PAREN);
                addLexeme(lexeme);
                result[2]--;
            }
            case ')' -> {
                addchar();
                nextToken = String.valueOf(Token.RIGHT_PAREN);
                addLexeme(lexeme);
                result[2]--;
            }
            case '+', '-' -> {
                addchar();
                nextToken = String.valueOf(Token.ADD_OP);
                addLexeme(lexeme);
            }
            case '*', '/' -> {
                addchar();
                nextToken = String.valueOf(Token.MULT_OP);
                addLexeme(lexeme);
            }
            case ':' -> {
                addchar();
                getchar();
                if (nextChar == '=') {
                    addchar();
                    nextToken = String.valueOf(Token.ASSIGN_OP);
                    addLexeme(lexeme);
                    result[2]--;
                    break;
                } else {
                    errorCode = "Unexpected operator";
                }
                result[2]--;
            }
            default -> {
                addchar();
                result[2]--;
                nextToken = String.valueOf(Token.EOF);
            }
        }
    }

    void addchar() {
        if (lexLen <= 98) {
            lexeme += nextChar;
            lexLen++;
        } else {
            errorFlag = true;
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
            case "LETTER" -> {
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
            }
            case "DIGIT" -> {
                addchar();
                getchar();
                while (charClass.equals(String.valueOf(CharClass.DIGIT))) {
                    addchar();
                    getchar();
                }
                result[1]++;
                addLexeme(lexeme);
                nextToken = String.valueOf(Token.INT_LIT);
            }
            case "EOF" -> nextToken = String.valueOf(Token.EOF);
            case "UNKNOWN" -> {
                lookup(nextChar);
                getchar();
            }
        }
        if (optionFlag) {
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
            if (nextToken.equals(String.valueOf(Token.ASSIGN_OP))) {
                errorCode = "Unexpected assignment operator";
                errorFlag = true;
            } else {
                errorCode = "Unexpected identifier";
                errorFlag = true;
            }
        }
        return 0;
    }

    int expression() {
        int num1 = term();
        ArrayList<Map<String, Integer>> tmp = termTail();
        if (!tmp.isEmpty()) {
            for (Map<String, Integer> map : tmp) {
                for (String str : map.keySet()) {
                    if (str.equals("+")) {
                        num1 += map.get(str);
                    } else {
                        num1 -= map.get(str);
                    }
                }
            }
        }
        return num1;
    }

    ArrayList<Map<String, Integer>> termTail() {
        if (nextToken.equals(String.valueOf(Token.ADD_OP))) {
            String op = lexemes.get(lexemes.size() - 1);
            lex();
            while (!nextToken.equals(String.valueOf(Token.INT_LIT)) && !nextToken.equals(String.valueOf(Token.IDENT))) {
                //ADD_OP일때
                if (nextToken.equals(String.valueOf(Token.ADD_OP))) {
                    if (lexemes.get(lexemes.size() - 1).equals(op)) {
                        warningFlag = true;
                        warningCode = "중복 연산자 (" + op + ") 제거";
                        lexemes.remove(lexemes.size() - 1);

                    } else {
                        Scanner scanner = new Scanner(System.in);
                        System.out.println("연산자가 연속해서 나왔습니다.");
                        System.out.print("올바른 판단을 위해 “-“와 “+“ 중 하나를 입력하세요 : ");
                        String tmp = scanner.nextLine();
                        while (!tmp.equals("+") && !tmp.equals("-")) {
                            System.out.print("“-“와 “+“ 중 하나를 입력하세요 : ");
                            tmp = scanner.nextLine();
                        }
                        warningFlag = true;
                        warningCode = "연속되서 나온 연산자";
                        op = tmp;
                        if (lexemes.get(lexemes.size() - 1).equals(op)) {
                            lexemes.remove(lexemes.size() - 2);
                        } else {
                            lexemes.remove(lexemes.size() - 1);
                        }
                    }
                }
                //MULT_OP일때
                else if (nextToken.equals(String.valueOf(Token.MULT_OP))) {
                    warningFlag = true;
                    warningCode = "연속되서 나온 연산자";
                    System.out.println("Term에서는 곱셈과 나눗셈 계산이 불가능하므로 무시하고 넘어갑니다.");
                    lexemes.remove(lexemes.size() - 1);
                } else {
                    errorCode = "Unexpected token";
                    errorFlag = true;
                    return new ArrayList<>();
                }
                result[2]--;
                lex();
            }
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
        if (!tmp.isEmpty()) {
            for (Map<String, Integer> map : tmp) {
                for (String str : map.keySet()) {
                    if (str.equals("*")) {
                        num1 *= map.get(str);
                    } else {
                        num1 /= map.get(str);
                    }
                }
            }
        }
        return num1;
    }

    ArrayList<Map<String, Integer>> factorTail() {
        if (nextToken.equals(String.valueOf(Token.MULT_OP))) {
            String op = lexemes.get(lexemes.size() - 1);
            lex();
            while (!nextToken.equals(String.valueOf(Token.INT_LIT)) && !nextToken.equals(String.valueOf(Token.IDENT))) {
                //MULT_OP일때
                if (nextToken.equals(String.valueOf(Token.MULT_OP))) {
                    if (lexemes.get(lexemes.size() - 1).equals(op)) {
                        warningFlag = true;
                        warningCode = "중복 연산자 (" + op + ") 제거";
                        lexemes.remove(lexemes.size() - 1);
                    } else {
                        Scanner scanner = new Scanner(System.in);
                        System.out.println("연산자가 연속해서 나왔습니다.");
                        System.out.print("올바른 판단을 위해 “*“와 “/“ 중 하나를 입력하세요 : ");
                        String tmp = scanner.nextLine();
                        while (!tmp.equals("*") && !tmp.equals("/")) {
                            System.out.print("“*“와 “/“ 중 하나를 입력하세요 : ");
                            tmp = scanner.nextLine();
                        }
                        warningFlag = true;
                        warningCode = "연속되서 나온 연산자";
                        op = tmp;
                        if (lexemes.get(lexemes.size() - 1).equals(op)) {
                            lexemes.remove(lexemes.size() - 2);
                        } else {
                            lexemes.remove(lexemes.size() - 1);
                        }
                    }
                } else if (nextToken.equals(String.valueOf(Token.ADD_OP))) {
                    warningFlag = true;
                    warningCode = "연속되서 나온 연산자";
                    System.out.println("“Factor에서는 덧셈과 뺄셈 계산이 불가능하므로 무시하고 넘어갑니다.”");
                    lexemes.remove(lexemes.size() - 1);
                } else {
                    errorCode = "Unexpected token";
                    errorFlag = true;
                    return new ArrayList<>();
                }
                result[2]--;
                lex();
            }
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
        if (nextToken.equals(String.valueOf(Token.LEFT_PAREN))) {
            lex();
            int tmp = expression();
            if (nextToken.equals(String.valueOf(Token.RIGHT_PAREN))) {
                lex();
                return tmp;
            } else {
                errorCode = "괄호가 닫히지 않았습니다";
                errorFlag = true;
                return 0;
            }
        } else if (nextToken.equals(String.valueOf(Token.IDENT))) {
            if (!IdentValue.containsKey(lexemes.get(lexemes.size() - 1))) {
                if (IdentList.contains(lexemes.get(lexemes.size() - 1)) && errorFlag2);
                else {
                    errorCode = "Undefined identifier";
                    errorFlag = true;
                    IdentValue.put(lexemes.get(lexemes.size() - 1), null);
                }
            }
            lex();
            if (!errorFlag && !errorFlag2) {
                if (nextToken.equals(String.valueOf(Token.EOF))) {
                    return IdentValue.get(lexemes.get(lexemes.size() - 1));
                }
                return IdentValue.get(lexemes.get(lexemes.size() - 2));
            }
            return 0;
        } else if (nextToken.equals(String.valueOf(Token.INT_LIT))) {
            lex();
            if (nextToken.equals(String.valueOf(Token.EOF))) {
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
        System.out.print("Statement : ");
        for (String str : lexemes) {
            if (str.equals("(") || str.equals(")")) {
                System.out.print(str);
            } else System.out.print(str + " ");
        }
        System.out.println();
        System.out.println("ID : " + result[0] + " CONST : " + result[1] + " OP : " + result[2]);
        if (warningFlag) {
            System.out.println("(Warning) - " + warningCode);
        } else if (!errorFlag) {
            System.out.println("(OK)");
        } else {
            System.out.println("(Error) - " + errorCode);
        }
        if (!errorFlag && !errorFlag2) {
            if (IdentValue.containsKey(LHS)) {
                updateIdentValue(LHS, num);
            } else {
                setIdentValue(LHS, num);
            }
            System.out.println("Result => " + LHS + " = " + IdentValue.get(LHS));
        } else {
            updateIdentValue(LHS, null);
            System.out.println("Result => " + LHS + " = Unknown ");
        }
        errorFlag2 = errorFlag || errorFlag2;
    }

    public static void printResult() {
        System.out.print("=> Result : ");
        for (String str : IdentList) {
            if (IdentValue.get(str) == null) {
                System.out.print(str + " = Unknown ");
            } else {
                System.out.print(str + " = " + IdentValue.get(str) + " ");
            }
        }
    }
}
