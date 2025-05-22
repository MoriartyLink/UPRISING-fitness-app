package project.uprising;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class LaunchApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Create splash screen stage
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);

        // HBox for side-by-side layout, centered
        HBox splashPane = new HBox(30); // 30px spacing between image and text
        splashPane.setAlignment(Pos.CENTER); // Center contents horizontally and vertically
        splashPane.setPadding(new Insets(20)); // Padding for breathing room

        // Set background (black, no rounded corners)
        BackgroundFill backgroundFill = new BackgroundFill(
                Color.BLACK,
                new CornerRadii(0),
                Insets.EMPTY
        );
        splashPane.setBackground(new Background(backgroundFill));

        // Load the splash image
        Image splashImage;
        try {
            splashImage = new Image(getClass().getResourceAsStream("/project/uprising/ImageData/Login/Uprising.jfif"));
            ImageView imageView = new ImageView(splashImage);
            imageView.setFitWidth(250);
            imageView.setFitHeight(250);
            imageView.setPreserveRatio(true);
            splashPane.getChildren().add(imageView); // Image on left
        } catch (NullPointerException e) {
            System.out.println("Splash image not found at /project/uprising/ImageData/Login/Uprising.jfif");
            splashPane.getChildren().add(new Label("Loading...")); // Fallback
        }

        // Add title "UPRISING" with bold and italic
        Label titleLabel = new Label("UPRISING");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 40)); // Bold and italic
        titleLabel.setTextFill(Color.WHITE);
        splashPane.getChildren().add(titleLabel); // Title on right

        // Set splash screen size
        Scene splashScene = new Scene(splashPane, 700, 450);
        splashStage.setScene(splashScene);

        // Center the stage on the screen
        splashStage.centerOnScreen();

        // Fade-in effect
        splashPane.setOpacity(0); // Start invisible
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), splashPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(event -> {
            // After fade-in, wait then fade out
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Display for 2 seconds after fade-in
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                javafx.application.Platform.runLater(() -> {
                    // Fade-out effect
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), splashPane);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> {
                        splashStage.close();
                        // Load and show login screen
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(LaunchApplication.class.getResource("Login.fxml"));
                            Scene loginScene = new Scene(fxmlLoader.load(), 1550, 900);
                            primaryStage.setTitle("Login");
                            primaryStage.setScene(loginScene);
                            primaryStage.initStyle(StageStyle.UNDECORATED);
                            primaryStage.show();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    fadeOut.play();
                });
            }).start();
        });
        splashStage.show();
        fadeIn.play(); // Start fade-in
    }

    public static void main(String[] args) {
        launch();
    }
}