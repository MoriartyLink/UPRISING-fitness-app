package project.uprising.Challenge;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import project.uprising.User.Session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomizeChallengeController {

    @FXML private ComboBox<Integer> durationComboBox;
    @FXML private ComboBox<Integer> minPerDayComboBox;
    @FXML private ComboBox<String> exerciseComboBox;
    @FXML private Button addExerciseButton;
    @FXML private VBox selectedExercisesBox;
    @FXML private Button saveButton;

    private List<Exercise> selectedExercises = new ArrayList<>(); // Changed to List<Exercise> for consistency
    private ChallengeDAO challengeDAO = new ChallengeDAO();

    @FXML
    public void initialize() {
        if (Session.getCurrentUser() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please log in to customize a challenge.");
            closeWindow();
            return;
        }

        // Populate durationComboBox
        durationComboBox.setItems(FXCollections.observableArrayList(7, 14, 21, 28, 30));
        durationComboBox.getSelectionModel().selectFirst();

        // Populate minPerDayComboBox
        minPerDayComboBox.setItems(FXCollections.observableArrayList(10, 20, 30, 40, 50, 60));
        minPerDayComboBox.getSelectionModel().selectFirst();

        // Populate exerciseComboBox with all exercises (original method)
        List<Exercise> exercises = challengeDAO.getAllExercises();
        if (exercises.isEmpty()) {
            System.err.println("No exercises found in database.");
            showAlert(Alert.AlertType.WARNING, "No Exercises", "No exercises available in the database.");
        } else {
            exerciseComboBox.setItems(FXCollections.observableArrayList(
                    exercises.stream().map(Exercise::getExerciseTitle).toList()
            ));
            exerciseComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void addExercise() {
        String selectedExerciseTitle = exerciseComboBox.getValue();
        if (selectedExerciseTitle != null && !selectedExercises.stream().anyMatch(e -> e.getExerciseTitle().equals(selectedExerciseTitle))) {
            Exercise selectedExercise = challengeDAO.getAllExercises().stream()
                    .filter(e -> e.getExerciseTitle().equals(selectedExerciseTitle))
                    .findFirst().orElse(null);
            if (selectedExercise != null) {
                if (selectedExercises.size() >= 2) { // Enforce max 2 exercises as per FXML label
                    showAlert(Alert.AlertType.WARNING, "Limit Reached", "You can only select up to 2 exercises.");
                    return;
                }
                selectedExercises.add(selectedExercise);
                HBox exerciseRow = new HBox(10);
                exerciseRow.getStyleClass().add("exercise-card");
                Label exerciseLabel = new Label(selectedExerciseTitle);
                exerciseLabel.getStyleClass().add("exercise-label");
                Button removeButton = new Button("Remove");
                removeButton.getStyleClass().add("remove-button");
                removeButton.setOnAction(e -> {
                    selectedExercises.remove(selectedExercise);
                    selectedExercisesBox.getChildren().remove(exerciseRow);
                });
                exerciseRow.getChildren().addAll(exerciseLabel, removeButton);
                selectedExercisesBox.getChildren().add(exerciseRow);
            }
        }
    }

    @FXML
    private void saveChallenge() {
        if (selectedExercises.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please select at least one exercise.");
            return;
        }

        int duration = durationComboBox.getValue() != null ? durationComboBox.getValue() : 7;
        int minPerDay = minPerDayComboBox.getValue() != null ? minPerDayComboBox.getValue() : 10;
        int userId = Session.getCurrentUser().getId();

        String challengeTitle = generateChallengeTitle(duration);
        CustomizedChallenge customChallenge = new CustomizedChallenge();
        customChallenge.setUserId(userId);
        customChallenge.setChallengeTitle(challengeTitle);
        customChallenge.setMinPerDay(minPerDay);
        customChallenge.setRequiredEquipments(compileRequiredEquipments());
        customChallenge.setExercises(selectedExercises.stream().map(Exercise::getExerciseTitle).toList());

        challengeDAO.saveCustomizedChallenge(customChallenge);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Challenge saved successfully!");
        closeWindow();
    }

    private String generateChallengeTitle(int duration) {
        StringBuilder title = new StringBuilder(duration + "-Day ");
        if (selectedExercises.size() == 1) {
            title.append(selectedExercises.get(0).getExerciseTitle()).append(" Challenge");
        } else {
            title.append(selectedExercises.get(0).getExerciseTitle())
                    .append(" & ")
                    .append(selectedExercises.get(1).getExerciseTitle())
                    .append(" Challenge");
        }
        return title.toString();
    }

    private String compileRequiredEquipments() {
        return String.join(", ", selectedExercises.stream()
                .map(Exercise::getRequiredEquipments)
                .filter(eq -> eq != null && !eq.isEmpty())
                .distinct()
                .toList());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
//09756643988