import java.util.*;


public class Parser {

    private boolean errorFlag = false;
    private static boolean errorFlag2 = false;
    private final boolean optionFlag;
    private boolean assignFlag = false;
    private String charClass;
    private String lexeme;
    private String LHS;
    private static final TreeSet<String> IdentList = new TreeSet<>();
    private final List<String> lexemes = new ArrayList<>();
    private final List<Pair> errors = new ArrayList<>();
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

    public void addErrors(Pair pair) {
        errors.add(pair);
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
                } else {
                    errors.add(new Pair("(Warning)", "Change : to := "));
                    lexeme += "=";
                }
                nextToken = String.valueOf(Token.ASSIGN_OP);
                addLexeme(lexeme);
                result[2]--;
            }
            case ';' -> {
                addchar();
                nextToken = String.valueOf(Token.SEMICOLON);
                addLexeme(lexeme);
                result[2]--;
            }
            case '=' -> {
                errors.add(new Pair("(Warning)", "Change = to := "));
                lexeme += ":";
                addchar();
                nextToken = String.valueOf(Token.ASSIGN_OP);
                addLexeme(lexeme);
                result[2]--;
            }
            case (char)-1 -> {
                addchar();
                result[2]--;
                nextToken = String.valueOf(Token.EOF);
            }
            default -> {
                errorFlag = true;
                errors.add(new Pair("(Error)", "Unexpected operator"));
                addchar();
                addLexeme(lexeme);
                nextToken = String.valueOf(Token.ERROR);
                result[2]--;
            }
        }
    }

    void addchar() {
        if (lexLen <= 98) {
            lexeme += nextChar;
            lexLen++;
        } else {
            errorFlag = true;
            errors.add(new Pair("(Error)", "Identifier too long"));
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
                assignFlag = true;
                LHS = lexemes.get(lexemes.size() - 2);
                lex();
                while (nextToken.equals(String.valueOf(Token.ASSIGN_OP))) {
                    errors.add(new Pair("(Warning)", "Consecutive Assignment Operator - Remove Duplicate Assignment Operator"));
                    lexemes.remove(lexemes.size() - 1);
                    lex();
                }
                return expression();
            } else {
                errorFlag = true;
                errors.add(new Pair("(Error)", "No assignment operator"));
            }
        } else {
            if (nextToken.equals(String.valueOf(Token.ASSIGN_OP))) {
                errorFlag = true;
                errors.add(new Pair("(Error)", "Assignment operator cannot be used without identifier"));
            } else {
                errorFlag = true;
                errors.add(new Pair("(Error)", "Unexpected identifier"));
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
            while (!nextToken.equals(String.valueOf(Token.INT_LIT)) && !nextToken.equals(String.valueOf(Token.IDENT))
                    && !nextToken.equals(String.valueOf(Token.LEFT_PAREN))) {
                //ADD_OP일때
                if (nextToken.equals(String.valueOf(Token.ADD_OP))) {
                    if (lexemes.get(lexemes.size() - 1).equals(op)) {
                        errors.add(new Pair("(Warning)", "Duplicate operators were found - Remove duplicate operator (" + op + ")"));
                        lexemes.remove(lexemes.size() - 1);

                    } else {
                        lexemes.remove(lexemes.size() - 1);
                        errors.add(new Pair("(Warning)", "Consecutive operators were found - Remove Backward operator " + op));
                    }
                }
                //MULT_OP일때
                else if (nextToken.equals(String.valueOf(Token.MULT_OP))) {
                    errors.add(new Pair("(Warning)", "Consecutive operators were found - Remove Backward Operator " + lexemes.get(lexemes.size() - 1)));
                    lexemes.remove(lexemes.size() - 1);
                }
                else {
                    errorFlag = true;
                    errors.add(new Pair("(Error)", "Unexpected token"));
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
        else if(!(nextToken.equals(String.valueOf(Token.SEMICOLON)) || nextToken.equals(String.valueOf(Token.EOF))
                || nextToken.equals(String.valueOf(Token.RIGHT_PAREN)) || nextToken.equals(String.valueOf(Token.ERROR)))) {
            errorFlag = true;
            errors.add(new Pair("(Error)", "Unexpected token"));
        }
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
            while (!nextToken.equals(String.valueOf(Token.INT_LIT)) && !nextToken.equals(String.valueOf(Token.IDENT))
                    && !nextToken.equals(String.valueOf(Token.LEFT_PAREN))) {
                //MULT_OP일때
                if (nextToken.equals(String.valueOf(Token.MULT_OP))) {
                    if (lexemes.get(lexemes.size() - 1).equals(op)) {
                        errors.add(new Pair("(Warning)", "Duplicate operators were found - Remove duplicate operator (" + op + ")"));
                        lexemes.remove(lexemes.size() - 1);
                    } else {
                        lexemes.remove(lexemes.size() - 1);
                        errors.add(new Pair("(Warning)", "Consecutive operators were found - Remove Backward operator " + op));
                    }
                } else if (nextToken.equals(String.valueOf(Token.ADD_OP))) {
                    errors.add(new Pair("(Warning)", "Consecutive operators were found - Remove Backward operator " + lexemes.get(lexemes.size() - 1)));
                    lexemes.remove(lexemes.size() - 1);
                }
                else {
                    errorFlag = true;
                    errors.add(new Pair("(Error)", "Unexpected token"));
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
        else if(!(nextToken.equals(String.valueOf(Token.SEMICOLON)) || nextToken.equals(String.valueOf(Token.EOF))
                || nextToken.equals(String.valueOf(Token.ADD_OP)) || nextToken.equals(String.valueOf(Token.RIGHT_PAREN)) || nextToken.equals(String.valueOf(Token.ERROR)))) {
            errorFlag = true;
            errors.add(new Pair("(Error)", "Unexpected token"));
        }
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
                errors.add(new Pair("(Warning)", "not found right parenthesis - add right parenthesis in the end"));
                lexemes.add(")");
                if(optionFlag) System.out.println(")");
                return tmp;
            }
        } else if (nextToken.equals(String.valueOf(Token.IDENT))) {
            if (!IdentValue.containsKey(lexemes.get(lexemes.size() - 1))) {
                if (!(IdentList.contains(lexemes.get(lexemes.size() - 1)) && !errorFlag2)) {
                    errorFlag = true;
                    errors.add(new Pair("(Error)", "Undefined identifier (" + lexemes.get(lexemes.size() - 1) + ")"));
                    IdentValue.put(lexemes.get(lexemes.size() - 1), null);
                }
                else if(IdentList.contains(lexemes.get(lexemes.size() - 1))) {
                    errorFlag = true;
                    errors.add(new Pair("(Error)", "Undefined identifier (" + lexemes.get(lexemes.size() - 1) + ")"));
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
            else if(nextToken.equals(String.valueOf(Token.ERROR))) {
                return 0;
            }
            return Integer.parseInt(lexemes.get(lexemes.size() - 2));
        } else {
            errorFlag = true;
            errors.add(new Pair("(Error)", "Unexpected token"));
            return 0;
        }
    }

    void run() {
        lex();
        int num = statement();
        remainLexer();
        if (!optionFlag) {
            System.out.print("Statement : ");
            StringBuilder stringBuilder = new StringBuilder();
            for (String str : lexemes) {
                if(str.equals("(")) stringBuilder.append(str);
                else if(str.equals(")") || str.equals(";")) {
                    if(stringBuilder.charAt(stringBuilder.length() - 1) == ' ') {
                        stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), str);
                        stringBuilder.append(" ");
                    }
                    else stringBuilder.append(str).append(" ");
                }
                else stringBuilder.append(str).append(" ");
            }
            System.out.println(stringBuilder);
            System.out.println("ID : " + result[0] + " CONST : " + result[1] + " OP : " + result[2]);
            if(!errors.isEmpty()) {
                for(Pair pair : errors) {
                    System.out.println(pair.getFirst() + " - " + pair.getSecond());
                }
            } else {
                System.out.println("(OK)");
            }
            if (!errorFlag) {
                if (IdentValue.containsKey(LHS)) {
                    updateIdentValue(LHS, num);
                } else {
                    setIdentValue(LHS, num);
                }
                //System.out.println("Result => " + LHS + " = " + IdentValue.get(LHS));
            } else {
                updateIdentValue(LHS, null);
                //System.out.println("Result => " + LHS + " = Unknown ");
            }
            errorFlag2 = errorFlag || errorFlag2;
        }
        else {
            for(String str : lexemes) {
                System.out.println(str);
            }
        }
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

    public void remainLexer() {
        while (!nextToken.equals(String.valueOf(Token.EOF))) {
            lex();
            if(assignFlag && nextToken.equals(String.valueOf(Token.ASSIGN_OP))) {
                errors.add(new Pair("(Error)", "Assignment Operator must be one"));
            }
            else if(nextToken.equals(String.valueOf(Token.IDENT))) {
                if(!IdentValue.containsKey(lexemes.get(lexemes.size() - 1))) {
                    errors.add(new Pair("(Error)", "Undefined identifier (" + lexemes.get(lexemes.size() - 1) + ")"));
                }
            }
        }
    }
}
