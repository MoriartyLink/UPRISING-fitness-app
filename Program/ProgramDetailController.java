package project.uprising.Program;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import project.uprising.Workout.ExerciseController;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class ProgramDetailController {
    @FXML private Label programTitle;
    @FXML private Label programName;
    @FXML private Label programDuration;
    @FXML private Label programExplanation;
    @FXML private Label programGoal;
    @FXML private Label programLevel;
    @FXML private Label programGender;
    @FXML private VBox exerciseList;

    private ProgramDAO programDAO = new ProgramDAO();
    private int programId;
    private Connection conn;

    @FXML
    private void initialize() {

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/project_db", "root", "12345");
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    public void setProgramId(int programId) {
        this.programId = programId;
        loadProgramDetails();
    }

    private void loadProgramDetails() {
        Program program = programDAO.getProgramById(programId);
        if (program != null) {
            programTitle.setText(program.getProgramTitle());
            programName.setText(program.getProgramTitle());
            programDuration.setText(program.getDuration() + " Days");
            programExplanation.setText(program.getExplanation() != null ? program.getExplanation() : "No description");
            programGoal.setText("Goal: " + program.getGoal());
            programLevel.setText("Level: " + program.getRequiredExperience());
            programGender.setText("For: " + program.getGender());

            loadExercises();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Program not found.");
        }
    }

    private void loadExercises() {
        exerciseList.getChildren().clear();
        List<Exercise> exercises = programDAO.getExercisesForProgram(programId);
        for (Exercise exercise : exercises) {
            Label exerciseLabel = new Label(exercise.getExerciseTitle() + " - " + exercise.getRepRange());
            exerciseLabel.getStyleClass().add("exercise-text");
            VBox item = new VBox(exerciseLabel);
            item.getStyleClass().add("exercise-item");
            item.setOnMouseClicked(event -> openExerciseDetail(exercise.getExerciseTitle()));
            exerciseList.getChildren().add(item);
        }
    }


    private void openExerciseDetail(String exerciseTitle) {
        try {
            if (exerciseTitle == null || exerciseTitle.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Exercise", "Exercise title is missing.");
                return;
            }
            // Get ExerciseID from database
            String sql = "SELECT ExerciseID FROM Exercise WHERE exerciseTitle = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
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
                exerciseController.setExerciseDetails(exerciseTitle, exerciseId);

                Stage stage = createSmoothStage(exercisePanel, "Exercise Details", 1550, 950);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.show();
            }
        } catch (IOException | SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Failed to load exercise details: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void showSchedule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("project/uprising/program_schedule.fxml"));
            Parent root = loader.load();
            ScheduleController controller = loader.getController();
            controller.setProgramId(programId);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);

            stage.setScene(new Scene(root, 800, 500));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open schedule: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType error, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
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
}