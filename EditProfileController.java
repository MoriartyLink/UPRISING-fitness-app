package project.uprising;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import project.uprising.User.Session;
import project.uprising.User.User;
import project.uprising.User.UserDAO;

import java.time.LocalDate;

public class EditProfileController {

    @FXML private TextField usernameField, phoneField, emailField;
    @FXML private RadioButton maleRadio, femaleRadio, otherRadio;
    @FXML private ToggleGroup genderGroup;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField heightFieldCm, heightFieldFt, heightFieldIn;
    @FXML private ToggleButton cmButton, ftButton;
    @FXML private ToggleGroup heightUnitGroup;
    @FXML private TextField weightField;
    @FXML private ToggleButton kgButton, lbButton;
    @FXML private ToggleGroup weightUnitGroup;
    @FXML private ComboBox<String> goalComboBox;
    @FXML private ComboBox<String> daysPerWeekComboBox;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            phoneField.setText(currentUser.getPhoneNo());
            emailField.setText(currentUser.getEmail());
            switch (currentUser.getGender()) {
                case "Male": maleRadio.setSelected(true); break;
                case "Female": femaleRadio.setSelected(true); break;
                default: otherRadio.setSelected(true); break;
            }
            birthDatePicker.setValue(currentUser.getDateOfBirth());
            heightFieldCm.setText(String.valueOf(currentUser.getHeight()));
            weightField.setText(String.valueOf(currentUser.getWeight()));
            goalComboBox.setValue(currentUser.getGoal());
            int workoutDays = currentUser.getWorkoutDaysPerWeek();
            daysPerWeekComboBox.setValue(workoutDays == 7 ? "Everyday" : String.valueOf(workoutDays));
        }

        // Height unit toggle listener
        heightUnitGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == cmButton && oldToggle == ftButton) {
                try {
                    double ft = Double.parseDouble(heightFieldFt.getText());
                    double in = Double.parseDouble(heightFieldIn.getText());
                    double cm = (ft * 30.48) + (in * 2.54);
                    heightFieldCm.setText(String.format("%.2f", cm));
                } catch (NumberFormatException e) {
                    errorLabel.setText("Invalid height input");
                }
                updateHeightFieldsVisibility(true);
            } else if (newToggle == ftButton && oldToggle == cmButton) {
                try {
                    double cm = Double.parseDouble(heightFieldCm.getText());
                    double totalInches = cm / 2.54;
                    int ft = (int) (totalInches / 12);
                    double in = totalInches % 12;
                    heightFieldFt.setText(String.valueOf(ft));
                    heightFieldIn.setText(String.format("%.2f", in));
                } catch (NumberFormatException e) {
                    errorLabel.setText("Invalid height input");
                }
                updateHeightFieldsVisibility(false);
            }
        });

        // Weight unit toggle listener
        weightUnitGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == kgButton && oldToggle == lbButton) {
                try {
                    double lb = Double.parseDouble(weightField.getText());
                    double kg = lb * 0.453592;
                    weightField.setText(String.format("%.2f", kg));
                    weightField.setPromptText("kg");
                } catch (NumberFormatException e) {
                    errorLabel.setText("Invalid weight input");
                }
            } else if (newToggle == lbButton && oldToggle == kgButton) {
                try {
                    double kg = Double.parseDouble(weightField.getText());
                    double lb = kg / 0.453592;
                    weightField.setText(String.format("%.2f", lb));
                    weightField.setPromptText("lb");
                } catch (NumberFormatException e) {
                    errorLabel.setText("Invalid weight input");
                }
            }
        });
    }

    private void updateHeightFieldsVisibility(boolean cmVisible) {
        heightFieldCm.setVisible(cmVisible);
        heightFieldCm.setManaged(cmVisible);
        heightFieldFt.setVisible(!cmVisible);
        heightFieldFt.setManaged(!cmVisible);
        heightFieldIn.setVisible(!cmVisible);
        heightFieldIn.setManaged(!cmVisible);
    }

    @FXML
    private void handleSave() {
        try {
            String username = usernameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String gender = ((RadioButton) genderGroup.getSelectedToggle()).getText();
            LocalDate birthDate = birthDatePicker.getValue();

            if (username.isEmpty() || phone.isEmpty() || gender.isEmpty() || birthDate == null) {
                errorLabel.setText("Please fill all required fields");
                return;
            }

            if (LocalDate.now().getYear() - birthDate.getYear() < 15) {
                errorLabel.setText("Age must be at least 15");
                return;
            }

            double height = cmButton.isSelected() ?
                    Double.parseDouble(heightFieldCm.getText()) :
                    (Double.parseDouble(heightFieldFt.getText()) * 30.48) + (Double.parseDouble(heightFieldIn.getText()) * 2.54);

            double weight = kgButton.isSelected() ?
                    Double.parseDouble(weightField.getText()) :
                    Double.parseDouble(weightField.getText()) * 0.453592;

            String goal = goalComboBox.getValue();
            String daysStr = daysPerWeekComboBox.getValue();
            int daysPerWeek = "Everyday".equals(daysStr) ? 7 : Integer.parseInt(daysStr);

            User user = Session.getCurrentUser();
            user.setUsername(username);
            user.setPhoneNo(phone);
            user.setEmail(email);
            user.setGender(gender);
            user.setDateOfBirth(birthDate);
            user.setHeight((int) height);
            user.setWeight(weight);
            user.setGoal(goal);
            user.setWorkoutDaysPerWeek(daysPerWeek);

            if (userDAO.updateUserProfile(user)) {
                Session.setCurrentUser(user);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.close();
            } else {
                errorLabel.setText("Failed to update profile");
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid numeric input");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    // Add this method to UserDAO class or create a new one
    public boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET username = ?, phone_no = ?, email = ?, gender = ?, date_of_birth = ?, height = ?, weight = ?, goal = ?, workoutdaysperweek = ? WHERE UserID = ?";
        try (java.sql.Connection conn = project.uprising.User.Database.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPhoneNo());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getGender());
            stmt.setDate(5, java.sql.Date.valueOf(user.getDateOfBirth()));
            stmt.setInt(6, user.getHeight());
            stmt.setDouble(7, user.getWeight());
            stmt.setString(8, user.getGoal());
            stmt.setInt(9, user.getWorkoutDaysPerWeek());
            stmt.setInt(10, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}