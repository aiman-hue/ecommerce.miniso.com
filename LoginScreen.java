package ecommerce;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginScreen {
    private VBox root;

    public LoginScreen(Stage stage) {
        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("auth-box");
        formBox.setAlignment(Pos.TOP_LEFT);  // Align all elements to top-left
        formBox.setPadding(new Insets(20));

        Label titleLabel = new Label("MINISO APP");
        titleLabel.getStyleClass().add("screen-title");

        // Username group
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        VBox usernameGroup = new VBox(5, usernameLabel, usernameField);
        usernameGroup.setAlignment(Pos.CENTER_LEFT);

        // Password group
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        VBox passwordGroup = new VBox(5, passwordLabel, passwordField);
        passwordGroup.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");
        HBox buttonBox = new HBox(10, loginBtn, registerBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Message Label
        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        // Login Action
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both fields.");
            } else {
                try {
                    boolean isValid = UserDataManager.validateUser(username, password);
                    if (isValid) {
                        if (username.equals("admin") && password.equals("admin123")) {
                            AdminDashboard adminDashboard = new AdminDashboard(stage, username);
                            stage.getScene().setRoot(adminDashboard.getRoot());
                        } else {
                            UserDashboard userDashboard = new UserDashboard(stage, username);
                            stage.getScene().setRoot(userDashboard.getRoot());
                        }
                    } else {
                        messageLabel.setText("Invalid username or password");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Register Action
        registerBtn.setOnAction(e -> {
            RegisterScreen registerScreen = new RegisterScreen(stage);
            stage.getScene().setRoot(registerScreen.getRoot());
        });

        formBox.getChildren().addAll(
            titleLabel,
            usernameGroup,
            passwordGroup,
            buttonBox,
            messageLabel
        );

        root = new VBox(formBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setPrefSize(1000, 600);
        root.getStyleClass().add("root");
    }

    public VBox getRoot() {
        return root;
    }
}
