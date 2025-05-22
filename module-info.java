module project.uprising {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;
    requires java.desktop;


    opens project.uprising to javafx.fxml;
    exports project.uprising;
    exports project.uprising.Challenge;
    opens project.uprising.Challenge to javafx.fxml;
    exports project.uprising.SignUp_Login;
    opens project.uprising.SignUp_Login to javafx.fxml;
    exports project.uprising.User;
    opens project.uprising.User to javafx.fxml;
    exports project.uprising.Workout;
    opens project.uprising.Workout to javafx.fxml;
    exports project.uprising.Program;
    opens project.uprising.Program to javafx.fxml;
}