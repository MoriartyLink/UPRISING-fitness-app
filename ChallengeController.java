package project.uprising.Challenge;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import project.uprising.ProgressController;
import project.uprising.User.Session;
import project.uprising.User.User;
import project.uprising.User.UserDAO;
import project.uprising.Workout.ExerciseController;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.ResourceBundle;

public class ChallengeController {

    @FXML private Label challengeTitleLabel;
    @FXML private Label requiredLevelLabel;
    @FXML private Label minPerDayLabel;
    @FXML private Label durationLabel;
    @FXML private Label achievementsLabel;
    @FXML private HBox equipmentHBox;
    @FXML private VBox exercisesVBox;
    @FXML private Button backButton;

    @FXML private Button completeButton;
    private int challengeId;


    private int customChallengeId = -1;
    private final ChallengeDAO challengeDAO = new ChallengeDAO();
    private Connection conn;
    private UserDAO userDao;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeDatabaseConnection();
        userDao = new UserDAO();
        completeButton.setOnAction(e -> completeChallenge());

    }


    // New method to handle database connection initialization/reconnection
    private void initializeDatabaseConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/project_db", "root", "12345");
                System.out.println("Database connection established successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            conn = null; // Ensure conn is null if connection fails
        }
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
        this.customChallengeId = -1; // Reset custom ID
        loadChallengeDetails();
    }

    public void setCustomChallengeId(int customChallengeId) {
        this.customChallengeId = customChallengeId;
        this.challengeId = -1; // Reset regular ID
        loadCustomChallengeDetails();
    }

    private void loadChallengeDetails() {
        if (challengeId == -1) return;

        Challenge challenge = challengeDAO.getChallengeById(challengeId);
        if (challenge != null) {
            challengeTitleLabel.setText(challenge.getChallengeTitle());
            requiredLevelLabel.setText(challenge.getRequiredExperience());
            minPerDayLabel.setText(challenge.getMinPerDay() + " min/day");
            durationLabel.setText(challenge.getDuration() + " days");
            achievementsLabel.setText(challenge.getAchievement() != null ? challenge.getAchievement() : "N/A");

            List<Exercise> exercises = challengeDAO.getExerciseDetailsForChallenge(challengeId);
            populateExercises(exercises);
            populateEquipment(exercises);
        } else {
            showAlert(Alert.AlertType.ERROR, "Challenge Not Found", "Could not load details for Challenge ID: " + challengeId);
        }
    }

    private void loadCustomChallengeDetails() {
        if (customChallengeId == -1) return;

        CustomizedChallenge customChallenge = challengeDAO.getCustomizedChallengeById(customChallengeId);
        if (customChallenge != null) {
            challengeTitleLabel.setText(customChallenge.getChallengeTitle());
            requiredLevelLabel.setText("Custom"); // No experience level for custom challenges
            minPerDayLabel.setText(customChallenge.getMinPerDay() + " min/day");
            durationLabel.setText(customChallenge.getMinPerDay() + " days"); // Using minPerDay as duration
            achievementsLabel.setText("Personal Goal Achievement"); // Custom placeholder

            List<Exercise> exercises = challengeDAO.getExerciseDetailsForCustomChallenge(customChallengeId);
            populateExercises(exercises);
            populateEquipment(exercises);
        } else {
            showAlert(Alert.AlertType.ERROR, "Custom Challenge Not Found", "Could not load details for Custom Challenge ID: " + customChallengeId);
        }
    }

    private void populateExercises(List<Exercise> exercises) {
        exercisesVBox.getChildren().clear();
        exercisesVBox.setSpacing(15);

        for (Exercise exercise : exercises) {
            HBox exerciseCard = new HBox();
            exerciseCard.setPrefHeight(120.0);
            exerciseCard.setPrefWidth(1450.0);
            exerciseCard.setAlignment(Pos.CENTER_LEFT);
            exerciseCard.setStyle(
                    "-fx-background-color: none; " +
                            "-fx-background-radius: 15; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
                            "-fx-padding: 10;"
            );
            exerciseCard.setSpacing(20.0);

            Button exerciseButton = new Button(exercise.getExerciseTitle());
            exerciseButton.setPrefHeight(100.0);
            exerciseButton.setPrefWidth(350.0);
            exerciseButton.setStyle(
                    "-fx-background-color:  rgba(62, 72, 97, 0.51); " +
                            "-fx-background-radius: 10; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-family: 'System Bold Italic';"
            );
            exerciseButton.setOnAction(this::StartExercise);
            exerciseButton.setOnMouseEntered(e -> exerciseButton.setStyle(
                    "-fx-background-color: rgba(25,25,62,0.59); " +
                            "-fx-background-radius: 10; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-family: 'System Bold Italic';"
            ));
            exerciseButton.setOnMouseExited(e -> exerciseButton.setStyle(
                    "-fx-background-color:  rgba(62, 72, 97, 0.51);" +
                            "-fx-background-radius: 10; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-family: 'System Bold Italic';"
            ));

            String descriptionText = String.format(
                    "%s (%s, %s)",
                    exercise.getGoal(),
                    exercise.getRepRange(),
                    exercise.getWeightRange() != null ? exercise.getWeightRange() : "N/A"
            );
            Label descriptionLabel = new Label(descriptionText);
            descriptionLabel.setPrefHeight(50.0);
            descriptionLabel.setPrefWidth(1050.0);
            descriptionLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            descriptionLabel.setFont(new Font("System Bold", 16.0));
            descriptionLabel.setAlignment(Pos.CENTER_LEFT);
            descriptionLabel.setPadding(new Insets(0, 0, 0, 20));

            exerciseCard.getChildren().addAll(exerciseButton, descriptionLabel);
            exercisesVBox.getChildren().add(exerciseCard);
            VBox.setMargin(exerciseCard, new Insets(0, 20, 0, 20));
        }
    }

    private void populateEquipment(List<Exercise> exercises) {
        equipmentHBox.getChildren().clear();
        List<String> uniqueEquipment = exercises.stream()
                .map(Exercise::getRequiredEquipments)
                .filter(eq -> eq != null && !eq.equals("None"))
                .distinct()
                .toList();

        if (uniqueEquipment.isEmpty()) {
            Label noEquipmentLabel = new Label("None");
            noEquipmentLabel.setPrefHeight(78.0);
            noEquipmentLabel.setPrefWidth(218.0);
            noEquipmentLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            noEquipmentLabel.setFont(new Font("System", 14.0));
            HBox.setMargin(noEquipmentLabel, new Insets(0, 0, 0, 20));
            equipmentHBox.getChildren().add(noEquipmentLabel);
        } else {
            for (String equipment : uniqueEquipment) {
                Label equipmentLabel = new Label(equipment);
                equipmentLabel.setPrefHeight(78.0);
                equipmentLabel.setPrefWidth(218.0);
                equipmentLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                equipmentLabel.setFont(new Font("System", 14.0));
                HBox.setMargin(equipmentLabel, new Insets(0, 0, 0, 20));
                equipmentHBox.getChildren().add(equipmentLabel);
            }
        }
    }

    public void StartExercise(ActionEvent actionEvent) {
        Button clickedButton = (Button) actionEvent.getSource();
        String exerciseTitle = clickedButton.getText();
        try {
            if (exerciseTitle == null || exerciseTitle.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Exercise", "Exercise title is missing.");
                return;
            }

            initializeDatabaseConnection();
            if (conn == null || conn.isClosed()) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to establish database connection.");
                return;
            }

            String sql = "SELECT ExerciseID FROM Exercise WHERE exerciseTitle = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, exerciseTitle);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int exerciseId = rs.getInt("ExerciseID");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/Exercise.fxml"));
                    if (loader.getLocation() == null) {
                        showAlert(Alert.AlertType.ERROR, "Resource Error", "Exercise.fxml could not be found.");
                        return;
                    }
                    Parent exercisePanel = loader.load();
                    ExerciseController exerciseController = loader.getController();
                    if (exerciseController == null) {
                        showAlert(Alert.AlertType.ERROR, "Controller Error", "ExerciseController could not be initialized.");
                        return;
                    }
                    int idToPass = (customChallengeId != -1) ? customChallengeId : challengeId;
                    boolean isCustom = (customChallengeId != -1);
                    exerciseController.setExerciseDetails(exerciseTitle, exerciseId, idToPass, isCustom);
                    Stage stage = createSmoothStage(exercisePanel, "Exercise Details", 1550, 950);
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.show();
                }
            }
        } catch (IOException | SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Failed to load exercise details: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void HandleExerciseDetail(ActionEvent actionEvent) {
        try {
            Parent profilePanel = FXMLLoader.load(getClass().getResource("exercise_detail.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Challenge");
            stage.setScene(new Scene(profilePanel, 600, 800));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleback(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), stage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> stage.close());
        fadeOut.play();
    }

    private Stage createSmoothStage(Parent root, String title, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(root, width, height);
        stage.setTitle(title);
        stage.setScene(scene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        stage.centerOnScreen();
        return stage;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void completeChallenge() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User not logged in.");
            return;
        }

        ProgressController progressController = new ProgressController();
        progressController.logChallengeCompletion(challengeId);

        Stage stage = (Stage) completeButton.getScene().getWindow();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), stage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> stage.close());
        fadeOut.play();
    }
}