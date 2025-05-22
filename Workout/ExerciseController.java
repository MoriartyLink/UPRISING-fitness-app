package project.uprising.Workout;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import project.uprising.User.Session;
import project.uprising.User.User;
import project.uprising.User.UserDAO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ExerciseController implements Initializable {

    @FXML private Label exerciseTitleLabel;
    @FXML private Label equipment1Label, equipment2Label, equipment3Label;
    @FXML private Label repLabel;
    @FXML private Button backButton, howToButton, finishButton;
    @FXML private HBox weightCardContainer;
    @FXML private Label warningLabel;
    @FXML private ImageView exerciseImage;

    private String currentExerciseTitle;
    private int currentExerciseId;
    private int parentChallengeId = -1;
    private boolean isCustomChallenge = false;
    private String requiredEquipments;
    private Connection conn;
    private UserDAO userDao = new UserDAO();
    private String selectedWeight;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/project_db", "root", "12345");
            System.out.println("Database connection established.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to database: " + e.getMessage());
        }
        weightCardContainer.setPrefWidth(800); // Increased from default (e.g., 600) to 800px
        weightCardContainer.setMinWidth(600);

        backButton.setOnAction(this::handleback);
        finishButton.setOnAction(event -> handleFinishButton());


        // Wrap weightCardContainer in a ScrollPane for horizontal scrolling
        Parent parent = weightCardContainer.getParent();
        if (parent instanceof Pane) {
            Pane pane = (Pane) parent;
            int index = pane.getChildren().indexOf(weightCardContainer);
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(weightCardContainer);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; .scroll-pane .scroll-bar:vertical {\n" +
                    "    -fx-pref-height: 10;\n" +
                    "    -fx-background-color: none;\n" +
                    "}\n" +
                    ".scroll-pane .thumb{\n" +
                    "    -fx-background-color: linear-gradient(to top, #c2caff, #6666FF);\n" +
                    "    -fx-background-radius: 5;\n" +
                    "}");
            scrollPane.setPrefWidth(800);
            pane.getChildren().remove(weightCardContainer);
            pane.getChildren().add(index, scrollPane);
        }
    }

    public void setUser(User user) {
        this.currentUser = user;

    }



    public void setExerciseDetails(String exerciseTitle, int exerciseId, int challengeId, boolean isCustom) {
        this.currentExerciseId = exerciseId;
        this.parentChallengeId = challengeId;
        this.isCustomChallenge = isCustom;
        if (exerciseTitle == null || exerciseTitle.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Exercise title cannot be empty.");
            return;
        }
        exerciseTitleLabel.setText(exerciseTitle);
        this.currentExerciseTitle = exerciseTitle;

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT required_equipments, repRange FROM Exercise WHERE exerciseID = ?")) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                requiredEquipments = rs.getString("required_equipments");
                String[] equipments = requiredEquipments.split(",");
                updateEquipmentLabels(equipments);
                repLabel.setText(rs.getString("repRange") != null ? rs.getString("repRange").trim() : "N/A");
                populateWeightCards(requiredEquipments);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Query Error", "Failed to fetch details: " + e.getMessage());
        }
    }

    public void setExerciseDetails(String exerciseTitle, int exerciseId) {
        this.currentExerciseId = exerciseId;
        if (exerciseTitle == null || exerciseTitle.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Exercise title cannot be empty.");
            return;
        }
        exerciseTitleLabel.setText(exerciseTitle);
        this.currentExerciseTitle = exerciseTitle;
        System.out.println("Setting exercise details: " + exerciseTitle + ", ID: " + exerciseId);

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT required_equipments, repRange FROM Exercise WHERE exerciseID = ?")) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                requiredEquipments = rs.getString("required_equipments");
                String[] equipments = requiredEquipments.split(",");
                updateEquipmentLabels(equipments);
                repLabel.setText(rs.getString("repRange") != null ? rs.getString("repRange").trim() : "N/A");
                System.out.println("Required equipments: " + requiredEquipments);
                populateWeightCards(requiredEquipments);
            } else {
                System.out.println("No exercise found for ID: " + exerciseId);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Query Error", "Failed to fetch details: " + e.getMessage());
            System.out.println("SQL Exception in setExerciseDetails: " + e.getMessage());
        }
    }

    private void populateWeightCards(String requiredEquipments) {
        weightCardContainer.getChildren().clear();
        weightCardContainer.setSpacing(15); // Add spacing between cards
        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            Label noLoginLabel = new Label("Login Required");
            noLoginLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            weightCardContainer.getChildren().add(noLoginLabel);
            return;
        }

        if ("None".equals(requiredEquipments)) {
            StackPane card = createWeightCard("Bodyweight", false);
            weightCardContainer.getChildren().add(card);
            selectedWeight = "Bodyweight";
            return;
        }

        String sql = "SELECT Weight FROM User_Equipment WHERE UserID = ? ORDER BY CAST(SUBSTRING_INDEX(Weight, ' ', 1) AS DECIMAL)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            double maxWeight = currentUser.getMaxWeight();
            int cardCount = 0;

            while (rs.next()) {
                String weightStr = rs.getString("Weight");
                if (weightStr == null || weightStr.trim().equals("N/A")) {
                    StackPane card = createWeightCard("Bodyweight", false);
                    weightCardContainer.getChildren().add(card);
                    if (selectedWeight == null) selectedWeight = "Bodyweight";
                    cardCount++;
                    continue;
                }
                try {
                    double weight = Double.parseDouble(weightStr.split(" ")[0]);
                    boolean isLocked = weight > maxWeight;
                    StackPane card = createWeightCard(weightStr, isLocked);
                    weightCardContainer.getChildren().add(card);
                    if (!isLocked && selectedWeight == null) selectedWeight = weightStr;
                    cardCount++;
                } catch (NumberFormatException e) {
                    StackPane card = createWeightCard(weightStr + " (Invalid)", true);
                    weightCardContainer.getChildren().add(card);
                    cardCount++;
                }
            }
            if (cardCount == 0) {
                Label noWeightLabel = new Label("No Equipment Available");
                noWeightLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                weightCardContainer.getChildren().add(noWeightLabel);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch equipment: " + e.getMessage());
        }
    }
    private StackPane createWeightCard(String weightStr, boolean isLocked) {
        StackPane card = new StackPane();
        card.setPrefWidth(250);
        card.setPrefHeight(50);
        card.getStyleClass().add("weight-card");
        if (isLocked) {
            card.getStyleClass().add("locked");
        }

        HBox content = new HBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);

        ImageView icon = new ImageView();
        String iconPath = weightStr.equals("Bodyweight") ? "/project/uprising/ImageData/Exercise/bodyweight.png" : "/project/uprising/ImageData/Exercise/dumbbell.png";
        InputStream iconStream = getClass().getResourceAsStream(iconPath);
        if (iconStream != null) {
            icon.setImage(new Image(iconStream));
        } else {
            System.err.println("Icon not found: " + iconPath);
            // Fallback to a default icon if available
            icon.setImage(new Image(getClass().getResourceAsStream("/project/uprising/ImageData/Exercise/bodyweight.png")));
        }
        icon.setFitWidth(30);
        icon.setFitHeight(30);

        Label weightLabel = new Label(weightStr);
        weightLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        weightLabel.setWrapText(true); // Enable text wrapping
        weightLabel.setMaxWidth(170);  // Set max width to fit within card (250 - 40 icon - 15 spacing - 20 padding)
        weightLabel.setMinWidth(100);  // Ensure it has a minimum size for readability
        weightLabel.setVisible(true);  // Explicitly ensure visibility
        content.getChildren().addAll(icon, weightLabel);
        card.getChildren().add(content);

        if (isLocked) {
            InputStream lockStream = getClass().getResourceAsStream("/project/uprising/ImageData/Exercise/lock.png");
            if (lockStream != null) {
                ImageView lockIcon = new ImageView(new Image(lockStream));
                lockIcon.setFitWidth(20);
                lockIcon.setFitHeight(20);
                card.getChildren().add(lockIcon);
                StackPane.setAlignment(lockIcon, javafx.geometry.Pos.TOP_RIGHT);
            } else {
                System.err.println("Lock icon not found: /images/lock.png");
            }
        }

        if (!isLocked) {
            card.setOnMouseClicked(event -> {
                weightCardContainer.getChildren().forEach(node -> {
                    if (node instanceof StackPane) {
                        node.getStyleClass().remove("selected");
                    }
                });
                card.getStyleClass().add("selected");
                selectedWeight = weightStr;
                warningLabel.setVisible(false);
            });
            card.setOnMouseEntered(event -> {
                card.setScaleX(1.05);
                card.setScaleY(1.05);
            });
            card.setOnMouseExited(event -> {
                card.setScaleX(1.0);
                card.setScaleY(1.0);
            });
        } else {
            card.setOnMouseClicked(event -> {
                try {
                    double weightValue = Double.parseDouble(weightStr.split(" ")[0]);
                    int requiredLevel = getRequiredLevel(weightValue);
                    warningLabel.setText("Level " + requiredLevel + " Required to Use " + weightStr);
                    warningLabel.setVisible(true);
                } catch (NumberFormatException e) {
                    warningLabel.setText("Invalid Weight Format");
                    warningLabel.setVisible(true);
                }
            });
        }

        return card;
    }

    private int getRequiredLevel(double weight) {
        if (weight <= 5.0) return 1;
        else if (weight <= 15.0) return 20;
        else return 60;
    }

    private void updateEquipmentLabels(String[] equipments) {
        equipment1Label.setText(equipments.length > 0 ? equipments[0].trim() : "No Equipment");
        equipment1Label.setVisible(true);
        equipment2Label.setText(equipments.length > 1 ? equipments[1].trim() : "");
        equipment2Label.setVisible(equipments.length > 1);
        equipment3Label.setText(equipments.length > 2 ? equipments[2].trim() : "");
        equipment3Label.setVisible(equipments.length > 2);
    }

    @FXML
    private void openExerciseDemo() {
        if (currentExerciseTitle == null || currentExerciseTitle.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "No exercise selected.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/ExerciseDemo.fxml"));
            Parent root = loader.load();
            ExerciseDemoController demoController = loader.getController();
            demoController.setExercise(currentExerciseTitle);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.setTitle("Exercise Demo");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load exercise demo: " + e.getMessage());
        }
    }

    @FXML
    private void handleFinishButton() {
        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User not logged in.");
            return;
        }

        LocalDate today = LocalDate.now();
        int attempts = userDao.getExerciseAttemptsToday(currentUser.getId(), currentExerciseId, today);

        if (attempts > 0) {
            if (attempts == 1) {
                showAlert(Alert.AlertType.WARNING, "Repeat Detected", "You’ve already completed this exercise today!");
            } else if (attempts == 2) {
                showAlert(Alert.AlertType.WARNING, "Stop Repeating", "Stop repeating this exercise! Next attempt will reduce EXP.");
            } else if (attempts >= 3) {
                int penaltyExp = 50;
                int newExp = Math.max(0, currentUser.getExp() - penaltyExp);
                currentUser.setExp(newExp);
                userDao.updateUserExp(currentUser.getId(), newExp);
                showAlert(Alert.AlertType.ERROR, "Penalty Applied", "EXP reduced by " + penaltyExp + " for repeating exercises!");
            }
            userDao.logProgress(currentUser.getId(), "exercise", currentExerciseId, today, 0);
            closeStage();
            return;
        }

        try {
            boolean success = userDao.logProgress(currentUser.getId(), "exercise", currentExerciseId, today, 0);
            if (success && parentChallengeId != -1) {
                String sqlUpdate = "UPDATE User_challenges SET last_completed_date = ? " +
                        "WHERE user_id = ? AND challenge_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setDate(1, java.sql.Date.valueOf(today));
                    stmt.setInt(2, currentUser.getId());
                    stmt.setInt(3, parentChallengeId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Updated last_completed_date for challenge " + parentChallengeId);
                    }
                }

                if (isChallengeCompletedToday(currentUser.getId(), parentChallengeId, isCustomChallenge)) {
                    userDao.logProgress(currentUser.getId(), "challenge", parentChallengeId, today, 0);
                    showAlert(Alert.AlertType.INFORMATION, "Challenge Progress", "Challenge step completed today!");
                }
            }

            if (success) {
                int baseExp = 50;
                int bonusExp = 0;
                if (selectedWeight != null && !selectedWeight.equals("Bodyweight")) {
                    try {
                        bonusExp = (int) Double.parseDouble(selectedWeight.split(" ")[0]);
                    } catch (NumberFormatException e) {
                        bonusExp = 0;
                    }
                }
                int totalExp = baseExp + bonusExp;
                int oldExp = currentUser.getExp();
                int newExp = oldExp + totalExp;
                int oldLevel = currentUser.getLevel();
                currentUser.setExp(newExp);
                userDao.updateUserExp(currentUser.getId(), newExp);
                int newLevel = currentUser.getLevel();

                String message = "Exercise logged! Gained " + totalExp + " EXP.";
                if (newLevel > oldLevel) {
                    message += "\nLevel Up! Reached Level " + newLevel + "!";
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", message);
                closeStage();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to log exercise or update challenge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isChallengeCompletedToday(int userId, int challengeId, boolean isCustom) throws SQLException {
        String exerciseTable = isCustom ? "CustomizedChallenge_Exercise" : "Challenge_Exercise";
        String idColumn = isCustom ? "CustomChallengeID" : "ChallengeID";
        String exerciseIdColumn = isCustom ? "ExerciseTitle" : "ExerciseID";

        String sqlTotal = "SELECT COUNT(*) FROM " + exerciseTable + " WHERE " + idColumn + " = ?";
        int totalExercises;
        try (PreparedStatement stmt = conn.prepareStatement(sqlTotal)) {
            stmt.setInt(1, challengeId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            totalExercises = rs.getInt(1);
        }

        String sqlCompleted;
        if (isCustom) {
            sqlCompleted = "SELECT COUNT(DISTINCT ce.ExerciseTitle) " +
                    "FROM " + exerciseTable + " ce " +
                    "JOIN Exercise e ON ce.ExerciseTitle = e.exerciseTitle " +
                    "JOIN user_progress up ON e.ExerciseID = up.exercise_id " +
                    "WHERE ce." + idColumn + " = ? AND up.user_id = ? AND up.completion_date = ?";
        } else {
            sqlCompleted = "SELECT COUNT(DISTINCT ce.ExerciseID) " +
                    "FROM " + exerciseTable + " ce " +
                    "JOIN user_progress up ON ce.ExerciseID = up.exercise_id " +
                    "WHERE ce." + idColumn + " = ? AND up.user_id = ? AND up.completion_date = ?";
        }
        int completedExercises;
        try (PreparedStatement stmt = conn.prepareStatement(sqlCompleted)) {
            stmt.setInt(1, challengeId);
            stmt.setInt(2, userId);
            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            completedExercises = rs.getInt(1);
        }

        return completedExercises >= totalExercises;
    }

    private void closeStage() {
        Stage stage = (Stage) finishButton.getScene().getWindow();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), stage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> stage.close());
        fadeOut.play();
    }

    @FXML
    public void handleback(ActionEvent event) {
        closeStage();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}