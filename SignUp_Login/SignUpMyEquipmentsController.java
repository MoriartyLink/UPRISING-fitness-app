package project.uprising.SignUp_Login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import project.uprising.MainDashboardController;
import project.uprising.User.Database;
import project.uprising.User.Session;
import project.uprising.User.User;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SignUpMyEquipmentsController implements Initializable {

    @FXML private CheckBox dumbbellsCheckBox;
    @FXML private ToggleButton dumbbells2_5, dumbbells5, dumbbells7_5, dumbbells10, dumbbells12_5, dumbbells15, dumbbells17_5, dumbbells20;
    @FXML private CheckBox kettlebellsCheckBox;
    @FXML private ToggleButton kettlebells2_5, kettlebells5, kettlebells7_5, kettlebells10, kettlebells12_5, kettlebells15, kettlebells17_5, kettlebells20;
    @FXML private CheckBox barbellsCheckBox;
    @FXML private ToggleButton barbells2_5, barbells5, barbells7_5, barbells10, barbells12_5, barbells15, barbells17_5, barbells20;
    @FXML private CheckBox ezBarCheckBox;
    @FXML private ToggleButton ezBars2_5, ezBar5, ezBar7_5, ezBar10, ezBar12_5, ezBar15, ezBar17_5, ezBar20;
    @FXML private CheckBox resistanceBandsCheckBox;
    @FXML private ToggleButton resistanceXXXLight, resistanceExtraLight, resistanceLight, resistanceMedium,
            resistanceHeavy, resistanceExtraHeavy, resistanceXXHeavy, resistanceXXXHeavy;
    @FXML private CheckBox benchCheckBox;
    @FXML private CheckBox bodyWeightCheckBox;
    @FXML private Button SubmitBtn;
    @FXML private Button resetButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bodyWeightCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                clearAllEquipmentSelections();
            }
        });
    }

    @FXML
    private void handleSubmit() {
        User user = Session.getCurrentUser();
        if (user == null || user.getId() <= 0) {
            showAlert("Error", "Invalid user session.");
            return;
        }

        if (!bodyWeightCheckBox.isSelected() && !isAnyEquipmentSelected()) {
            showAlert("Selection Required", "Please select at least one equipment or the body weight option.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String deleteSql = "DELETE FROM User_Equipment WHERE UserID = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, user.getId());
            int rowsDeleted = deleteStmt.executeUpdate();
            System.out.println("Cleared existing equipment for UserID " + user.getId() + ", Rows deleted: " + rowsDeleted);

            String insertSql = "INSERT INTO User_Equipment (UserID, EquipmentType, Weight) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSql);

            if (bodyWeightCheckBox.isSelected()) {
                saveEquipment(pstmt, user.getId(), "Body Weight", "");
            } else {
                saveSelectedEquipment(pstmt, user.getId());
            }

            System.out.println("Equipment selections saved for UserID " + user.getId());

            navigateToDashboard();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save equipment selections: " + e.getMessage());
        }
    }

    private void saveSelectedEquipment(PreparedStatement pstmt, int userId) throws SQLException {
        if (dumbbellsCheckBox.isSelected()) {
            String weights = getSelectedWeights(dumbbells2_5, dumbbells5, dumbbells7_5, dumbbells10,
                    dumbbells12_5, dumbbells15, dumbbells17_5, dumbbells20);
            if (!weights.equals("N/A")) {
                saveEquipment(pstmt, userId, "Dumbbells", weights);
            }
        }
        if (kettlebellsCheckBox.isSelected()) {
            String weights = getSelectedWeights(kettlebells2_5, kettlebells5, kettlebells7_5, kettlebells10,
                    kettlebells12_5, kettlebells15, kettlebells17_5, kettlebells20);
            if (!weights.equals("N/A")) {
                saveEquipment(pstmt, userId, "Kettlebells", weights);
            }
        }
        if (barbellsCheckBox.isSelected()) {
            String weights = getSelectedWeights(barbells2_5, barbells5, barbells7_5, barbells10,
                    barbells12_5, barbells15, barbells17_5, barbells20);
            if (!weights.equals("N/A")) {
                saveEquipment(pstmt, userId, "Barbells", weights);
            }
        }
        if (ezBarCheckBox.isSelected()) {
            String weights = getSelectedWeights(ezBars2_5, ezBar5, ezBar7_5, ezBar10,
                    ezBar12_5, ezBar15, ezBar17_5, ezBar20);
            if (!weights.equals("N/A")) {
                saveEquipment(pstmt, userId, "EZ Bar", weights);
            }
        }
        if (resistanceBandsCheckBox.isSelected()) {
            String weights = getSelectedWeights(resistanceXXXLight, resistanceExtraLight, resistanceLight,
                    resistanceMedium, resistanceHeavy, resistanceExtraHeavy, resistanceXXHeavy, resistanceXXXHeavy);
            if (!weights.equals("N/A")) {
                saveEquipment(pstmt, userId, "Resistance Bands", weights);
            }
        }
        if (benchCheckBox.isSelected()) {
            saveEquipment(pstmt, userId, "Bench", "N/A");
        }
    }

    private void saveEquipment(PreparedStatement pstmt, int userId, String type, String weight) throws SQLException {
        pstmt.setInt(1, userId);
        pstmt.setString(2, type);
        pstmt.setString(3, weight);
        pstmt.executeUpdate();
        System.out.println("Saved equipment: UserID=" + userId + ", Type=" + type + ", Weight=" + weight);
    }

    private String getSelectedWeights(ToggleButton... buttons) {
        StringBuilder weights = new StringBuilder();
        boolean first = true;
        for (ToggleButton button : buttons) {
            if (button != null && button.isSelected()) {
                if (!first) {
                    weights.append(", ");
                }
                weights.append(button.getText()).append(" Kg");
                first = false;
            }
        }
        return weights.length() > 0 ? weights.toString() : "N/A";
    }

    private boolean isAnyEquipmentSelected() {
        return dumbbellsCheckBox.isSelected() || kettlebellsCheckBox.isSelected() ||
                barbellsCheckBox.isSelected() || ezBarCheckBox.isSelected() ||
                resistanceBandsCheckBox.isSelected() || benchCheckBox.isSelected();
    }

    @FXML
    private void handleReset() {
        clearAllEquipmentSelections();
        bodyWeightCheckBox.setSelected(false);
    }

    private void clearAllEquipmentSelections() {
        dumbbellsCheckBox.setSelected(false);
        kettlebellsCheckBox.setSelected(false);
        barbellsCheckBox.setSelected(false);
        ezBarCheckBox.setSelected(false);
        resistanceBandsCheckBox.setSelected(false);
        benchCheckBox.setSelected(false);

        deselectToggleButtons(dumbbells2_5, dumbbells5, dumbbells7_5, dumbbells10,
                dumbbells12_5, dumbbells15, dumbbells17_5, dumbbells20);
        deselectToggleButtons(kettlebells2_5, kettlebells5, kettlebells7_5, kettlebells10,
                kettlebells12_5, kettlebells15, kettlebells17_5, kettlebells20);
        deselectToggleButtons(barbells2_5, barbells5, barbells7_5, barbells10,
                barbells12_5, barbells15, barbells17_5, barbells20);
        deselectToggleButtons(ezBars2_5, ezBar5, ezBar7_5, ezBar10,
                ezBar12_5, ezBar15, ezBar17_5, ezBar20);
        deselectToggleButtons(resistanceXXXLight, resistanceExtraLight, resistanceLight, resistanceMedium,
                resistanceHeavy, resistanceExtraHeavy, resistanceXXHeavy, resistanceXXXHeavy);
    }

    private void deselectToggleButtons(ToggleButton... buttons) {
        for (ToggleButton button : buttons) {
            if (button != null) {
                button.setSelected(false);
            }
        }
    }

    private void navigateToDashboard() {
        try {
            System.out.println("Navigating to maindashboard.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/maindashboard.fxml"));
            Parent root = loader.load();
            MainDashboardController controller = loader.getController();
            controller.setCurrentUser(Session.getCurrentUser());
            Stage stage = (Stage) SubmitBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Dashboard");
            stage.setX(0);
            stage.setY(0);
            stage.show();
            System.out.println("Successfully navigated to Main Dashboard for UserID: " + Session.getCurrentUser().getId());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load the main dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}