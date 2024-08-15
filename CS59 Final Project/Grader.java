/**
 * CS59 Project 03
 * Author: Warren Shepard and Daisy Li
 * Credit: none
 * Date: 14 Aug 2024
 */

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Grader {
//    hardcoding these in; on parser side, if one of these don't exist just set it to null.
    private String due_date;
    private Double late_penalty;
    private Double early_time;
    private Double early_reward;
    private ArrayList<Function> functions = new ArrayList<>();
    private ArrayList<Submission> submissions;

    // constructor
    Grader() {
        this.submissions = new ArrayList<Submission>();
    }

    Grader(
            String due_date,
    Double late_penalty,
    Double early_time,
    Double early_reward,
    ArrayList<Function> functions) {
        this.due_date = due_date;
        this.late_penalty = late_penalty;
        this.early_time = early_time;
        this.early_reward = early_reward;
        this.functions = functions;
        this.submissions = new ArrayList<Submission>();
    }

    public void addSubmission(Submission s) {
        submissions.add(s);
    }

    public void setDue_date(String dd) { due_date = dd; }
    public void setLate_penalty(Double lp) { late_penalty = lp; }
    public void setEarly_time(Double et) { early_time = et; }
    public void setEarly_reward(Double er) { early_reward = er; }
    public void addFunction(Function f) { functions.add(f); }

    public String getDue_date() { return due_date; }
    public Double getLate_penalty() { return late_penalty; }
    public Double getEarly_time() { return early_time; }
    public Double getEarly_reward() { return early_reward; }
    // public ArrayList<String> getFunctions() { return functions; }

//    actually run the test case

    public Double calc_mean() {
        Double sum = 0.0;
        for (int i = 0; i < submissions.size(); i++) {
            Submission s = submissions.get(i);
            sum += s.getScore();
        }
        return sum / submissions.size();
    }

    public Double calc_median() {
        ArrayList<Double> scores = new ArrayList<>();
        int size = submissions.size();
        for (int i = 0; i < size; i++) {
            Submission s = submissions.get(i);
            scores.add(s.getScore());
        }
        Collections.sort(scores);

        if (size % 2 == 0) {
            return (scores.get((size/2) - 1) + scores.get((size/2))) / 2;
        }
        else {
            return scores.get( size / 2);
        }
    }

    public Double calc_range() {
        ArrayList<Double> scores = new ArrayList<>();
        int size = submissions.size();
        for (int i = 0; i < size; i++) {
            Submission s = submissions.get(i);
            scores.add(s.getScore());
        }
        Collections.sort(scores);
        return scores.get(size-1) - scores.get(0);
    }

    public void gradeAllSubmissions(String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {

            // set the date! We're using this package called SimpleDateFormat
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            Date dueDate = dateFormat.parse(due_date + " 23:59");

            for (Submission submission : submissions) {
                Double totalScore = 0.0;
                writer.write("Grading submission: " + submission.getName() + "\n");


                //----------------------------------
                // see if they submitted it on time bc most of the time they don't
                // Parse the submission date and time
                Date submissionDate = dateFormat.parse(submission.getDay() + " " + submission.getTime());

                // Calculate time difference in minutes
                long timeDiff = submissionDate.getTime() - dueDate.getTime();
                long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);

                // keep track of multiplier to use at the end
                double finalScoreMultiplier = 1.0;

                // if late
                if (diffMinutes > 0 && late_penalty != null) {
                    finalScoreMultiplier -= late_penalty;
                } else if (diffMinutes < 0 && early_reward != null && early_time != null && (-diffMinutes) >= early_time) { //if early!
                    finalScoreMultiplier += early_reward;
                }
                //------------------------------------

                for (Function function : functions) {
                    Double functionScore = 0.0;
                    writer.write("  Function: " + function.name + "\n");
                    for (TestCase testCase : function.getTestcases()) {
                        try {
                            Class<?> clazz = Class.forName(submission.getPath());
                            Object instance = clazz.getDeclaredConstructor().newInstance();

                            // Get the method by name, assuming all parameters are integers
                            ArrayList<String> parameters = testCase.getParameters();
                            Class<?>[] paramTypes = new Class[parameters.size()];
                            for (int i = 0; i < parameters.size(); i++) {
                                paramTypes[i] = int.class; // All parameters are assumed to be integers. Otherwise it is overly complex :(
                            }
                            Method method = clazz.getMethod(function.name, paramTypes);

                            // Convert parameters to integers and invoke the method
                            Object result = method.invoke(instance, convertParameters(parameters));

                            // Compare the output with the expected result
                            if (result.toString().equals(testCase.getOutput().toLowerCase())) {
                                totalScore += testCase.getPoints();
                                functionScore += testCase.getPoints();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    writer.write("      Total score for function: " + functionScore + "\n");
                }
                // Update the submission score and set it
                totalScore = totalScore * finalScoreMultiplier;
                submission.setScore(totalScore);
                writer.write("Final score multiplier (from late/early penalty/reward): " + finalScoreMultiplier + "\n");
                writer.write("Final score for submission: " + totalScore + " * " + finalScoreMultiplier + " = " + submission.getScore() + "\n\n");
            }
            Double mean = calc_mean();
            Double median = calc_median();
            Double range = calc_range();

            writer.write("Statistics:\n");
            writer.write("Mean score: " + mean + "\n");
            writer.write("Median score: " + median + "\n");
            writer.write("Range of scores: " + range + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Convert String parameters (they're stored as strings) to integer parameters
    private Object[] convertParameters(ArrayList<String> parameters) {
        Object[] convertedParams = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            convertedParams[i] = Integer.parseInt(parameters.get(i));
        }
        return convertedParams;
    }

}
