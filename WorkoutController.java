package project.uprising;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import project.uprising.User.Session;
import project.uprising.User.User;
import project.uprising.Workout.ExerciseController;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class WorkoutController implements Initializable {

    @FXML
    private ToggleButton btnBackMuscle, btnBicep, btnChest, btnShoulder, btnUpperBack, btnLowerBack, btnGlutes, btnLeg;

    @FXML
    private VBox exerciseList;

    private Connection conn;

    @FXML
    private ImageView bodyImage;

    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize database connection
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/project_db", "root", "12345");
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }

        loadMuscleMap();

    }
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    private void loadMuscleMap() {
        String gender = Session.getCurrentUser() != null ? Session.getCurrentUser().getGender() : null;
        System.out.println("Current user gender: " + gender);

        String imagePath;
        if (gender != null && gender.equalsIgnoreCase("male")) {
            imagePath = "ImageData/BodyMap/musclemap-male.png";
        } else if (gender != null && gender.equalsIgnoreCase("female")) {
            imagePath = "ImageData/BodyMap/musclemap-female.png";
        } else {
            imagePath = "ImageData/BodyMap/musclemap-female.png";
        }

        URL resourceUrl = getClass().getResource(imagePath);
        if (resourceUrl == null) {
            System.out.println("Error: Resource not found at " + imagePath);
            bodyImage.setImage(null); // Or set a placeholder
        } else {
            bodyImage.setImage(new Image(resourceUrl.toExternalForm()));
            System.out.println("Loaded image from: " + resourceUrl.toExternalForm());
        }
    }

    @FXML
    private void showExercises(ActionEvent event) {
        // Clear previous exercises
        exerciseList.getChildren().clear();

        // Check which toggle button is selected
        if (btnBackMuscle.isSelected()) {
            addExercises("Back");
        }
        if (btnBicep.isSelected()) {
            addExercises("Bicep");
        }
        if (btnChest.isSelected()) {
            addExercises("Chest");
        }
        if (btnShoulder.isSelected()) {
            addExercises("Shoulder");
        }
        if (btnUpperBack.isSelected()) {
            addExercises("Upper Back");
        }
        if (btnLowerBack.isSelected()) {
            addExercises("Lower Back");
        }
        if (btnGlutes.isSelected()) {
            addExercises("Glutes");
        }
        if (btnLeg.isSelected()) {
            addExercises("Leg");
        }
    }

    private void addExercises(String targetMuscle) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ExerciseID, exerciseTitle FROM Exercise WHERE targetMuscles LIKE '%" + targetMuscle + "%'")) {
            while (rs.next()) {
                int exerciseId = rs.getInt("ExerciseID");
                String exerciseTitle = rs.getString("exerciseTitle");

                // Create a card (HBox)
                HBox card = new HBox();
                card.setUserData(exerciseId); // Store ExerciseID
                card.setPrefHeight(100);
                card.setPrefWidth(400);
                card.setAlignment(Pos.CENTER);
                card.setPadding(new Insets(10));
                card.setStyle(
                        "-fx-background-color: rgba(102,102,255,0.68);" +
                                "-fx-translate-x: 20;"+
                                "-fx-background-radius: 15;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);"
                );

                // Exercise title
                Label titleLabel = new Label(exerciseTitle);
                titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Arial';");

                card.getChildren().add(titleLabel);

                // Modified click handler
                card.setOnMouseClicked(event -> {
                    HBox clickedCard = (HBox) event.getSource();
                    int selectedExerciseId = (int) clickedCard.getUserData();
                    showExerciseDetails(exerciseTitle, selectedExerciseId);
                });

                // Hover animation
                card.setOnMouseEntered(event -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                    st.setToX(1.05);
                    st.setToY(1.05);
                    st.play();
                });
                card.setOnMouseExited(event -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                    st.setToX(1);
                    st.setToY(1);
                    st.play();
                });

                // Fade-in animation
                card.setOpacity(0);
                exerciseList.getChildren().add(card);
                FadeTransition ft = new FadeTransition(Duration.millis(500), card);
                ft.setToValue(1);
                ft.play();
            }
        } catch (SQLException e) {
            System.out.println("Error fetching exercises: " + e.getMessage());
        }
    }

    private void showExerciseDetails(String exerciseTitle, int exerciseId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/Exercise.fxml"));
            AnchorPane exercisePane = loader.load();
            ExerciseController exerciseController = loader.getController();
            exerciseController.setExerciseDetails(exerciseTitle, exerciseId); // Pass both title and ID

            Stage newStage = new Stage();
            newStage.initStyle(StageStyle.UNDECORATED);
            newStage.setScene(new Scene(exercisePane));
            newStage.setTitle(exerciseTitle);
            newStage.setResizable(false);
            newStage.show();
        } catch (IOException e) {
            System.out.println("Error loading exercise details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}