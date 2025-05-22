package project.uprising;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import project.uprising.User.Database;
import project.uprising.User.Session;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MyEquipmentsController implements Initializable {

    @FXML private CheckBox dumbbellsCheckBox;
    @FXML private ToggleButton dumbbells2_5, dumbbells5, dumbbells7_5, dumbbells10, dumbbells12_5, dumbbells15, dumbbells17_5, dumbbells20;
    @FXML private CheckBox kettlebellsCheckBox;
    @FXML private ToggleButton kettlebells2_5, kettlebells5, kettlebells7_5, kettlebells10, kettlebells12_5, kettlebells15, kettlebells17_5, kettlebells20;
    @FXML private CheckBox barbellsCheckBox;
    @FXML private ToggleButton barbells2_5, barbells5, barbells7_5, barbells10, barbells12_5, barbells15, barbells17_5, barbells20;
    @FXML private CheckBox ezBarCheckBox;
    @FXML private ToggleButton ezBar2_5, ezBar5, ezBar7_5, ezBar10, ezBar12_5, ezBar15, ezBar17_5, ezBar20;
    @FXML private CheckBox resistanceBandsCheckBox;
    @FXML private ToggleButton resistanceXXXLight, resistanceExtraLight, resistanceLight, resistanceMedium,
            resistanceHeavy, resistanceExtraHeavy, resistanceXXHeavy, resistanceXXXHeavy;
    @FXML private CheckBox benchCheckBox;
    @FXML private CheckBox bodyWeightCheckBox;
    @FXML private Button signupButton;
    @FXML private Button backButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSavedEquipment();

        bodyWeightCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                clearAllEquipmentSelections();
            }
        });

        signupButton.setOnAction(event -> handleSave()); // Bind signupButton to handleSave
        backButton.setOnAction(event -> handleBack());   // Bind backButton to handleBack
    }

    private void loadSavedEquipment() {
        int userId = Session.getCurrentUser().getId();

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT EquipmentType, Weight FROM User_Equipment WHERE UserID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String equipmentType = rs.getString("EquipmentType");
                String weight = rs.getString("Weight");

                switch (equipmentType) {
                    case "Dumbbells":
                        dumbbellsCheckBox.setSelected(true);
                        setToggleButtonSelected(weight, dumbbells2_5, dumbbells5, dumbbells7_5, dumbbells10,
                                dumbbells12_5, dumbbells15, dumbbells17_5, dumbbells20);
                        break;
                    case "Kettlebells":
                        kettlebellsCheckBox.setSelected(true);
                        setToggleButtonSelected(weight, kettlebells2_5, kettlebells5, kettlebells7_5, kettlebells10,
                                kettlebells12_5, kettlebells15, kettlebells17_5, kettlebells20);
                        break;
                    case "Barbells":
                        barbellsCheckBox.setSelected(true);
                        setToggleButtonSelected(weight, barbells2_5, barbells5, barbells7_5, barbells10,
                                barbells12_5, barbells15, barbells17_5, barbells20);
                        break;
                    case "EZ Bar":
                        ezBarCheckBox.setSelected(true);
                        setToggleButtonSelected(weight, ezBar2_5, ezBar5, ezBar7_5, ezBar10,
                                ezBar12_5, ezBar15, ezBar17_5, ezBar20);
                        break;
                    case "Resistance Bands":
                        resistanceBandsCheckBox.setSelected(true);
                        setToggleButtonSelected(weight, resistanceXXXLight, resistanceExtraLight, resistanceLight,
                                resistanceMedium, resistanceHeavy, resistanceExtraHeavy, resistanceXXHeavy, resistanceXXXHeavy);
                        break;
                    case "Bench":
                        benchCheckBox.setSelected(true);
                        break;
                    case "Body Weight":
                        bodyWeightCheckBox.setSelected(true);
                        break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load saved equipment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setToggleButtonSelected(String weight, ToggleButton... buttons) {
        for (ToggleButton button : buttons) {
            if (button.getText().equals(weight)) {
                button.setSelected(true);
                break;
            }
        }
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
        deselectToggleButtons(ezBar2_5, ezBar5, ezBar7_5, ezBar10,
                ezBar12_5, ezBar15, ezBar17_5, ezBar20);
        deselectToggleButtons(resistanceXXXLight, resistanceExtraLight, resistanceLight, resistanceMedium,
                resistanceHeavy, resistanceExtraHeavy, resistanceXXHeavy, resistanceXXXHeavy);
    }

    private void deselectToggleButtons(ToggleButton... buttons) {
        for (ToggleButton button : buttons) {
            button.setSelected(false);
        }
    }

    @FXML
    private void handleSave() {
        int userId = Session.getCurrentUser().getId();

        try (Connection conn = Database.getConnection()) {
            // Clear existing entries for this user
            String deleteSql = "DELETE FROM User_Equipment WHERE UserID = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, userId);
            deleteStmt.executeUpdate();

            String sql = "INSERT INTO User_Equipment (UserID, EquipmentType, Weight) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            if (bodyWeightCheckBox.isSelected()) {
                saveEquipment(pstmt, userId, "Body Weight", "N/A");
            } else {
                // Save all selected weights for each equipment type
                if (dumbbellsCheckBox.isSelected()) {
                    saveSelectedWeights(pstmt, userId, "Dumbbells", dumbbells2_5, dumbbells5, dumbbells7_5,
                            dumbbells10, dumbbells12_5, dumbbells15, dumbbells17_5, dumbbells20);
                }
                if (kettlebellsCheckBox.isSelected()) {
                    saveSelectedWeights(pstmt, userId, "Kettlebells", kettlebells2_5, kettlebells5, kettlebells7_5,
                            kettlebells10, kettlebells12_5, kettlebells15, kettlebells17_5, kettlebells20);
                }
                if (barbellsCheckBox.isSelected()) {
                    saveSelectedWeights(pstmt, userId, "Barbells", barbells2_5, barbells5, barbells7_5,
                            barbells10, barbells12_5, barbells15, barbells17_5, barbells20);
                }
                if (ezBarCheckBox.isSelected()) {
                    saveSelectedWeights(pstmt, userId, "EZ Bar", ezBar2_5, ezBar5, ezBar7_5,
                            ezBar10, ezBar12_5, ezBar15, ezBar17_5, ezBar20);
                }
                if (resistanceBandsCheckBox.isSelected()) {
                    saveSelectedWeights(pstmt, userId, "Resistance Bands", resistanceXXXLight, resistanceExtraLight,
                            resistanceLight, resistanceMedium, resistanceHeavy, resistanceExtraHeavy,
                            resistanceXXHeavy, resistanceXXXHeavy);
                }
                if (benchCheckBox.isSelected()) {

                    saveEquipment(pstmt, userId, "Bench", "N/A");
                }
            }

            System.out.println("Equipment saved successfully for UserID: " + userId);
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            System.err.println("Failed to save equipment to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveEquipment(PreparedStatement pstmt, int userId, String equipmentType, String weight) throws SQLException {
        pstmt.setInt(1, userId);
        pstmt.setString(2, equipmentType);
        pstmt.setString(3, weight);
        pstmt.executeUpdate();
    }

    private void saveSelectedWeights(PreparedStatement pstmt, int userId, String equipmentType, ToggleButton... buttons) throws SQLException {
        for (ToggleButton button : buttons) {
            if (button != null && button.isSelected()) {
                saveEquipment(pstmt, userId, equipmentType, button.getText());
            }
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}