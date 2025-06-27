package ecommerce;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class RegisterScreen {
    private VBox root;

    public RegisterScreen(Stage stage) {
        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("auth-box");
        formBox.setAlignment(Pos.TOP_LEFT);
        formBox.setPadding(new Insets(20));

        Label titleLabel = new Label("Register on MINISO");
        titleLabel.getStyleClass().add("screen-title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Username
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        VBox usernameGroup = new VBox(5, usernameLabel, usernameField);
        usernameGroup.setAlignment(Pos.CENTER_LEFT);

        // Email
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        VBox emailGroup = new VBox(5, emailLabel, emailField);
        emailGroup.setAlignment(Pos.CENTER_LEFT);

        // Password
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        VBox passwordGroup = new VBox(5, passwordLabel, passwordField);
        passwordGroup.setAlignment(Pos.CENTER_LEFT);

        // Confirm Password
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");
        VBox confirmPasswordGroup = new VBox(5, confirmPasswordLabel, confirmPasswordField);
        confirmPasswordGroup.setAlignment(Pos.CENTER_LEFT);

        // Gender
        Label genderLabel = new Label("Gender:");
        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        maleRadio.setToggleGroup(genderGroup);
        RadioButton femaleRadio = new RadioButton("Female");
        femaleRadio.setToggleGroup(genderGroup);
        HBox genderBox = new HBox(10, maleRadio, femaleRadio);
        genderBox.setAlignment(Pos.CENTER_LEFT);
        VBox genderGroupBox = new VBox(5, genderLabel, genderBox);
        genderGroupBox.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button registerBtn = new Button("Register");
        Button backBtn = new Button("Back to Login");
        HBox buttonBox = new HBox(10, registerBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Message Label
        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        // Register Action
        registerBtn.setOnAction((ActionEvent e) -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            RadioButton selectedGender = (RadioButton) genderGroup.getSelectedToggle();
            String gender = selectedGender != null ? selectedGender.getText() : "";

            // Validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || gender.isEmpty()) {
                messageLabel.setText("Please fill all fields.");
            } else if (!email.endsWith("@gmail.com")) {
                messageLabel.setText("Email must be a Gmail address (e.g., name@gmail.com).");
            } else if (password.length() < 8) {
                messageLabel.setText("Password must be at least 8 characters.");
            } else if (!password.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) {
                messageLabel.setText("Password must contain at least one special character.");
            } else if (!password.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match.");
            } else if (UserDataManager.userExists(username)) {
                messageLabel.setText("Username already exists.");
            } else {
                UserDataManager.saveUser(username, password, email, gender, "user");
                LoginScreen login = new LoginScreen(stage);
                stage.getScene().setRoot(login.getRoot());
            }
        });

        // Back button action
        backBtn.setOnAction(e -> {
            LoginScreen login = new LoginScreen(stage);
            stage.getScene().setRoot(login.getRoot());
        });

        formBox.getChildren().addAll(
            titleLabel,
            usernameGroup,
            emailGroup,
            passwordGroup,
            confirmPasswordGroup,
            genderGroupBox,
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
