import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    //txt파일 불러옴
    public static void main(String[] args) throws FileNotFoundException {

        boolean optionFlag = false;

        if(args.length == 0 || args.length > 2) {
            System.out.println("Error");
            System.exit(1);
        }
        if(args.length == 2 && !args[0].equals("-v")) {
            System.out.println("Error");
            System.exit(1);
        }
        if(args[0].equals("-v")) {
            optionFlag = true;
        }

        Scanner scanner = new Scanner(new File(args[args.length-1]));

        StringBuilder lines = new StringBuilder();
        while(scanner.hasNextLine()) {
            lines.append(scanner.nextLine());
        }
        scanner.close();

        //공백과 개행문자 같이 지음
        String tmp = lines.toString().replaceAll(" ", "");
        tmp = tmp.replaceAll("\n", "");
        if(tmp.isEmpty()) {
            System.out.println();
            System.out.println("ID : 0 CONST : 0 OP : 0");
            System.out.println("(Error) - There is no input.");
            return;
        }

        //개행문자 제거후 한 문장으로 묶기
        String input;
        input = lines.toString().replaceAll("\n", "");
        Program program = new Program(input);
        program.run(optionFlag);
    }
}
