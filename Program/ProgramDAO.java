package project.uprising.Program;

import project.uprising.User.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgramDAO {
    public List<Program> getAllPrograms() throws SQLException {
        List<Program> programs = new ArrayList<>();
        String sql = "SELECT * FROM Program";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                programs.add(new Program(
                        rs.getInt("ProgramID"),
                        rs.getString("ProgramTitle"),
                        rs.getInt("Duration"),
                        rs.getString("goal"),
                        rs.getString("required_experience"),
                        rs.getString("gender"),
                        rs.getString("explanation")
                ));
            }
        }
        return programs;
    }

    public Program getProgramById(int programId) {
        String sql = "SELECT * FROM Program WHERE ProgramID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Program(
                        rs.getInt("ProgramID"),
                        rs.getString("ProgramTitle"),
                        rs.getInt("Duration"),
                        rs.getString("goal"),
                        rs.getString("required_experience"),
                        rs.getString("gender"),
                        rs.getString("explanation")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Exercise> getExercisesForProgram(int programId) {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT e.* FROM Exercise e JOIN Program_Exercise pe ON e.ExerciseID = pe.ExerciseID WHERE pe.programID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                exercises.add(new Exercise(
                        rs.getInt("ExerciseID"),
                        rs.getInt("exerciseImageID"),
                        rs.getString("targetMuscles"),
                        rs.getString("goal"),
                        rs.getString("experience"),
                        rs.getString("gender"),
                        rs.getString("required_equipments"),
                        rs.getString("exerciseTitle"),
                        rs.getString("repRange"),
                        rs.getString("weightRange"),
                        rs.getString("age_range"),
                        rs.getString("guideline")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }

    public List<Exercise> getExercisesForProgramDay(int programId, String dayString) {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT e.* " +
                "FROM Exercise e " +
                "JOIN Program_Exercise pe ON e.ExerciseID = pe.ExerciseID " +
                "WHERE pe.programID = ? AND pe.day = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            stmt.setString(2, dayString);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                exercises.add(new Exercise(
                        rs.getInt("ExerciseID"),
                        rs.getInt("exerciseImageID"),
                        rs.getString("targetMuscles"),
                        rs.getString("goal"),
                        rs.getString("experience"),
                        rs.getString("gender"),
                        rs.getString("required_equipments"),
                        rs.getString("exerciseTitle"),
                        rs.getString("repRange"),
                        rs.getString("weightRange"),
                        rs.getString("age_range"),
                        rs.getString("guideline")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching exercises for ProgramID: " + programId + ", Day: " + dayString + " - " + e.getMessage());
        }
        return exercises;
    }
}