import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    //txt파일 불러옴
    public static void main(String[] args) throws FileNotFoundException {

        boolean optionFlag = false;
        /*
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

        String lines = "";
        while(scanner.hasNextLine()) {
            lines += scanner.nextLine();
        }
        scanner.close();
        */
        String tmp = "c := 1 + 1;\n" +
                "py := 2 % 2;\n" +
                "rs := 3 * 3;\n" +
                "rb := 4 $ 4";
        //개행문자 제거후 한 문장으로 묶기
        String input;
        input = tmp.replaceAll("\n", "");
        Program program = new Program(input);
        program.run(optionFlag);
    }

}
