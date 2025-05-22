package project.uprising.Workout;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import project.uprising.User.Session;
import project.uprising.User.User;

import java.net.URL;
import java.sql.*;

public class ExerciseDemoController {

    @FXML private BorderPane borderPane;
    @FXML private ImageView exerciseGifView;
    @FXML private Button backButton;
    @FXML private Label exerciseTitleLabel;
    @FXML private Label guidelineLabel;

    private Connection conn;

    public void initialize() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/project_db", "root", "12345");
            // Bind ImageView width to BorderPane width minus guideline pane width
            exerciseGifView.fitWidthProperty().bind(borderPane.widthProperty().subtract(300));
            exerciseGifView.setPreserveRatio(true);
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    // Method to set exercise and load data
    public void setExercise(String exerciseTitle) {
        if (exerciseTitle == null || exerciseTitle.trim().isEmpty()) {
            System.out.println("Error: exerciseTitle is null or empty.");
            return;
        }
        exerciseTitleLabel.setText(exerciseTitle);
        loadGif(exerciseTitle);
        loadGuideline(exerciseTitle);
    }

    public void loadGif(String exerciseTitle) {
        if (exerciseTitle == null || exerciseTitle.trim().isEmpty()) {
            System.out.println("Error: exerciseTitle is null or empty. Cannot load GIF.");
            exerciseGifView.setImage(null);
            return;
        }

        try {
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                System.out.println("Error: No user logged in. Defaulting to UserID = 1.");
                currentUser = new User(1, "Guest", "", "", "", "Male", null, 0, 0.0, "", "",0,0);
            }

            int userId = currentUser.getId();
            String userGender = getUserGender(userId);
            String equipment = getRequiredEquipment(exerciseTitle);

            String gifFileName = constructGifFileName(exerciseTitle, userGender, equipment);
            String resourcePath = "/project/uprising/GifData/" + gifFileName + ".gif";
            URL gifUrl = getClass().getResource(resourcePath);

            if (gifUrl == null) {
                System.out.println("Error: GIF not found at: " + resourcePath);
                exerciseGifView.setImage(null);
                return;
            }

            Image gifImage = new Image(gifUrl.toExternalForm());
            exerciseGifView.setImage(gifImage);

            if (gifImage.isError()) {
                System.out.println("Failed to load GIF: " + resourcePath);
                exerciseGifView.setImage(null);
            }
        } catch (SQLException e) {
            System.out.println("Error loading GIF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGuideline(String exerciseTitle) {
        try {
            String sql = "SELECT guideline FROM Exercise WHERE exerciseTitle = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, exerciseTitle);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String guideline = rs.getString("guideline");
                guidelineLabel.setText(guideline != null ? guideline : "No guideline available.");
            } else {
                guidelineLabel.setText("No guideline found for this exercise.");
            }
        } catch (SQLException e) {
            System.out.println("Error loading guideline: " + e.getMessage());
            guidelineLabel.setText("Error loading guideline.");
        }
    }

    private String getUserGender(int userId) throws SQLException {
        String sql = "SELECT gender FROM users WHERE UserID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String gender = rs.getString("gender");
                return (gender != null && !gender.trim().isEmpty()) ? gender : "Male";
            }
            return "Male"; // Default
        }
    }

    private String getRequiredEquipment(String exerciseTitle) throws SQLException {
        String sql = "SELECT required_equipments FROM Exercise WHERE exerciseTitle = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, exerciseTitle);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String equipments = rs.getString("required_equipments");
                return equipments != null && !equipments.isEmpty() ? equipments : "None";
            }
            return "None";
        }
    }

    private String constructGifFileName(String exerciseTitle, String gender, String equipment) {
        String baseName = exerciseTitle.replace(" ", "").toLowerCase();
        String genderSuffix = gender.equalsIgnoreCase("Male") ? "-m" : "-w";
        String equipmentSuffix = equipment.equals("None") ? "" : "-" + equipment.replace(" ", "").toLowerCase();
        return baseName + genderSuffix + equipmentSuffix;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}