/**
 * CS59 Project 03
 * Author: Warren Shepard and Daisy Li
 * Credit: none
 * Date: 14 Aug 2024
 */

import java.util.ArrayList;

public class Function {
    String name;
    ArrayList<TestCase> testcases;
    int points;

    Function(String name) {
        this.name = name;
        this.testcases = new ArrayList<TestCase>();
    }
    Function(String name, ArrayList<TestCase> tests) {
        this.name = name;
        this.testcases = tests;
    }

    public ArrayList<TestCase> getTestcases() { return testcases; }
    public void setPoints(int p) { points = p;}
    public int getPoints() { return points; }
}
