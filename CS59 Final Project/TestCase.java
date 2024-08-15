/**
 * CS59 Project 03
 * Author: Warren Shepard and Daisy Li
 * Credit: none
 * Date: 14 Aug 2024
 */

import java.util.ArrayList;

public class TestCase {
    int points;
    ArrayList<String> parameters;
    String output;

    TestCase(ArrayList<String> parameters, String output, int points) {
        this.parameters = parameters;
        this.points = points;
        this.output = output;
    }

    public int getPoints() { return points; }
    public String getOutput() { return output; }
    public ArrayList<String> getParameters() { return parameters; }
}
