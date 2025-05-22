package project.uprising;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import project.uprising.User.Database;
import project.uprising.User.Session;
import project.uprising.User.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.util.Pair;

public class CheckBMIController {

    @FXML private TextField weightField;
    @FXML private ToggleButton unitToggle;
    @FXML private Label bmiResult;
    @FXML private Button backButton;
    @FXML private Button calculateButton;

    private User currentUser;
    private static final int MAX_DAILY_ENTRIES = 10;
    private Consumer<Pair<Double, Double>> bmiCallback; // Updated to Pair<Weight, BMI>

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("CheckBMIController - Set user: " + (user != null ? user.getUsername() : "null"));
        if (bmiResult != null) {
            bmiResult.setText(user != null ? "Enter weight to calculate BMI" : "Please log in to check BMI");
            System.out.println("Initial bmiResult text set to: " + bmiResult.getText());
        } else {
            System.err.println("bmiResult is null in setCurrentUser");
        }
    }

    public void setBMICallback(Consumer<Pair<Double, Double>> callback) {
        this.bmiCallback = callback;
        System.out.println("BMICallback set: " + (callback != null));
    }

    @FXML
    private void handleUnitToggle() {
        unitToggle.setText(unitToggle.isSelected() ? "kg" : "lb");
    }

    @FXML
    private void handleCalculateButton(ActionEvent event) {
        try {
            if (currentUser == null) {
                updateBmiResult("Error: No user data available. Please log in.");
                System.err.println("No current user in handleCalculateButton");
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightField.getText());
            } catch (NumberFormatException e) {
                updateBmiResult("Error: Enter a valid number");
                System.err.println("Invalid weight input: " + weightField.getText());
                return;
            }

            if (weight <= 0) {
                updateBmiResult("Error: Weight must be greater than 0");
                System.err.println("Invalid weight: " + weight);
                return;
            }

            // Convert to kg if lb is selected
            if (!unitToggle.isSelected()) { // lb selected
                weight *= 0.45359237; // Convert lb to kg
                System.out.println("Converted weight from lb to kg: " + weight);
            }

            double heightInMeters = currentUser.getHeight() / 100.0;
            if (heightInMeters <= 0) {
                updateBmiResult("Error: Invalid height in profile");
                System.err.println("Invalid height: " + currentUser.getHeight());
                return;
            }

            double bmi = weight / (heightInMeters * heightInMeters);
            System.out.println("Calculated BMI: " + bmi + " (weight=" + weight + " kg, height=" + heightInMeters + " m)");

            // Update MainDashboard with weight (in kg) and individual BMI
            updateMainDashboard(weight, bmi);

            // Store and process average
            try {
                storeTemporaryBMIRecord(weight, bmi);
                processDailyAverage();
            } catch (SQLException e) {
                System.err.println("Failed to store BMI record: " + e.getMessage());
                e.printStackTrace();
                updateBmiResult("Error: Database issue occurred");
            }

            weightField.clear();
        } catch (Exception e) {
            updateBmiResult("Error: Unexpected issue occurred");
            System.err.println("Unexpected error in handleCalculateButton: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBmiResult(String text) {
        if (bmiResult != null) {
            bmiResult.setText(text);
            System.out.println("Updated bmiResult to: " + text);
        } else {
            System.err.println("bmiResult is null; cannot update to: " + text);
        }
    }

    private void updateMainDashboard(double weightInKg, double bmi) {
        if (bmiCallback != null) {
            bmiCallback.accept(new Pair<>(weightInKg, bmi));
            System.out.println("Sent weight (kg): " + weightInKg + " and BMI: " + bmi + " to callback");
        } else {
            System.err.println("bmiCallback is null; cannot send data");
        }
    }

    private void storeTemporaryBMIRecord(double weight, double bmi) throws SQLException {
        String sql = "INSERT INTO bmi_temp (user_id, weight, bmi, record_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setDouble(2, weight); // Already in kg
            pstmt.setDouble(3, bmi);
            pstmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
            System.out.println("Stored temporary BMI record: " + bmi);
        }
    }

    private void processDailyAverage() throws SQLException {
        List<Double> dailyBMIs = new ArrayList<>();
        String selectSql = "SELECT bmi FROM bmi_temp WHERE user_id = ? AND record_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dailyBMIs.add(rs.getDouble("bmi"));
            }
        }

        int entriesToday = dailyBMIs.size();
        System.out.println("Entries today: " + entriesToday + ", Daily BMIs: " + dailyBMIs);
        if (entriesToday > 0 && entriesToday <= MAX_DAILY_ENTRIES) {
            double avgBMI = dailyBMIs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            updateBmiResult(String.format("BMI: %.2f (Entry %d/%d)", avgBMI, entriesToday, MAX_DAILY_ENTRIES));

            if (entriesToday == MAX_DAILY_ENTRIES) {
                double avgWeight = getAverageWeight();
                storeFinalBMIRecord(avgWeight, avgBMI);
                updateUserWeight(avgWeight);
                clearTemporaryRecords();
                updateBmiResult(String.format("Daily Avg BMI: %.2f (Complete)", avgBMI));
            }
        }
    }

    private double getAverageWeight() throws SQLException {
        String sql = "SELECT AVG(weight) FROM bmi_temp WHERE user_id = ? AND record_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = pstmt.executeQuery();
            double avgWeight = rs.next() ? rs.getDouble(1) : 0.0;
            System.out.println("Calculated average weight: " + avgWeight);
            return avgWeight;
        }
    }

    private void storeFinalBMIRecord(double weight, double bmi) throws SQLException {
        String sql = "INSERT INTO bmi_records (user_id, weight, bmi, record_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setDouble(2, weight); // Already in kg
            pstmt.setDouble(3, bmi);
            pstmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
            System.out.println("Stored final BMI record: " + bmi);
        }
    }

    private void updateUserWeight(double weight) throws SQLException {
        String sql = "UPDATE users SET weight = ? WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, weight); // Already in kg
            pstmt.setInt(2, currentUser.getId());
            pstmt.executeUpdate();
            currentUser.setWeight(weight);
            System.out.println("Updated user weight: " + weight);
        }
    }

    private void clearTemporaryRecords() throws SQLException {
        String sql = "DELETE FROM bmi_temp WHERE user_id = ? AND record_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
            System.out.println("Cleared temporary BMI records");
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}