package project.uprising.Program;

public class Program {
    private int programID;
    private String programTitle;
    private int duration;
    private String goal;
    private String requiredExperience;
    private String gender;
    private String explanation;

    public Program(int programID, String programTitle, int duration, String goal,
                   String requiredExperience, String gender, String explanation) {
        this.programID = programID;
        this.programTitle = programTitle;
        this.duration = duration;
        this.goal = goal;
        this.requiredExperience = requiredExperience;
        this.gender = gender;
        this.explanation = explanation;
    }

    public int getProgramID() { return programID; }
    public String getProgramTitle() { return programTitle; }
    public int getDuration() { return duration; }
    public String getGoal() { return goal; }
    public String getRequiredExperience() { return requiredExperience; }
    public String getGender() { return gender; }
    public String getExplanation() { return explanation; }
}