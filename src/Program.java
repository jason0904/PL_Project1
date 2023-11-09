import java.util.ArrayList;
import java.util.List;

public class Program {

    private final String input;
    private List<String>Statements = new ArrayList<>();


    Program(String input) {
        this.input = input;
        this.Statements = splitStatements();
    }

    //문장을 ;기준으로 나누어 리스트에 저장
    private List<String> splitStatements() {
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
            statement.run();
        }
        Parser.printResult();
    }
}
