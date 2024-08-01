import org.antlr.v4.runtime.*;
import java.io.BufferedReader;
import java.io.FileReader;
import org.antlr.v4.runtime.tree.ParseTree;

/*
 * CS59 P02: Functional Parser
 * Author: Warren Shepard and Daisy li
 * Date: 30 July 2024
 * Credit: Warren and Daisy code from assignment 01 and 02 and class slides
 */

public class Test {
    public static void main(String[] args) throws Exception {

        //-------------------------------
        // IMPORTANT: Change below line to change the file being read!
        BufferedReader reader = new BufferedReader(new FileReader("inputs/invalid_tests.txt"));
        //-------------------------------

        StringBuilder fileContent = new StringBuilder();
        String line = reader.readLine();

        while(line != null) {
            fileContent.append(line).append("\n");
            line = reader.readLine();
        }
        reader.close();

        String[] sections = fileContent.toString().split("---");
        int testNumber = 1;

        for (String section : sections) {
            System.out.println("Processing test " + testNumber);
            CharStream input = CharStreams.fromString(section);
            AutogradeLexer lexer = new AutogradeLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AutogradeParser parser = new AutogradeParser(tokens);

            ParseTree tree = parser.start(); // Parse the input starting from the 'start' rule

            // Check for unprocessed tokens --> error
            if (tokens.LA(1) != Token.EOF) {
                System.err.println("Syntax Error: Unprocessed tokens found in section " + testNumber + ". Input not fully parsed.");
            }

            testNumber++;
            Thread.sleep(100); // Sleep to make sure errors get printed along with test number (if this statement is not there then they will not)
        }
    }
}
