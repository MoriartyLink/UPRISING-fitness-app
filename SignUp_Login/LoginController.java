package project.uprising.SignUp_Login;

import javafx.stage.StageStyle;
import project.uprising.User.Session;
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
import javafx.scene.input.KeyCode; // Add this import for KeyCode

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML private TextField phoneNoField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private TextField phoneField; // Note: This seems unused; verify if needed

    private UserDAO userDAO = new UserDAO();

    // Initialize method to set up Enter key handling
    @FXML
    public void initialize() {
        // Trigger handleLogin when Enter is pressed in phoneNoField
        phoneNoField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // Trigger handleLogin when Enter is pressed in passwordField
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    @FXML
    private void handleLogin() {
        String phone = phoneNoField.getText();
        String password = passwordField.getText();
        User user = userDAO.getUserByPhoneAndPassword(phone, password);
        if (user != null) {
            Session.setCurrentUser(user);
            goToMainDashboard();
        } else {
            errorLabel.setText("Invalid phone number or password");
        }
    }

    private void goToMainDashboard() {
        try {
            URL fxmlLocation = getClass().getResource("/project/uprising/maindashboard.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: 'maindashboard.fxml' not found at '/project/uprising/maindashboard.fxml'");
                System.err.println("Current package path: " + getClass().getResource("").toString());
                return;
            }
            System.out.println("Loading FXML from: " + fxmlLocation.toString());
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Dashboard");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
            Stage loginStage = (Stage) phoneNoField.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            System.err.println("Failed to load maindashboard.fxml:");
            e.printStackTrace();
        }
    }

    @FXML
    public void goToSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) errorLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign Up");
            stage.setX(0);
            stage.setY(0);
            //stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}