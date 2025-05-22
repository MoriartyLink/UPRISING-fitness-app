package project.uprising.Challenge;

import project.uprising.User.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExerciseDAO {

    public List<Exercise> getExercisesByEquipment(List<String> userEquipment) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM Exercise";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Exercise exercise = createExerciseFromResultSet(rs);
                System.out.println("Fetched: " + exercise.getExerciseTitle() +
                        ", RequiredEquipments: " + exercise.getRequiredEquipments());

                if (isEquipmentCompatible(exercise.getRequiredEquipments(), userEquipment)) {
                    exercises.add(exercise);
                    System.out.println("Added: " + exercise.getExerciseTitle() + " (equipment match)");
                }
            }

            if (exercises.isEmpty()) {
                System.out.println("No equipment matches, falling back to bodyweight exercises.");
                exercises.addAll(getBodyweightExercises());
            }

            return exercises;
        }
    }

    private List<Exercise> getBodyweightExercises() throws SQLException {
        List<Exercise> bodyweightExercises = new ArrayList<>();
        String sql = "SELECT * FROM Exercise WHERE RequiredEquipments IS NULL OR " +
                "TRIM(RequiredEquipments) = '' OR UPPER(TRIM(RequiredEquipments)) = 'BODYWEIGHT'";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Exercise exercise = createExerciseFromResultSet(rs);
                bodyweightExercises.add(exercise);
                System.out.println("Added bodyweight: " + exercise.getExerciseTitle() +
                        ", RequiredEquipments: " + exercise.getRequiredEquipments());
            }
            if (bodyweightExercises.isEmpty()) {
                System.err.println("No bodyweight exercises found in database!");
            }
            return bodyweightExercises;
        }
    }

    private Exercise createExerciseFromResultSet(ResultSet rs) throws SQLException {
        Exercise exercise = new Exercise();
        exercise.setExerciseId(rs.getInt("ExerciseID"));
        exercise.setExerciseImageId(rs.getInt("ExerciseImageID"));
        exercise.setTargetMuscles(rs.getString("TargetMuscles"));
        exercise.setGoal(rs.getString("Goal"));
        exercise.setExperience(rs.getString("Experience"));
        exercise.setGender(rs.getString("Gender"));
        exercise.setRequiredEquipments(rs.getString("RequiredEquipments"));
        exercise.setExerciseTitle(rs.getString("ExerciseTitle"));
        exercise.setRepRange(rs.getString("RepRange"));
        exercise.setWeightRange(rs.getString("WeightRange"));
        return exercise;
    }

    private boolean isEquipmentCompatible(String requiredEquipments, List<String> userEquipment) {
        List<String> normalizedUserEquipment = userEquipment.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();

        if (requiredEquipments == null || requiredEquipments.trim().isEmpty() ||
                requiredEquipments.trim().equalsIgnoreCase("Bodyweight")) {
            System.out.println("Compatible: No equipment or Bodyweight.");
            return true;
        }

        List<String> requiredList = Arrays.stream(requiredEquipments.split("\\s*,\\s*"))
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();

        System.out.println("Required: " + requiredList + ", User: " + normalizedUserEquipment);
        for (String required : requiredList) {
            if (!normalizedUserEquipment.contains(required)) {
                System.out.println("Incompatible: Missing " + required);
                return false;
            }
        }
        System.out.println("Compatible: All equipment matched.");
        return true;
    }
}