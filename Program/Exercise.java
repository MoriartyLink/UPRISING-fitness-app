package project.uprising.Program;

public class Exercise {
    private int exerciseID;
    private int exerciseImageID;
    private String targetMuscles;
    private String goal;
    private String experience;
    private String gender;
    private String requiredEquipments;
    private String exerciseTitle;
    private String repRange;
    private String weightRange;
    private String ageRange;
    private String guideline;

    public Exercise(int exerciseID, int exerciseImageID, String targetMuscles, String goal,
                    String experience, String gender, String requiredEquipments, String exerciseTitle,
                    String repRange, String weightRange, String ageRange, String guideline) {
        this.exerciseID = exerciseID;
        this.exerciseImageID = exerciseImageID;
        this.targetMuscles = targetMuscles;
        this.goal = goal;
        this.experience = experience;
        this.gender = gender;
        this.requiredEquipments = requiredEquipments;
        this.exerciseTitle = exerciseTitle;
        this.repRange = repRange;
        this.weightRange = weightRange;
        this.ageRange = ageRange;
        this.guideline = guideline;
    }

    public String getExerciseTitle() { return exerciseTitle; }
    public String getRepRange() { return repRange; }
    public String getGuideline() { return guideline; }

    public Object getExerciseId() {
        return exerciseID;
    }
}