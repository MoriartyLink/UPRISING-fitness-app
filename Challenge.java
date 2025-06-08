package project.uprising.Challenge;

public class Challenge {
    private int challengeId;
    private String challengeTitle;
    private int minPerDay;
    private String achievement;
    private int duration;
    private String goal;
    private String requiredExperience;
    private String gender;
    private int difficulty;

    // Getters and Setters
    public int getChallengeId() { return challengeId; }
    public void setChallengeId(int challengeId) { this.challengeId = challengeId; }

    public String getChallengeTitle() { return challengeTitle; }
    public void setChallengeTitle(String challengeTitle) { this.challengeTitle = challengeTitle; }

    public int getMinPerDay() { return minPerDay; }
    public void setMinPerDay(int minPerDay) { this.minPerDay = minPerDay; }

    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getRequiredExperience() { return requiredExperience; }
    public void setRequiredExperience(String requiredExperience) { this.requiredExperience = requiredExperience; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}