package project.uprising.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import project.uprising.User.Session;
public class User {
    private int id;
    private String username;
    private String phoneNo;
    private String password;
    private String email;
    private String gender;
    private LocalDate dateOfBirth; // Changed from int to LocalDate
    private int height;
    private double weight;
    private String goal;
    private String experience;


    private int exp;
    private LocalDate lastActiveDate;
    private int dailyExerciseCount;
    private int  workoutDaysPerWeek;
    private int level;



    public User(int id, String username, String phoneNo, String password, String email, String gender, LocalDate dateOfBirth, int height, double weight, String goal, String experience, int workoutDaysPerWeek,int exp) {
        this.id = id;
        this.username = username;
        this.phoneNo = phoneNo;
        this.password = password;
        this.email = email;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.height = height;
        this.weight = weight;
        this.goal = goal;
        this.experience = experience;
        this.workoutDaysPerWeek = workoutDaysPerWeek;
        this.exp = exp;
        this.level = level;
    }

    public User() {

    }


    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public  String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; } // Changed getter
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; } // Changed setter

    public int getHeight() {return height; } // New getter for height
    public void setHeight(int height) { this.height = height; }

    public double getWeight() {return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }

    public int getLevel() {
        return exp / 100; // 100 EXP per level
    }

    public double getMaxWeight() {
        int level = getLevel();
        if (level < 20) return 5.0; // Beginner
        else if (level < 60) return 15.0; // Intermediate
        else return 20.0; // Advanced (cap at owned weight)
    }

    public void decayExp() {
        LocalDate today = LocalDate.now();
        if (lastActiveDate != null && today.minusDays(5).isAfter(lastActiveDate)) {
            int daysInactive = (int) ChronoUnit.DAYS.between(lastActiveDate, today);
            int expLost = (daysInactive - 5) * 10; // 10 EXP per day after 5 days
            exp = Math.max(0, exp - expLost);
        }
    }

    // Getters and setters
    public LocalDate getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(LocalDate date) { this.lastActiveDate = date; }
    public int getDailyExerciseCount() { return dailyExerciseCount; }
    public void setDailyExerciseCount(int count) { this.dailyExerciseCount = count; }

    public int getWorkoutDaysPerWeek() { return workoutDaysPerWeek; }
    public void setWorkoutDaysPerWeek(int days){ this.workoutDaysPerWeek = days; }



}
