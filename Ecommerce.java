package ecommerce;

import java.net.URL;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Ecommerce extends Application {

    @Override
    public void start(Stage stage) {
        LoginScreen loginScreen = new LoginScreen(stage);
        Scene scene = new Scene(loginScreen.getRoot(), 1000, 600);
        
        // Load stylesheet
        URL resource = getClass().getResource("/resources/styles/style.css");
        if (resource == null) {
            System.out.println("Resource not found!");
        } else {
            scene.getStylesheets().add(resource.toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle("Miniso eCommerce App");
        stage.getIcons().add(new Image("file:/C:\\Users\\HAROON TRADERS\\OneDrive\\Pictures\\Screenshots\\Screenshot 2025-06-29 143519.png"));

        stage.setMaximized(true);  // ✅ Make the window open in full screen (maximized)
        stage.show();
    }

    
}
