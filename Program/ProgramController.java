package project.uprising.Program;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import project.uprising.User.Session;
import project.uprising.User.User;
import project.uprising.User.UserDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProgramController {
    @FXML private FlowPane programGrid;

    private ProgramDAO programDAO = new ProgramDAO();

    @FXML
    private void initialize() {
        loadPrograms();
    }

    private void loadPrograms() {
        try {
            List<Program> programs = programDAO.getAllPrograms();
            for (Program program : programs) {
                VBox card = createProgramCard(program);
                programGrid.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load programs: " + e.getMessage());
        }
    }

    private VBox createProgramCard(Program program) {
        VBox card = new VBox(10);
        card.getStyleClass().add("program-card");

        Label title = new Label(program.getProgramTitle());
        title.getStyleClass().add("card-title");

        Label details = new Label("Duration: " + program.getDuration() + " Days\nGoal: " + program.getGoal());
        details.getStyleClass().add("card-detail");

        card.getChildren().addAll(title, details);
        card.setOnMouseClicked(event -> openProgramDetail(program.getProgramID()));
        return card;
    }

    private void openProgramDetail(int programId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/programdetail.fxml"));
            Parent root = loader.load();
            ProgramDetailController controller = loader.getController();
            controller.setProgramId(programId);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);

            stage.setScene(new Scene(root, 1550, 900));
            stage.show();
            // Close current window if desired
            // ((Stage) programGrid.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to open program details: " + e.getMessage());
        }
    }

    @FXML
    private void showProfile() {
        // Implement profile navigation
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void checkConsistencyBonus() {
        User currentUser = Session.getCurrentUser(); // Assuming a Session class exists
        if (currentUser == null) return;

        UserDAO userDao = new UserDAO(); // Assuming this is how you instantiate it
        int streak = userDao.getExerciseStreak(currentUser.getId());
        if (streak >= 7) {
            int bonusExp = 100;
            currentUser.setExp(currentUser.getExp() + bonusExp);
            userDao.updateUserExp(currentUser.getId(), currentUser.getExp());
            System.out.println("Gained " + bonusExp + " bonus EXP for a " + streak + "-day streak!");
            // Optionally, show an alert to the user
        }
    }
}