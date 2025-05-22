package project.uprising.Challenge;

import project.uprising.User.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.sql.ResultSet;
import project.uprising.User.Database;
import project.uprising.User.User;
import project.uprising.User.UserDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChallengeDAO {

    public List<Challenge> getAllChallenges() {
        List<Challenge> challenges = new ArrayList<>();
        String sql = "SELECT * FROM Challenge";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Challenge challenge = new Challenge();
                challenge.setChallengeId(rs.getInt("ChallengeID"));
                challenge.setChallengeTitle(rs.getString("ChallengeTitle"));
                challenge.setMinPerDay(rs.getInt("minPerDay"));
                challenge.setAchievement(rs.getString("Achievement"));
                challenge.setDuration(rs.getInt("Duration"));
                challenge.setGoal(rs.getString("goal"));
                challenge.setRequiredExperience(rs.getString("required_experience"));
                challenge.setGender(rs.getString("gender"));
                challenges.add(challenge);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return challenges;
    }

    public List<Exercise> getExerciseDetailsForChallenge(int challengeId) {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT e.* FROM Exercise e " +
                "JOIN Challenge_Exercise ce ON e.ExerciseID = ce.ExerciseID " +
                "WHERE ce.ChallengeID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, challengeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Exercise exercise = new Exercise();
                    exercise.setExerciseId(rs.getInt("ExerciseID"));
                    exercise.setTargetMuscles(rs.getString("targetMuscles"));
                    exercise.setGoal(rs.getString("goal"));
                    exercise.setExperience(rs.getString("experience"));
                    exercise.setGender(rs.getString("gender"));
                    exercise.setRequiredEquipments(rs.getString("required_equipments"));
                    exercise.setExerciseTitle(rs.getString("exerciseTitle"));
                    exercise.setRepRange(rs.getString("repRange"));
                    exercise.setWeightRange(rs.getString("weightRange"));
                    exercises.add(exercise);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }

    public List<Exercise> getExerciseDetailsForCustomChallenge(int customChallengeId) {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT e.* FROM Exercise e " +
                "JOIN CustomizedChallenge_Exercise cce ON e.exerciseTitle = cce.ExerciseTitle " +
                "WHERE cce.CustomChallengeID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customChallengeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Exercise exercise = new Exercise();
                    exercise.setExerciseId(rs.getInt("ExerciseID"));
                    exercise.setTargetMuscles(rs.getString("targetMuscles"));
                    exercise.setGoal(rs.getString("goal"));
                    exercise.setExperience(rs.getString("experience"));
                    exercise.setGender(rs.getString("gender"));
                    exercise.setRequiredEquipments(rs.getString("required_equipments"));
                    exercise.setExerciseTitle(rs.getString("exerciseTitle"));
                    exercise.setRepRange(rs.getString("repRange"));
                    exercise.setWeightRange(rs.getString("weightRange"));
                    exercises.add(exercise);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }
    public class DatabaseConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/project_db"; // Replace with your DB URL
        private static final String USER = "root"; // Replace with your DB username
        private static final String PASSWORD = "12345"; // Replace with your DB password

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Exercise")) {
            while (rs.next()) {
                Exercise exercise = new Exercise();
                exercise.setExerciseId(rs.getInt("ExerciseID"));
                exercise.setExerciseTitle(rs.getString("ExerciseTitle"));
                exercise.setRequiredEquipments(rs.getString("RequiredEquipments"));
                exercises.add(exercise);
            }
            System.out.println("Exercises retrieved: " + exercises.size()); // Debug line
        } catch (SQLException e) {
            e.printStackTrace(); // Log the error
        }
        return exercises;
    }

    public List<CustomizedChallenge> getCustomizedChallengesForUser(int userId) {
        List<CustomizedChallenge> challenges = new ArrayList<>();
        String sql = "SELECT * FROM CustomizedChallenge WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CustomizedChallenge challenge = new CustomizedChallenge();
                    challenge.setChallengeId(rs.getInt("CustomChallengeID"));
                    challenge.setUserId(rs.getInt("UserID"));
                    challenge.setChallengeTitle(rs.getString("ChallengeTitle"));
                    challenge.setMinPerDay(rs.getInt("minPerDay"));
                    challenge.setRequiredEquipments(rs.getString("required_equipments"));
                    challenge.setExercises(getExercisesForCustomChallenge(rs.getInt("CustomChallengeID")));
                    challenges.add(challenge);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return challenges;
    }

    public CustomizedChallenge getCustomizedChallengeById(int customChallengeId) {
        String sql = "SELECT * FROM CustomizedChallenge WHERE CustomChallengeID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customChallengeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CustomizedChallenge challenge = new CustomizedChallenge();
                    challenge.setChallengeId(rs.getInt("CustomChallengeID"));
                    challenge.setUserId(rs.getInt("UserID"));
                    challenge.setChallengeTitle(rs.getString("ChallengeTitle"));
                    challenge.setMinPerDay(rs.getInt("minPerDay"));
                    challenge.setRequiredEquipments(rs.getString("required_equipments"));
                    challenge.setExercises(getExercisesForCustomChallenge(customChallengeId));
                    return challenge;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String> getExercisesForCustomChallenge(int customChallengeId) {
        List<String> exercises = new ArrayList<>();
        String sql = "SELECT ExerciseTitle FROM CustomizedChallenge_Exercise WHERE CustomChallengeID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customChallengeId);
            try (ResultSet rs = stmt.executeQuery()) { // Use ResultSet, not 'yourResultSet'
                while (rs.next()) {
                    exercises.add(rs.getString("ExerciseTitle"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }

    public void saveCustomizedChallenge(CustomizedChallenge challenge) {
        String sql = "INSERT INTO CustomizedChallenge (UserID, ChallengeTitle, minPerDay, required_equipments) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, challenge.getUserId());
            stmt.setString(2, challenge.getChallengeTitle());
            stmt.setInt(3, challenge.getMinPerDay());
            stmt.setString(4, challenge.getRequiredEquipments());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int challengeId = rs.getInt(1);
                    saveCustomChallengeExercises(challengeId, challenge.getExercises());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveCustomChallengeExercises(int challengeId, List<String> exercises) {
        String sql = "INSERT INTO CustomizedChallenge_Exercise (CustomChallengeID, ExerciseTitle) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String exercise : exercises) {
                stmt.setInt(1, challengeId);
                stmt.setString(2, exercise);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Challenge getChallengeById(int challengeId) {
        String sql = "SELECT * FROM Challenge WHERE ChallengeID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, challengeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Challenge challenge = new Challenge();
                challenge.setChallengeId(rs.getInt("ChallengeID"));
                challenge.setChallengeTitle(rs.getString("ChallengeTitle"));
                challenge.setMinPerDay(rs.getInt("minPerDay"));
                challenge.setAchievement(rs.getString("Achievement"));
                challenge.setDuration(rs.getInt("Duration"));
                challenge.setGoal(rs.getString("goal"));
                challenge.setRequiredExperience(rs.getString("required_experience"));
                challenge.setGender(rs.getString("gender"));
                challenge.setDifficulty(rs.getInt("difficulty")); // Fetch difficulty
                return challenge;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int mapExperienceToLevel(String requiredExperience) {
        switch (requiredExperience.toLowerCase()) {
            case "beginner":
                return 1;
            case "intermediate":
                return 2;
            case "advanced":
                return 3;
            default:
                return 1; // Default to beginner if unknown
        }
    }

    // Get user's level
    private int getUserLevel(int userId) throws SQLException {
        String sql = "SELECT level FROM users WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("level");
                }
            }
        }
        return 1; // Default to level 1 if not found
    }

    // Parse required equipments into a map of equipment type to required weight
    private Map<String, Double> parseRequiredEquipments(String requiredEquipments) {
        Map<String, Double> equipmentMap = new HashMap<>();
        if (requiredEquipments == null || requiredEquipments.trim().isEmpty()) {
            return equipmentMap;
        }

        // Split by comma to get individual equipment items (e.g., "dumbbells 5 kg, bench")
        String[] items = requiredEquipments.split(",");
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;

            String[] parts = item.split("\\s+");
            String equipmentType;
            Double weight = null;

            if (parts.length >= 2 && parts[parts.length - 1].equalsIgnoreCase("kg")) {
                // Equipment with weight, e.g., "dumbbells 5 kg"
                equipmentType = parts[0];
                try {
                    weight = Double.parseDouble(parts[parts.length - 2]);
                } catch (NumberFormatException e) {
                    weight = null;
                }
            } else {
                // Equipment without weight, e.g., "bench"
                equipmentType = item;
            }
            equipmentMap.put(equipmentType, weight);
        }
        return equipmentMap;
    }

    // Get user's equipment
    private Map<String, List<Double>> getUserEquipment(int userId) throws SQLException {
        Map<String, List<Double>> userEquipment = new HashMap<>();
        String sql = "SELECT EquipmentType, Weight FROM User_Equipment WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String equipmentType = rs.getString("EquipmentType");
                String weightStr = rs.getString("Weight");
                Double weight = null;
                if (weightStr != null && !weightStr.trim().isEmpty()) {
                    try {
                        weight = Double.parseDouble(weightStr.split(" ")[0]);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid weight: " + weightStr);
                        continue; // Skip this entry
                    }
                }
                userEquipment.computeIfAbsent(equipmentType, k -> new ArrayList<>()).add(weight);
            }
        }
        return userEquipment;
    }

    private Map<String, Double> getChallengeRequiredEquipment(int challengeId) throws SQLException {
        Map<String, Double> requiredEquipment = new HashMap<>();
        List<Exercise> exercises = getExerciseDetailsForChallenge(challengeId);
        for (Exercise exercise : exercises) {
            Map<String, Double> exerciseEquipment = parseRequiredEquipments(exercise.getRequiredEquipments());
            for (Map.Entry<String, Double> entry : exerciseEquipment.entrySet()) {
                String type = entry.getKey();
                Double weight = entry.getValue();
                Double currentMax = requiredEquipment.get(type);
                if (currentMax == null || (weight != null && weight > currentMax)) {
                    requiredEquipment.put(type, weight);
                }
            }
        }
        return requiredEquipment;
    }


    // Method to add a challenge as a customized challenge for a user
    public void addChallengeForUser(int userId, Challenge challenge) throws SQLException {
        // Create a new CustomizedChallenge based on the Challenge
        CustomizedChallenge customChallenge = new CustomizedChallenge();
        customChallenge.setUserId(userId);
        customChallenge.setChallengeTitle(challenge.getChallengeTitle() + " (Custom for User " + userId + ")");
        customChallenge.setMinPerDay(challenge.getMinPerDay());

        // Get required equipments from exercises
        Map<String, Double> requiredEquipment = getChallengeRequiredEquipment(challenge.getChallengeId());
        StringBuilder equipmentString = new StringBuilder();
        for (Map.Entry<String, Double> entry : requiredEquipment.entrySet()) {
            if (equipmentString.length() > 0) equipmentString.append(", ");
            equipmentString.append(entry.getKey());
            if (entry.getValue() != null) {
                equipmentString.append(" ").append(entry.getValue()).append(" kg");
            }
        }
        customChallenge.setRequiredEquipments(equipmentString.toString());

        // Get exercises for the challenge
        List<Exercise> exercises = getExerciseDetailsForChallenge(challenge.getChallengeId());
        List<String> exerciseTitles = new ArrayList<>();
        for (Exercise exercise : exercises) {
            exerciseTitles.add(exercise.getExerciseTitle());
        }
        customChallenge.setExercises(exerciseTitles);

        // Save the customized challenge
        saveCustomizedChallenge(customChallenge);
    }

    public List<Challenge> getFilteredChallenges(int userId) throws SQLException {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);
        if (user == null) return new ArrayList<>();


        List<Challenge> challenges = getAllChallenges();
        List<Challenge> filteredChallenges = new ArrayList<>();
        int userLevel = userDAO.getUserLevel(userId);
        String userGender = user.getGender();
        String userGoal = user.getGoal();
        int userAge = (int) ChronoUnit.YEARS.between(user.getDateOfBirth(), LocalDate.now());
        double userHeight = user.getHeight() / 100.0;
        double userWeight = user.getWeight();
        double userBMI = userWeight / (userHeight * userHeight);

        for (Challenge challenge : challenges) {
            System.out.println("Checking challenge: " + challenge.getChallengeTitle());
            String requiredLevel = challenge.getRequiredExperience();
            String challengeGender = challenge.getGender();
            String challengeGoal = challenge.getGoal();

                System.out.println("Checking challenge: " + challenge.getChallengeTitle());

                if (userLevel < getLevelForExperience(requiredLevel)) {
                    System.out.println(" - Excluded: Experience level too low");
                    continue;
                }
                if (!challengeGender.equalsIgnoreCase("All") && !challengeGender.equalsIgnoreCase(userGender)) {
                    System.out.println(" - Excluded: Gender doesn’t match");
                    continue;
                }
                if (!challengeGoal.equalsIgnoreCase(userGoal)) {
                    System.out.println(" - Excluded: Goal doesn’t match");
                    continue;
                }
                if (!isBMICompatible(challenge.getChallengeId(), userBMI)) {
                    System.out.println(" - Excluded: BMI doesn’t fit");
                    continue;
                }



            if (userLevel < getLevelForExperience(requiredLevel)) continue;
            if (!challengeGender.equalsIgnoreCase("All") && !challengeGender.equalsIgnoreCase(userGender)) continue;
            // Temporarily disable age compatibility check
            // if (!isAgeCompatible(challenge.getChallengeId(), userAge)) {
            //     System.out.println(" - Excluded due to age mismatch");
            //     continue;
            // }
            if (!challengeGoal.equalsIgnoreCase(userGoal)) continue;
            // Temporarily disable equipment check
            // if (!hasRequiredEquipment(userId, challenge.getChallengeId())) {
            //     System.out.println(" - Excluded due to equipment mismatch");
            //     continue;
            // }
            if (!isBMICompatible(challenge.getChallengeId(), userBMI)) continue;

            filteredChallenges.add(challenge);
        }
        return filteredChallenges;
    }

    private int getLevelForExperience(String requiredLevel) {
        if (requiredLevel == null) return 1;
        switch (requiredLevel.toLowerCase()) {
            case "beginner": return 1;
            case "intermediate": return 2;
            case "advanced": return 3;
            default: return 1;
        }
    }

    private boolean hasRequiredEquipment(int userId, int challengeId) throws SQLException {
        Map<String, List<Double>> userEquipment = getUserEquipment(userId);
        Map<String, Double> requiredEquipment = getChallengeRequiredEquipment(challengeId);
        return hasRequiredEquipment(userEquipment, requiredEquipment);
    }

//    private boolean hasRequiredEquipment(Map<String, List<Double>> userEquipment, Map<String, Double> requiredEquipment) {
//        for (Map.Entry<String, Double> req : requiredEquipment.entrySet()) {
//            String reqType = req.getKey();
//            Double reqWeight = req.getValue();
//            List<Double> userWeights = userEquipment.getOrDefault(reqType, new ArrayList<>());
//            if (userWeights.isEmpty() && reqWeight != null) return false;
//            if (reqWeight != null && !userWeights.stream().anyMatch(w -> w != null && w >= reqWeight)) {
//                return false;
//            }
//        }
//        return true;
//    }

    private boolean isAgeCompatible(int challengeId, int userAge) throws SQLException {
        String sql = "SELECT e.age_range FROM Exercise e " +
                "JOIN Challenge_Exercise ce ON e.ExerciseID = ce.ExerciseID " +
                "WHERE ce.ChallengeID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, challengeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String ageRange = rs.getString("age_range");
                if (isAgeInRange(ageRange, userAge)) return true;
            }
        }
        return false;
    }

    public boolean isAgeInRange(String ageRange, int userAge) {
        if (ageRange == null || ageRange.trim().isEmpty() || ageRange.equalsIgnoreCase("All")) {
            return true; // Treat null, empty, or "All" as "all ages"
        }
        try {
            if (ageRange.contains("-")) {
                String[] range = ageRange.split("-");
                int minAge = Integer.parseInt(range[0].trim());
                int maxAge = Integer.parseInt(range[1].trim());
                return userAge >= minAge && userAge <= maxAge;
            } else if (ageRange.endsWith("+")) {
                int minAge = Integer.parseInt(ageRange.replace("+", "").trim());
                return userAge >= minAge;
            } else {
                int exactAge = Integer.parseInt(ageRange.trim());
                return userAge == exactAge;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid age range format: " + ageRange);
            return false;
        }
    }

    // Fetch user details from the database
    private User getUserDetails(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("UserID"));
                user.setGender(rs.getString("gender"));
                user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                user.setGoal(rs.getString("goal"));
                user.setExperience(rs.getString("experience"));
                user.setHeight(rs.getInt("height"));
                user.setWeight(rs.getDouble("weight"));
                return user;
            }
        }
        return null;
    }



    // Check if user has the required equipment
    private boolean hasRequiredEquipment(Map<String, List<Double>> userEquipment, Map<String, Double> requiredEquipment) {
        List<Double> userWeights = userEquipment.getOrDefault("Generic", new ArrayList<>());
        for (Map.Entry<String, Double> req : requiredEquipment.entrySet()) {
            Double reqWeight = req.getValue();
            if (reqWeight != null && !userWeights.stream().anyMatch(w -> w != null && w >= reqWeight)) {
                return false;
            }
        }
        return !userWeights.isEmpty() || requiredEquipment.isEmpty();
    }

    // Calculate BMI for the user
    private double calculateBMI(int userId) throws SQLException {
        String sql = "SELECT height, weight FROM users WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int height = rs.getInt("height"); // in cm
                double weight = rs.getDouble("weight"); // in kg
                double heightInMeters = height / 100.0;
                return heightInMeters > 0 ? weight / (heightInMeters * heightInMeters) : 0.0;
            }
        }
        return 0.0; // Default if no data
    }

    // BMI compatibility based on difficulty
    private boolean isBMICompatible(int challengeId, double bmi) throws SQLException {
//        Challenge challenge = getChallengeById(challengeId);
//        int difficulty = challenge != null ? challenge.getDifficulty() : 1;
//        if (bmi == 0.0) return true; // No BMI set, allow all
//        if (bmi < 18.5) return difficulty <= 2; // Underweight
//        if (bmi >= 18.5 && bmi <= 24.9) return true; // Normal weight
//        if (bmi >= 25 && bmi <= 29.9) return difficulty >= 2; // Overweight
//        return difficulty >= 3; // Obese

        return true;
    }
}