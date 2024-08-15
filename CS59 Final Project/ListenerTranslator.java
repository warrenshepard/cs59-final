/**
 * CS59 Project 03
 * Author: Warren Shepard and Daisy Li
 * Credit: none
 * Date: 14 Aug 2024
 */

import org.antlr.v4.runtime.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class ListenerTranslator extends AutogradeBaseListener{
    Grader grader;
    HashMap<String, ArrayList<TestCase>> tests = new HashMap<>(); // to store tests that user declares

    // get the grader name
    public void exitNew(AutogradeParser.NewContext ctx) {
        try {
            String graderName = ctx.name(0).getText();
            grader = new Grader();
        } catch (Exception e) {
            System.out.println("ERROR with new grader configuration: " + e);
        }
    }

    public void exitConfigure(AutogradeParser.ConfigureContext ctx) {
        try {
            if (ctx.id(1) != null) {
                String configureType = ctx.id(1).getText();

                if ("func".equals(configureType)) {

                    // Initialize parameters
                    String functionName = null;
                    ArrayList<TestCase> testCases = new ArrayList<>();
                    int points = -1;

                    // Loop through the config list to get function, test cases, and points
                    for (AutogradeParser.Config_itemContext itemCtx : ctx.config_list().config_item()) {
                        String itemName = itemCtx.config_key().getText();

                        if ("function".equals(itemName)) {
                            functionName = itemCtx.config_value().function_name().getText();
                            functionName = functionName.substring(0, functionName.length() - 2);
                        } else if ("test_cases".equals(itemName)) {
                            testCases = tests.get(itemCtx.config_value().getText());
                        } else if ("points".equals(itemName)) {
                            points = Integer.parseInt(itemCtx.config_value().getText());
                        }
                    }

                    // Create and configure the Function object
                    Function function = new Function(functionName, testCases);
                    if (points == -1) {
                        // If points are not specified, calculate as the sum of the test case points
                        points = testCases.stream().mapToInt(TestCase::getPoints).sum();
                    }
                    function.setPoints(points);
                    grader.addFunction(function);
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR with function configuration: " + e);
        }
    }


    // for configuring the grader: use config_item
    public void exitConfig_item(AutogradeParser.Config_itemContext ctx) {
        try {
            String key = ctx.config_key().getText();
            String value = ctx.config_value().getText();

            if (key.equals("due_date")) {
                grader.setDue_date(value);
            } else if (key.equals("late_penalty")) {
                Double penalty = Double.parseDouble(value);
                grader.setLate_penalty(penalty);
            } else if (key.equals("early_time")) {
                // the time is formated by a number than m, h, or d. For simplicity, we convert to minutes.
                String timeStr = value.substring(0, value.length() - 1);
                String unit = value.substring(value.length() - 1);
                Double time = Double.parseDouble(timeStr);
                if (unit.equals("h")) {
                    time = time * 60;
                } else if (unit.equals("d")) {
                    time = time * 24 * 60;
                }
                // actually set the time
                grader.setEarly_time(time);
            } else if (key.equals("early_reward")) {
                Double reward = Double.parseDouble(value);
                grader.setEarly_reward(reward);
            } else if (key.equals("functions")) {
                ArrayList<String> functions = new ArrayList<>();    // Arraylist to store functions
                if (ctx.config_value().function_name() != null) {
                    String function = ctx.config_value().function_name().getText();
                    function = function.substring(0, function.length() - 2);
                    functions.add(function);
                } else if (ctx.config_value().function_list() != null) {
                    String functionLst = ctx.config_value().function_list().getText();
                    functionLst = functionLst.replace("{", "").replace("}", ""); // get rid of brackets
                    String[] function_names = functionLst.split(",");

                    // loop through functions and add to list
                    for (String function : function_names) {
                        function = function.trim(); // trim spaces
                        function = function.substring(0, function.length() - 2); // strip off the "()"
                        functions.add(function); // add to list
                    }
                }
                // set the functions on the grader
                // grader.setFunctions(functions);
            }
        } catch (Exception e) {
            System.out.println("ERROR with grader parameter configuration: " + e);
        }
    }

    // for getting test cases -- use a map to get the (potenital) list of test cases from variable and test_list. use a map to store the test cases
    public void exitVariable(AutogradeParser.VariableContext ctx) {
        try {
            String name = ctx.name().getText();

            // if a test_list exists
            if (ctx.test_list() != null) {
                ArrayList<TestCase> testCases = new ArrayList<>();

                // loop over every test item in the test_list and add cases
                for (AutogradeParser.Test_itemContext testItemCtx : ctx.test_list().test_item()) {

                    // extract parameters
                    ArrayList<String> parameters = new ArrayList<>();
                    // if in a list, extract all the parameters
                    if (testItemCtx.input_list() != null) {
                        for (AutogradeParser.InputContext inputCtx : testItemCtx.input_list().input()) {
                            parameters.add(inputCtx.getText().trim());
                        }
                        // if a singleton, just extract and trim
                    } else {
                        parameters.add(testItemCtx.input().getText().trim());
                    }

                    // Extract the expected output
                    String expectedOutput = testItemCtx.expected_output().getText().trim();

                    // Extract the points, defaulting to 0 if not provided
                    int points = 0;
                    if (testItemCtx.points() != null) {
                        if (testItemCtx.points().num() != null) {
                            points = Integer.parseInt(testItemCtx.points().num().getText().trim());
                        } else if (testItemCtx.points().POINTS() != null) {
                            points = Integer.parseInt(testItemCtx.points().num().getText().trim());
                        }
                    }

                    // Create a new TestCase object and add it to the list
                    TestCase testCase = new TestCase(parameters, expectedOutput, points);
                    testCases.add(testCase);
                }
                // store list of tests for later use
                tests.put(name, testCases);
            }
        } catch (Exception e) {
            System.out.println("ERROR with TEST CASE configuration: " + e);
        }
    }

    // for configuring a function
    public void exitMethod_call(AutogradeParser.Method_callContext ctx) {
        try {
            String methodName = ctx.name().getText();

            if (methodName.equals("addsubmission")) {
                // Assuming the method call looks like grader.addsubmission {...}
                String name = null;
                String path = null;
                String day = null;
                String time = null;

                // Iterate over method_item in method_list
                for (AutogradeParser.Method_itemContext itemCtx : ctx.method_list().method_item()) {
                    String itemName = itemCtx.name().getText();
                    String itemValue = itemCtx.method_item_val().getText();

                    if ("name".equals(itemName)) {
                        name = itemValue.replace("\"", "");  // Remove quotes if necessary
                    } else if ("path".equals(itemName)) {
                        path = itemValue.replace("\"", "");  // Remove quotes if necessary
                    } else if ("day".equals(itemName)) {
                        day = itemValue;
                    } else if ("time".equals(itemName)) {
                        time = itemValue;
                    }
                    else {
                        System.out.println("ERROR: unexpected token");
                    }
                }
                if (name != null && path != null && day != null && time != null) {
                    Submission submission = new Submission(name, path, day, time);
                    grader.addSubmission(submission);
                }
                else {
                    System.out.println("ERROR with adding submission: possibly not every parameter specified");
                }
            }
            if (methodName.equals("grade")) {
                AutogradeParser.Method_itemContext itemCtx = ctx.method_list().method_item(0);

                // Check if this method item is the output path
                if ("output".equals(itemCtx.name().getText())) {
                    String outputPath = itemCtx.method_item_val().getText().replace("\"", ""); // Remove quotes if present
                    try {
                        grader.gradeAllSubmissions(outputPath);
                    }
                    catch (Exception e) {
                        System.out.println("ERROR with grading" + e);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR with SUBMISSION/GRADING configuration: " + e);
        }

    }

    public static void main(String[] args) throws Exception {

        //-------------------------------
        // IMPORTANT: Change below line to change the file being read!
        BufferedReader reader = new BufferedReader(new FileReader("inputs/valid_test.txt"));
        //-------------------------------

        // create error listeners
        SilentErrorListener errorListener = new SilentErrorListener();
        LexerErrorListener lexerErrorListener = new LexerErrorListener();

        StringBuilder fileContent = new StringBuilder();
        String line = reader.readLine();

        while (line != null) {
            fileContent.append(line).append("\n");
            line = reader.readLine();
        }
        reader.close();

        String[] sections = fileContent.toString().split("---");
        int testNumber = 1;

        for (String section : sections) {
            System.out.print("Processing test: " + testNumber);
            CharStream input = CharStreams.fromString(section);
            AutogradeLexer lexer = new AutogradeLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AutogradeParser parser = new AutogradeParser(tokens);

            // add custom error listeners
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            lexer.addErrorListener(lexerErrorListener);

            // Create a listener instance
            ListenerTranslator listener = new ListenerTranslator();
            parser.addParseListener(listener);

            // Start parsing
            parser.start();

            // check for errors
            if (errorListener.error() || lexerErrorListener.error() || tokens.LA(1) != Token.EOF) {
                System.out.println(" INVALID :(");
            } else {
                System.out.println(" VALID :)");
            }

            // reset error listeners
            errorListener.reset();
            lexerErrorListener.reset();

            testNumber++;
            Thread.sleep(100); // Sleep to ensure errors get printed along with test number (if this statement is not there then they will not)
        }
    }
}
