package project.uprising.SignUp_Login;

import javafx.scene.paint.Color;
import project.uprising.User.UserDAO;
import project.uprising.User.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.DatePicker;

import java.io.IOException;
import java.time.LocalDate;

public class SignUpController {

    @FXML private TextField usernameField;
    @FXML private TextField phoneNoField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private ToggleGroup genderGroup;
    @FXML private DatePicker birthDatePicker;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Configure date picker
        birthDatePicker.setValue(LocalDate.of(2000, 1, 1));
        birthDatePicker.setPromptText("Age must be at least 15");
        errorLabel.setTextFill(Color.RED);
    }

    @FXML
    public void handleNext() {
        String username = usernameField.getText().trim();
        String phoneNo  = phoneNoField.getText().trim();
        String password = passwordField.getText().trim();
        String email    = emailField.getText().trim();
        String gender = null;
        if (maleRadio.isSelected()) {
            gender = "Male";
        } else if (femaleRadio.isSelected()) {
            gender = "Female";
        }
        LocalDate birthDate = birthDatePicker.getValue();

        // Basic validation
        if (username.isEmpty() || phoneNo.isEmpty() || password.isEmpty() || gender == null || birthDate == null) {
            errorLabel.setText("Please fill in all required fields");
            return;
        }

        // Check if age is at least 15
        int age = LocalDate.now().getYear() - birthDate.getYear();
        if (age < 15) {
            errorLabel.setText("Age must be at least 15");
            return;
        }

        // Check if phone number is already in use
        if (userDAO.phoneExists(phoneNo)) {
            errorLabel.setText("Phone number is already registered.");
            return;
        }

        // Create new user object
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPhoneNo(phoneNo);
        newUser.setPassword(password); // In real app, hash the password
        newUser.setEmail(email);
        newUser.setGender(gender);
        newUser.setDateOfBirth(birthDate); // Set the full date of birth

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/SignUp_bodyState.fxml"));
            Parent root = loader.load();

            SignUpBodyStateController controller = loader.getController();
            controller.setUser(newUser); // Pass user object

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) errorLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
