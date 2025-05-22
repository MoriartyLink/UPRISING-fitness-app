package project.uprising.Program;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import project.uprising.Program.ScheduleController;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/uprising/program_schedule.fxml"));
        Parent root = loader.load();
        ScheduleController controller = loader.getController();
        controller.setProgramId(1); // Replace with actual program ID
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("Fitness Program Schedule");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}