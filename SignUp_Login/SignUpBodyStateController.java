package project.uprising.SignUp_Login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import project.uprising.User.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpBodyStateController implements Initializable {
    private User user;

    @FXML private HBox heightInputContainer;
    @FXML private TextField heightFieldCm;
    @FXML private TextField heightFieldFt;
    @FXML private TextField heightFieldIn;
    @FXML private ToggleButton cmButton;
    @FXML private ToggleButton ftButton;
    @FXML private TextField weightField;
    @FXML private ToggleButton kgButton;
    @FXML private ToggleButton lbButton;
    @FXML private Label errorLabel;

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Ensure cm is the default view
        toggleHeightUnit();
    }

    @FXML
    private void toggleHeightUnit() {
        if (cmButton.isSelected()) {
            heightFieldCm.setVisible(true);
            heightFieldCm.setManaged(true);
            heightFieldFt.setVisible(false);
            heightFieldFt.setManaged(false);
            heightFieldIn.setVisible(false);
            heightFieldIn.setManaged(false);
        } else if (ftButton.isSelected()) {
            heightFieldCm.setVisible(false);
            heightFieldCm.setManaged(false);
            heightFieldFt.setVisible(true);
            heightFieldFt.setManaged(true);
            heightFieldIn.setVisible(true);
            heightFieldIn.setManaged(true);
        }
    }

    @FXML
    private void handleNext() {
        String weightText = weightField.getText().trim();

        // Validate weight
        if (weightText.isEmpty()) {
            errorLabel.setText("Please fill in all required fields.");
            return;
        }

        try {
            // Handle height
            double heightCm;
            if (cmButton.isSelected()) {
                String heightCmText = heightFieldCm.getText().trim();
                if (heightCmText.isEmpty()) {
                    errorLabel.setText("Please enter height.");
                    return;
                }
                heightCm = Double.parseDouble(heightCmText);
            } else {
                String ftText = heightFieldFt.getText().trim();
                String inText = heightFieldIn.getText().trim();
                if (ftText.isEmpty() || inText.isEmpty()) {
                    errorLabel.setText("Please enter both feet and inches.");
                    return;
                }
                double feet = Double.parseDouble(ftText);
                double inches = Double.parseDouble(inText);
                heightCm = (feet * 12 + inches) * 2.54; // Convert ft/in to cm
            }
            int heightCmInt = (int) Math.round(heightCm);

            // Handle weight
            double weight = Double.parseDouble(weightText);
            if (lbButton.isSelected()) {
                weight *= 0.453592; // Convert lb to kg
            }

            // Set values in User object
            user.setHeight(heightCmInt);
            user.setWeight(weight);

            // Load next screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/SignUp_goalexp.fxml"));
            Parent root = loader.load();
            SignUpGoalExpController controller = loader.getController();
            controller.setUser(user);
            Stage stage = (Stage) heightFieldCm.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid input. Please enter valid numbers.");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load the next screen.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) heightFieldCm.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to return to previous screen.");
        }
    }
}