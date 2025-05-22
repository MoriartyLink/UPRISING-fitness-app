package project.uprising.Challenge;

import java.util.List;

public class CustomizedChallenge {
    private int challengeId;
    private int userId;
    private String challengeTitle;
    private int minPerDay;
    private String requiredEquipments;
    private List<String> exercises;

    // Getters and Setters
    public int getChallengeId() { return challengeId; }
    public void setChallengeId(int challengeId) { this.challengeId = challengeId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getChallengeTitle() { return challengeTitle; }
    public void setChallengeTitle(String challengeTitle) { this.challengeTitle = challengeTitle; }

    public int getMinPerDay() { return minPerDay; }
    public void setMinPerDay(int minPerDay) { this.minPerDay = minPerDay; }

    public String getRequiredEquipments() { return requiredEquipments; }
    public void setRequiredEquipments(String requiredEquipments) { this.requiredEquipments = requiredEquipments; }

    public List<String> getExercises() { return exercises; }
    public void setExercises(List<String> exercises) { this.exercises = exercises; }
}