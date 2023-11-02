import java.util.ArrayList;
import java.util.List;

public class Program {

    private String input;
    private List<String>Statements = new ArrayList<>();


    Program(String input) {
        this.input = input;
        this.Statements = splitStatements(input);
    }

    //문장을 ;기준으로 나누어 리스트에 저장
    private List<String> splitStatements(String input) {
        List<String> result = new ArrayList<>();
        String[] tmp = input.split(";");
        for(String statement : tmp) {
            result.add(statement.trim());
        }
        return result;
    }

    public void run(boolean optionFlag) {
        for(String tmp : Statements) {
            Parser statement = new Parser(tmp, optionFlag);
            statement.run();
        }
        Parser.printResult();
    }
}
