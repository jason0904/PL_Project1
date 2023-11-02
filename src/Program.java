import java.util.List;

public class Program {

    private String input;
    private List<String> Statements;

    Program(String input) {
        this.input = input;
        //;기준으로 자름
        this.Statements = List.of(input.split(";"));
    }

    public void run() {
        for(String tmp : Statements) {
            Parser statement = new Parser(tmp);
            statement.run();
        }
        Parser.printResult();
    }
}
