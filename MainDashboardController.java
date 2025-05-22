package project.uprising;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.Pair;
import project.uprising.Challenge.Challenge;
import project.uprising.Challenge.ChallengeController;
import project.uprising.Challenge.ChallengeDAO;
import project.uprising.Challenge.CustomizedChallenge;
import project.uprising.User.Database;
import project.uprising.User.Session;
import project.uprising.User.User;
import project.uprising.User.UserDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainDashboardController implements Initializable {

    @FXML private VBox rootVBox;
    @FXML private VBox navBar;
    @FXML private VBox mainContent;
    @FXML private Label welcomeLabel;
    @FXML private Button exitbtn;
    @FXML private HBox challengeHBox;
    @FXML private ScrollPane challengeScroll;
    @FXML private BorderPane contentPane;
    @FXML private Pane pane;
    @FXML private Label knowledgeLabel;
    @FXML private TextField weightField;
    @FXML private Label bmiResult;
    @FXML private CheckBox dumbbellsCheckBox;
    @FXML private ToggleButton dumbbells2_5, dumbbells5, dumbbells7_5, dumbbells10, dumbbells12_5, dumbbells15, dumbbells17_5, dumbbells20;
    @FXML private CheckBox kettlebellsCheckBox;
    @FXML private ToggleButton kettlebells2_5, kettlebells5, kettlebells7_5, kettlebells10, kettlebells12_5, kettlebells15, kettlebells17_5, kettlebells20;
    @FXML private CheckBox barbellsCheckBox;
    @FXML private CheckBox ezBarCheckBox;
    @FXML private CheckBox resistanceBandsCheckBox;
    @FXML private ToggleButton resistanceXXXLight, resistanceExtraLight, resistanceLight, benchCheckBox;
    @FXML private CheckBox resistanceMedium;
    @FXML private CheckBox bodyWeightCheckBox;
    @FXML private Button saveButton;
    @FXML private Circle bmiProgressCircle;
    @FXML private Label bmiValueLabel;
    @FXML private ToggleButton removeBtn ;
    private Timeline bmiAnimation;

    private UserDAO userDAO;
    private static final double BMI_MIN = 18.5;
    private static final double BMI_MAX = 24.9;
    private static final double ANIMATION_DURATION_SECONDS = 1.0;
    private static final double DEFAULT_RADIUS = 46.0;

    private User currentUser;
    private boolean isNavBarOpen = false;
    private TranslateTransition navBarTransition;
    private TranslateTransition knowledgeTransition;
    private static final int MAX_DAILY_ENTRIES = 3;
    private String[] advice = {
            "Hydrate daily for optimal performance.",
            "Consistency drives fitness success.",
            "Stretch to enhance flexibility.",
            "Prioritize form to prevent injuries.",
            "Nutrition fuels your workouts."
    };
    private int currentIndex = 0;
    private double labelWidth;
    private double paneWidth;
    private String currentUsername;

    private static final double NAVBAR_CLOSED_X = -1200.0;
    private static final double NAVBAR_OPEN_X = -620.0;
    private static final Duration ANIMATION_DURATION = Duration.millis(700);

    private double dashboardWidth = 3000.0;
    @FXML private Label levelLabel;

    private ProgressController progressController = new ProgressController();

    public MainDashboardController() {
        this.userDAO = new UserDAO();
        this.bmiAnimation = new Timeline();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUsername = Session.getCurrentUsername();
        updateWelcomeMessage();

        knowledgeLabel.setVisible(true);
        knowledgeLabel.setManaged(true); // Ensure it participates in layout

        // Set up the advice cycling timeline
        Timeline adviceTimeline = new Timeline(new KeyFrame(Duration.seconds(20), event -> {
            knowledgeLabel.setText(advice[currentIndex]);
            currentIndex = (currentIndex + 1) % advice.length;
            resetKnowledgeAnimation();
        }));
        adviceTimeline.setCycleCount(Timeline.INDEFINITE);
        adviceTimeline.play();
        setupTransitions();

        if (challengeHBox == null) System.out.println("challengeHBox is null");
        challengeHBox.setPrefWidth(2000);

        Platform.runLater(() -> {
            paneWidth = pane.getPrefWidth();
            labelWidth = knowledgeLabel.getBoundsInLocal().getWidth();
            startKnowledgeAnimation();
            updateLabelWidth();
            knowledgeLabel.setTranslateX(-labelWidth - 700); // Start off-screen left
            System.out.println("Initial setup: paneWidth=" + paneWidth + ", labelWidth=" + labelWidth + ", translateX=" + knowledgeLabel.getTranslateX());
            startKnowledgeAnimation();
            populateChallenges();

            initializeEquipment();
            setupNavbarAutoClose();
            loadUserBMIData();
        });

        if (removeBtn != null) {
            removeBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println("Remove mode " + (newVal ? "enabled" : "disabled"));
            });
        }

        if (bmiProgressCircle == null || bmiValueLabel == null) {
            logError("UI elements not initialized properly: bmiProgressCircle or bmiValueLabel is null", null);
        } else {
            loadUserBMIData();
        }

        exitbtn.setOnAction(event -> Platform.exit());

        FadeTransition fadeInCenter = new FadeTransition(Duration.millis(500));
        fadeInCenter.setFromValue(0);
        fadeInCenter.setToValue(1);
        fadeInCenter.play();
    }

    private void setupTransitions() {
        navBarTransition = new TranslateTransition(ANIMATION_DURATION, navBar);
        navBarTransition.setFromX(NAVBAR_CLOSED_X);
        navBarTransition.setToX(NAVBAR_OPEN_X);
        navBar.setTranslateX(NAVBAR_CLOSED_X);
    }

    private void setupNavbarAutoClose() {
        mainContent.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (isNavBarOpen) {
                closeNavBar();
            }
        });

        navBar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                ((Button) node).setOnAction(event -> {
                    handleNavButtonAction((ActionEvent) event);
                    closeNavBar();
                });
            }
        });
    }

    @FXML
    private void toggleNavBar() {
        if (isNavBarOpen) {
            closeNavBar();
        } else {
            openNavBar();
        }
    }

    private void openNavBar() {
        navBarTransition.setFromX(NAVBAR_CLOSED_X);
        navBarTransition.setToX(NAVBAR_OPEN_X);
        navBarTransition.play();
        isNavBarOpen = true;
    }

    private void closeNavBar() {
        navBarTransition.setFromX(NAVBAR_OPEN_X);
        navBarTransition.setToX(NAVBAR_CLOSED_X);
        navBarTransition.play();
        isNavBarOpen = false;
    }

    private void handleNavButtonAction(ActionEvent event) {
        Button source = (Button) event.getSource();
        String buttonText = source.getText();
        switch (buttonText) {
            case "Body":
                handleBodyButton();
                break;
            case "Progress":
                handleProgressButton();
                break;
            case "Program":
                handleProgramButton();
                break;
            default:
                break;
        }
    }

    private void initializeEquipment() {
        if (dumbbellsCheckBox != null) dumbbellsCheckBox.setSelected(false);
        if (kettlebellsCheckBox != null) kettlebellsCheckBox.setSelected(false);
        if (barbellsCheckBox != null) barbellsCheckBox.setSelected(false);
        if (ezBarCheckBox != null) ezBarCheckBox.setSelected(false);
        if (resistanceBandsCheckBox != null) resistanceBandsCheckBox.setSelected(false);
        if (benchCheckBox != null) benchCheckBox.setSelected(false);
        if (bodyWeightCheckBox != null) bodyWeightCheckBox.setSelected(false);
    }
    private void populateChallenges() {
        ChallengeDAO challengeDAO = new ChallengeDAO();
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No current user logged in.");
            challengeHBox.getChildren().clear();
            Label noUserLabel = new Label("Please log in to see challenges.");
            noUserLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            challengeHBox.getChildren().add(noUserLabel);
            return;
        }

        challengeHBox.getChildren().clear();
        challengeHBox.setSpacing(5);
        challengeHBox.setAlignment(Pos.CENTER_LEFT);

        try {
            List<Challenge> filteredChallenges = challengeDAO.getFilteredChallenges(currentUser.getId());
            List<CustomizedChallenge> customChallenges = challengeDAO.getCustomizedChallengesForUser(currentUser.getId());

            for (Challenge challenge : filteredChallenges) {
                VBox challengeCard = createChallengeCard(challenge.getChallengeTitle(), challenge.getChallengeId(), false);
                challengeHBox.getChildren().add(challengeCard);
                HBox.setMargin(challengeCard, new Insets(0, 0, 0, 10));
            }

            for (CustomizedChallenge customChallenge : customChallenges) {
                VBox challengeCard = createChallengeCard(customChallenge.getChallengeTitle(), customChallenge.getChallengeId(), true);
                challengeHBox.getChildren().add(challengeCard);
                HBox.setMargin(challengeCard, new Insets(0, 0, 0, 10));
            }

            if (filteredChallenges.isEmpty() && customChallenges.isEmpty()) {
                Label noChallengesLabel = new Label("No challenges match your profile yet!");
                noChallengesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                challengeHBox.getChildren().add(noChallengesLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading challenges.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            challengeHBox.getChildren().add(errorLabel);
        }
    }
    private VBox createChallengeCard(String title, int id, boolean isCustom) {
        VBox card = new VBox(50);
        card.setPrefSize(200, 50);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(12,17,21,0.57); -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 6); -fx-padding: 12;");

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(34,67,120,0.81); -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 6); -fx-padding: 12;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: rgba(12,17,21,0.57); -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 6); -fx-padding: 12;"));
//77777777
        Label label = new Label(title);
        label.setTextFill(Color.WHITE);
        label.setFont(new javafx.scene.text.Font("Arial Bold", 13));
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setMaxWidth(230);

        Button joinButton = new Button("Join");
        joinButton.setPrefSize(120, 35);
        joinButton.setStyle("-fx-background-color: #6666FF; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial Bold';");
        joinButton.setOnMouseEntered(e -> joinButton.setStyle("-fx-background-color: #8888FF; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial Bold';"));
        joinButton.setOnMouseExited(e -> joinButton.setStyle("-fx-background-color: #6666FF; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial Bold';"));

        card.getChildren().addAll(label, joinButton);

        // Handle click based on removeBtn state
        card.setOnMouseClicked(e -> {
            if (removeBtn != null && removeBtn.isSelected() && isCustom) {
                // Remove mode is active and this is a custom challenge
                removeCustomizedChallenge(id);
                populateChallenges(); // Refresh the challenge list
                removeBtn.setSelected(false); // Reset remove mode
            } else if (!removeBtn.isSelected()) {
                // Normal mode: join the challenge
                try {
                    handleJoinChallenge(id, isCustom);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        joinButton.setOnAction(e -> {
            try {
                handleJoinChallenge(id, isCustom);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        return card;
    }

    // New method to remove a customized challenge
    private void removeCustomizedChallenge(int customChallengeId) {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please log in to remove a challenge.");
            return;
        }

        ChallengeDAO challengeDAO = new ChallengeDAO();
        try {
            // Verify the challenge belongs to the user
            CustomizedChallenge challenge = challengeDAO.getCustomizedChallengeById(customChallengeId);
            if (challenge == null || challenge.getUserId() != currentUser.getId()) {
                showAlert(Alert.AlertType.ERROR, "Error", "You can only remove your own customized challenges.");
                return;
            }

            // Delete from CustomizedChallenge_Exercise first (due to foreign key constraint)
            String deleteExercisesSQL = "DELETE FROM CustomizedChallenge_Exercise WHERE CustomChallengeID = ?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(deleteExercisesSQL)) {
                stmt.setInt(1, customChallengeId);
                stmt.executeUpdate();
            }

            // Then delete from CustomizedChallenge
            String deleteChallengeSQL = "DELETE FROM CustomizedChallenge WHERE CustomChallengeID = ? AND UserID = ?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(deleteChallengeSQL)) {
                stmt.setInt(1, customChallengeId);
                stmt.setInt(2, currentUser.getId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Customized challenge removed successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove the challenge.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to remove challenge: " + e.getMessage());
        }
    }

    @FXML
    private void handleCustomizeChallengeButton() {
        try {
            URL fxmlLocation = getClass().getResource("/project/uprising/customize_challenge.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: customize_challenge.fxml not found in resources!");
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot find customize_challenge.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Customize Your Challenge");
            stage.setScene(new Scene(root, 800, 800));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.showAndWait(); // Use showAndWait to block until closed
            populateChallenges(); // Refresh challenges after closing
        } catch (IOException e) {
            System.err.println("Failed to load customize_challenge.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Customize Challenge: " + e.getMessage());
        }
    }
    private void updateWelcomeMessage() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + (currentUsername != null ? currentUsername : "Guest"));
        }
    }

    @FXML
    private void handleProfileButton() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Profile");
            stage.setScene(new Scene(root, 700, 600));


            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlemyEquipmentsButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("myEquipments.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("My Equipments");
            stage.setScene(new Scene(root, 1550, 900));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditProfileButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit_profile.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root, 400, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleJoinChallenge(int id, boolean isCustom) throws IOException {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please log in to join a challenge.");
            return;
        }

        // Record challenge entry
        recordChallengeEntry(currentUser.getId(), id, isCustom);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("joinChallenge.fxml"));
        Parent root = loader.load();
        ChallengeController controller = loader.getController();

        if (isCustom) {
            controller.setCustomChallengeId(id);
        } else {
            controller.setChallengeId(id);
        }

        Stage stage = new Stage();
        stage.setTitle(isCustom ? "Custom Challenge" : "Challenge");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(root, 1550, 850));
        stage.show();
    }

    private void recordChallengeEntry(int userId, int challengeId, boolean isCustom) {
        String sqlCheck = "SELECT COUNT(*) FROM User_challenges WHERE user_id = ? AND challenge_id = ?";
        String sqlInsert = "INSERT INTO User_challenges (user_id, challenge_id, start_date) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE start_date = VALUES(start_date)";

        try (Connection conn = Database.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(sqlCheck);
             PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
            // Check if entry already exists
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, challengeId);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count == 0) {
                // Insert new entry if it doesn’t exist
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, challengeId);
                insertStmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                insertStmt.executeUpdate();
                System.out.println("Challenge entry recorded for user " + userId + " and " +
                        (isCustom ? "custom " : "") + "challenge " + challengeId);
            } else {
                System.out.println("User " + userId + " already joined challenge " + challengeId);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to record challenge entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void handleBodyButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("workout.fxml"));
            Parent root = loader.load();
            WorkoutController workoutController = loader.getController();
            workoutController.setCurrentUser(Session.getCurrentUser()); // Pass the current user
            contentPane.getChildren().clear();
            contentPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProgressButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Progress.fxml"));
            Parent root = loader.load();
            contentPane.getChildren().clear();
            contentPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProgramButton() {
        try {
            System.out.println("Entering Program");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Program.fxml"));
            System.out.println("IN Program");
            Parent root = loader.load();
            contentPane.getChildren().clear();
            contentPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            this.currentUsername = user.getUsername();
            updateWelcomeMessage();
        }
        if (user == null && bmiResult != null) {
            bmiResult.setText("Please log in first");
        }
    }

    @FXML
    private void HandleCheckBMIButton() {
        try {
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                showErrorAlert("No user logged in. Please log in to check your BMI.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/check_BMI.fxml"));
            if (loader.getLocation() == null) {
                logError("check_BMI.fxml resource not found", null);
                throw new IOException("check_BMI.fxml not found");
            }

            Parent root = loader.load();
            CheckBMIController controller = loader.getController();
            if (controller == null) {
                logError("CheckBMIController not initialized", null);
                throw new IllegalStateException("Controller not set for check_BMI.fxml");
            }

            controller.setCurrentUser(currentUser);
            controller.setBMICallback(data -> {
                double weightInKg = data.getKey();
                double bmi = data.getValue();
                System.out.println("Callback received - Weight (kg): " + weightInKg + ", BMI: " + bmi);
                Platform.runLater(() -> {
                    if (currentUser != null) {
                        currentUser.setWeight(weightInKg); // Update user weight in kg
                        updateBMIDisplay(bmi);
                    } else {
                        logError("Current user is null in callback", null);
                    }
                });
            });

            Stage stage = new Stage();
            stage.setTitle("Daily BMI Check");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Unable to open BMI check window: " + e.getMessage());
            logError("Failed to load check_BMI.fxml", e);
        } catch (Exception e) {
            showErrorAlert("An unexpected error occurred: " + e.getMessage());
            logError("Unexpected error in HandleCheckBMIButton", e);
        }
    }

    private void loadUserBMIData() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No current user; setting BMI to 0.0");
            updateBMIDisplay(0.0);
            return;
        }

        try {
            double latestBMI = fetchLatestBMI(currentUser.getId());
            System.out.println("Loaded latest BMI for user " + currentUser.getUsername() + ": " + latestBMI);
            updateBMIDisplay(latestBMI);
        } catch (SQLException e) {
            logError("Failed to load BMI data", e);
            updateBMIDisplay(0.0);
        }
    }

    private double fetchLatestBMI(int userId) throws SQLException {
        String sql = "SELECT bmi FROM bmi_records WHERE user_id = ? " +
                "AND record_date = (SELECT MAX(record_date) FROM bmi_records WHERE user_id = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double bmi = rs.getDouble("bmi");
                    System.out.println("Fetched BMI from database: " + bmi);
                    return bmi;
                }
            }
        }
        User user = Session.getCurrentUser();
        double calculatedBMI = calculateBMI(user.getWeight(), user.getHeight());
        System.out.println("No record found; calculated BMI from user data: " + calculatedBMI);
        return calculatedBMI;
    }

    private double calculateBMI(double weight, int height) {
        double heightInMeters = height / 100.0;
        if (heightInMeters <= 0) {
            System.out.println("Invalid height (" + height + "); returning 0.0");
            return 0.0;
        }
        double bmi = weight / (heightInMeters * heightInMeters);
        System.out.println("Calculated BMI: " + bmi + " (weight=" + weight + ", height=" + heightInMeters + ")");
        return bmi;
    }

    private void updateBMIDisplay(double bmi) {
        if (bmiValueLabel != null) {
            String newText = String.format("%.1f", bmi);
            bmiValueLabel.setText(newText);
            bmiValueLabel.setVisible(true);
            System.out.println("Updated bmiValueLabel to: " + newText + ", Visible: " + bmiValueLabel.isVisible());
        } else {
            logError("bmiValueLabel is null; cannot update BMI display", null);
        }
        if (bmiProgressCircle != null) {
            Color bmiColor = getBMIColor(bmi);
            bmiProgressCircle.setStroke(bmiColor);
            Glow glow = new Glow(0.8);
            bmiProgressCircle.setEffect(glow);
            bmiProgressCircle.setVisible(true);
            System.out.println("Updated bmiProgressCircle stroke to: " + bmiColor + ", Visible: " + bmiProgressCircle.isVisible());
            bmiProgressCircle.getParent().requestLayout();
        } else {
            logError("bmiProgressCircle is null; cannot update circle", null);
        }
    }

    private Color getBMIColor(double bmi) {
        if (bmi == 0.0) return Color.web("#708090");         // Slate gray
        if (bmi < 18.5) return Color.web("#87CEEB");         // Pale blue
        if (bmi >= 18.5 && bmi <= 24.9) return Color.web("#66BBA8"); // Mint green
        if (bmi >= 25 && bmi <= 29.9) return Color.web("#FF9999");   // Soft coral
        return Color.web("#800080");                         // Dark purple
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void logError(String message, Exception e) {
        System.err.println(message + (e != null ? ": " + e.getMessage() : ""));
        if (e != null) e.printStackTrace();
    }

    private void updateLabelWidth() {
        labelWidth = knowledgeLabel.getBoundsInLocal().getWidth();
        // System.out.println("Updated labelWidth=" + labelWidth);
    }

    private void startKnowledgeAnimation() {
        if (knowledgeTransition == null) {
            knowledgeTransition = new TranslateTransition(Duration.seconds(10), knowledgeLabel);
            knowledgeTransition.setCycleCount(TranslateTransition.INDEFINITE);
            knowledgeTransition.setAutoReverse(false);
            knowledgeTransition.setInterpolator(Interpolator.LINEAR);
        }

        updateLabelWidth();
        double fromX = -labelWidth;
        double toX = dashboardWidth;
        knowledgeTransition.setFromX(fromX);
        knowledgeTransition.setToX(toX);
        // System.out.println("Starting animation: fromX=" + fromX + ", toX=" + toX + ", labelWidth=" + labelWidth + ", visible=" + knowledgeLabel.isVisible());
        knowledgeTransition.play();
    }

    private void resetKnowledgeAnimation() {
        if (knowledgeTransition != null) {
            knowledgeTransition.stop();
            updateLabelWidth();
            knowledgeLabel.setTranslateX(-labelWidth);
            double fromX = -labelWidth;
            double toX = dashboardWidth;
            knowledgeTransition.setFromX(fromX);
            knowledgeTransition.setToX(toX);
            // System.out.println("Reset animation: translateX=" + knowledgeLabel.getTranslateX() + ", fromX=" + fromX + ", toX=" + toX);
            knowledgeTransition.play();
        }
    }

    private void populateRecentChallenges() {
        String sql = "SELECT c.ChallengeTitle FROM user_progress up " +
                "JOIN Challenge c ON up.challenge_id = c.ChallengeID " +
                "WHERE up.user_id = ? AND up.activity_type = 'challenge' " +
                "ORDER BY up.completion_date DESC LIMIT 5";
        // Populate UI with results
    }

}