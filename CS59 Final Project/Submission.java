/**
 * CS59 Project 03
 * Author: Warren Shepard and Daisy Li
 * Credit: none
 * Date: 14 Aug 2024
 */

public class Submission {
    private String name;
    private String path;
    private String day;
    private String time;
    private Double score;

    public Submission(String name, String path, String day, String time) {
        this.name = name;
        this.path = path;
        this.day = day;
        this.time = time;
        score = -1.0;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getDay() { return day; }
    public String getTime() { return time; }
    public Double getScore() { return score; }

    public void setName(String n) { name = n; }

    public void setTime(String t) { time = t; }
    public void setScore(Double s) { score = s; }
}
