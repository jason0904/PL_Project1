import java.util.ArrayList;
import java.util.List;

public class Program {

    private String input;
    private List<String> Statements = new ArrayList<>();

    private boolean semicolonFlag = false;


    Program(String input) {
        this.input = input;
        this.Statements = splitStatements();
    }

    //문장을 ;기준으로 나누어 리스트에 저장
    private List<String> splitStatements() {
        if(input.charAt(input.length() - 1) == ';') {
            semicolonFlag = true;
            input = input.substring(0, input.length() - 1);
        }
        List<String> result = new ArrayList<>();
        String[] tmp = input.split(";");
        for(String statement : tmp) {
            result.add(statement.trim());
        }
        for(int i = 0; i < result.size() - 1; i++) {
            result.set(i, result.get(i) + ";");
        }
        return result;
    }

    public void run(boolean optionFlag) {
        for(String tmp : Statements) {
            Parser statement = new Parser(tmp, optionFlag);
            if(semicolonFlag && tmp.equals(Statements.get(Statements.size() - 1))) {
                statement.addErrors(new Pair("(Warning)", "Semicolon in the last statement is not required. - Delete semicolon"));
            }
            statement.run();
        }
        if(!optionFlag) Parser.printResult();
    }
}
