package project.uprising;

import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import project.uprising.User.Session;
import project.uprising.User.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import project.uprising.User.Session;
import project.uprising.User.User;



public class ProfileController {
    @FXML private Label usernameLabel, phoneLabel, emailLabel, genderLabel,heightLabel, birthYearLabel, expLabel, levelLabel, goalLabel, workoutDaysLabel;
    @FXML private Button backButton;


    public void initialize() {
        refreshProfileData();
    }

    private void refreshProfileData() {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
            phoneLabel.setText(currentUser.getPhoneNo());
            emailLabel.setText(currentUser.getEmail());
            genderLabel.setText(currentUser.getGender());
            heightLabel.setText(String.valueOf(currentUser.getHeight()));
            birthYearLabel.setText(String.valueOf(currentUser.getDateOfBirth()));
            expLabel.setText("(" + currentUser.getExp() + ")");
            levelLabel.setText("" + currentUser.getLevel());
            // Add these if goal and workout days are displayed in profile.fxml
            goalLabel.setText(currentUser.getGoal());
           // workoutDaysLabel.setText(String.valueOf(currentUser.getWorkoutDaysPerWeek()));
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void handleEditProfileButton(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit_profile.fxml"));
            Parent profilePanel = loader.load();
            Stage editStage = new Stage();
            editStage.setTitle("Edit Profile");
            editStage.initStyle(StageStyle.UNDECORATED);
            editStage.setScene(new Scene(profilePanel, 400, 600));
            editStage.showAndWait(); // Blocks until the edit stage is closed
            refreshProfileData(); // Refresh profile data after edit
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
