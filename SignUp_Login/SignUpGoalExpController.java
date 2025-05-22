package project.uprising.SignUp_Login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.Label;
import javafx.stage.Stage;
import project.uprising.MainDashboardController;
import project.uprising.User.Session;
import project.uprising.User.User;
import project.uprising.User.UserDAO;

public class SignUpGoalExpController implements Initializable {

    @FXML private ComboBox<String> goalComboBox;
    @FXML private ComboBox<String> experienceComboBox;
    @FXML private ComboBox<String> daysPerWeekComboBox;
    @FXML private Label errorLabel;
    private User user;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/SignUp_bodyState.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) goalComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp() {
        if (goalComboBox.getValue() == null || experienceComboBox.getValue() == null || daysPerWeekComboBox.getValue() == null) {
            errorLabel.setText("Please select your goal, experience level, and days per week.");
            return;
        }

        String experience = experienceComboBox.getValue();
        user.setGoal(goalComboBox.getValue());
        user.setExperience(experience);

        String daysSelection = daysPerWeekComboBox.getValue();
        int daysValue = daysSelection.startsWith("Everyday") ? 7 :
                Integer.parseInt(String.valueOf(daysSelection.charAt(0) - '0'));
        user.setWorkoutDaysPerWeek(daysValue);

        System.out.println("Selected experience: " + experience);

        UserDAO userDAO = new UserDAO();
        if (userDAO.phoneExists(user.getPhoneNo())) {
            errorLabel.setText("This phone number is already registered. Please log in instead.");
            return;
        }

        if (userDAO.createUser(user)) {
            int initialExp = switch (experience) {
                case "Intermediate (1-3 years)" -> 2000;
                case "Advanced (3+ years)" -> 6000;
                default -> 0;
            };
            user.setExp(initialExp); // Sync EXP locally
            Session.setCurrentUser(user);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/Signup_myEquipments.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) errorLabel.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Select Your Equipment");
                stage.setX(0);
                stage.setY(0);
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Error loading equipment screen: " + e.getMessage());
            }
        } else {
            errorLabel.setText("Sign up failed. Check your details and try again.");
        }
    }

}
