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
        // Main container
        VBox formBox = new VBox(15);
        formBox.getStyleClass().addAll("auth-box", "register-form-container");
        formBox.setId("register-form-box");
        formBox.setAlignment(Pos.TOP_LEFT);
        formBox.setPadding(new Insets(20));

        // Title Section
        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().addAll("screen-title", "register-title");
        titleLabel.setId("register-main-title");
        
        Label subtitleLabel = new Label("Join MINISO family today!");
        subtitleLabel.getStyleClass().addAll("subtitle", "register-subtitle");
        subtitleLabel.setId("register-subtitle");
        
        // Separator
        Separator titleSeparator = new Separator();
        titleSeparator.getStyleClass().add("title-separator");
        titleSeparator.setId("register-title-separator");

        // Username
        Label usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().addAll("input-label", "username-label");
        usernameLabel.setId("register-username-label");
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.getStyleClass().addAll("form-field", "username-field");
        usernameField.setId("register-username-field");
        
        VBox usernameGroup = new VBox(5, usernameLabel, usernameField);
        usernameGroup.getStyleClass().add("input-group");
        usernameGroup.setId("register-username-group");
        usernameGroup.setAlignment(Pos.CENTER_LEFT);

        // Email
        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().addAll("input-label", "email-label");
        emailLabel.setId("register-email-label");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email (must be @gmail.com)");
        emailField.getStyleClass().addAll("form-field", "email-field");
        emailField.setId("register-email-field");
        
        VBox emailGroup = new VBox(5, emailLabel, emailField);
        emailGroup.getStyleClass().add("input-group");
        emailGroup.setId("register-email-group");
        emailGroup.setAlignment(Pos.CENTER_LEFT);

        // Password with strength indicator
        Label passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().addAll("input-label", "password-label");
        passwordLabel.setId("register-password-label");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password (min 8 chars, 1 special char)");
        passwordField.getStyleClass().addAll("form-field", "password-field");
        passwordField.setId("register-password-field");
        
        // Password strength indicator
        ProgressBar strengthIndicator = new ProgressBar(0);
        strengthIndicator.setId("password-strength-indicator");
        strengthIndicator.setPrefWidth(200);
        strengthIndicator.setStyle("-fx-accent: #e74c3c;");
        
        Label strengthLabel = new Label("Password strength: Weak");
        strengthLabel.getStyleClass().add("strength-label");
        strengthLabel.setId("password-strength-label");
        
        HBox strengthBox = new HBox(10, strengthIndicator, strengthLabel);
        strengthBox.setAlignment(Pos.CENTER_LEFT);
        strengthBox.setId("password-strength-box");
        
        VBox passwordGroup = new VBox(5, passwordLabel, passwordField, strengthBox);
        passwordGroup.getStyleClass().add("input-group");
        passwordGroup.setId("register-password-group");
        passwordGroup.setAlignment(Pos.CENTER_LEFT);

        // Confirm Password
        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.getStyleClass().addAll("input-label", "confirm-password-label");
        confirmPasswordLabel.setId("register-confirm-password-label");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");
        confirmPasswordField.getStyleClass().addAll("form-field", "confirm-password-field");
        confirmPasswordField.setId("register-confirm-password-field");
        
        // Match indicator
        Label matchLabel = new Label();
        matchLabel.getStyleClass().add("match-indicator");
        matchLabel.setId("password-match-indicator");
        
        VBox confirmPasswordGroup = new VBox(5, confirmPasswordLabel, confirmPasswordField, matchLabel);
        confirmPasswordGroup.getStyleClass().add("input-group");
        confirmPasswordGroup.setId("register-confirm-password-group");
        confirmPasswordGroup.setAlignment(Pos.CENTER_LEFT);

        // Gender
        Label genderLabel = new Label("Gender:");
        genderLabel.getStyleClass().addAll("input-label", "gender-label");
        genderLabel.setId("register-gender-label");
        
        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        maleRadio.getStyleClass().add("gender-radio");
        maleRadio.setId("register-male-radio");
        maleRadio.setToggleGroup(genderGroup);
        
        RadioButton femaleRadio = new RadioButton("Female");
        femaleRadio.getStyleClass().add("gender-radio");
        femaleRadio.setId("register-female-radio");
        femaleRadio.setToggleGroup(genderGroup);
        
        HBox genderBox = new HBox(20, maleRadio, femaleRadio);
        genderBox.setAlignment(Pos.CENTER_LEFT);
        genderBox.setId("register-gender-box");
        
        VBox genderGroupBox = new VBox(5, genderLabel, genderBox);
        genderGroupBox.getStyleClass().add("input-group");
        genderGroupBox.setId("register-gender-group");
        genderGroupBox.setAlignment(Pos.CENTER_LEFT);

        // Terms and Conditions
        CheckBox termsCheckBox = new CheckBox("I agree to the Terms and Conditions");
        termsCheckBox.getStyleClass().add("terms-checkbox");
        termsCheckBox.setId("register-terms-checkbox");

        // Buttons
        Button registerBtn = new Button("Create Account");
        registerBtn.getStyleClass().addAll("primary-btn", "register-btn");
        registerBtn.setId("register-submit-btn");
        
        Button backBtn = new Button("← Back to Login");
        backBtn.getStyleClass().addAll("secondary-btn", "back-btn");
        backBtn.setId("register-back-btn");
        
        HBox buttonBox = new HBox(15, registerBtn, backBtn);
        buttonBox.getStyleClass().add("button-group");
        buttonBox.setId("register-button-group");
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Message Label
        Label messageLabel = new Label();
        messageLabel.getStyleClass().addAll("message-label");
        messageLabel.setId("register-message-label");
        messageLabel.setVisible(false);

        // Footer note
        Label footerNote = new Label("Already have an account? Click 'Back to Login'");
        footerNote.getStyleClass().addAll("footer-note", "register-footer-note");
        footerNote.setId("register-footer-note");

        // Password strength checker
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            String password = newVal;
            double strength = 0;
            String strengthText = "Weak";
            String color = "#e74c3c";
            
            if (password.length() >= 8) strength += 0.3;
            if (password.matches(".*[A-Z].*")) strength += 0.2;
            if (password.matches(".*[a-z].*")) strength += 0.2;
            if (password.matches(".*\\d.*")) strength += 0.15;
            if (password.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) strength += 0.15;
            
            strengthIndicator.setProgress(strength);
            
            if (strength >= 0.8) {
                strengthText = "Strong";
                color = "#27ae60";
                strengthIndicator.setStyle("-fx-accent: #27ae60;");
            } else if (strength >= 0.5) {
                strengthText = "Medium";
                color = "#f39c12";
                strengthIndicator.setStyle("-fx-accent: #f39c12;");
            } else {
                strengthText = "Weak";
                color = "#e74c3c";
                strengthIndicator.setStyle("-fx-accent: #e74c3c;");
            }
            
            strengthLabel.setText("Password strength: " + strengthText);
            strengthLabel.setStyle("-fx-text-fill: " + color + ";");
        });
        
        // Password match checker
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            String password = passwordField.getText();
            String confirm = newVal;
            
            if (!confirm.isEmpty()) {
                if (password.equals(confirm)) {
                    matchLabel.setText("✓ Passwords match");
                    matchLabel.getStyleClass().removeAll("match-error");
                    matchLabel.getStyleClass().add("match-success");
                    confirmPasswordField.getStyleClass().remove("field-error");
                } else {
                    matchLabel.setText("✗ Passwords do not match");
                    matchLabel.getStyleClass().removeAll("match-success");
                    matchLabel.getStyleClass().add("match-error");
                    confirmPasswordField.getStyleClass().add("field-error");
                }
            } else {
                matchLabel.setText("");
                confirmPasswordField.getStyleClass().remove("field-error");
            }
        });
        
        // Email validation live
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.endsWith("@gmail.com")) {
                emailField.getStyleClass().add("field-error");
            } else {
                emailField.getStyleClass().remove("field-error");
            }
        });

        // Register Action
        registerBtn.setOnAction((ActionEvent e) -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            RadioButton selectedGender = (RadioButton) genderGroup.getSelectedToggle();
            String gender = selectedGender != null ? selectedGender.getText() : "";
            
            // Clear previous error styling
            usernameField.getStyleClass().remove("field-error");
            emailField.getStyleClass().remove("field-error");
            passwordField.getStyleClass().remove("field-error");
            confirmPasswordField.getStyleClass().remove("field-error");
            
            // Validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || gender.isEmpty()) {
                messageLabel.setText("❌ Please fill all fields.");
                messageLabel.getStyleClass().add("error-message");
                messageLabel.setVisible(true);
                
                if (username.isEmpty()) usernameField.getStyleClass().add("field-error");
                if (email.isEmpty()) emailField.getStyleClass().add("field-error");
                if (password.isEmpty()) passwordField.getStyleClass().add("field-error");
                if (confirmPassword.isEmpty()) confirmPasswordField.getStyleClass().add("field-error");
                if (gender.isEmpty()) genderGroupBox.getStyleClass().add("field-error");
                
            } else if (!email.endsWith("@gmail.com")) {
                messageLabel.setText("❌ Email must be a Gmail address (e.g., name@gmail.com).");
                messageLabel.getStyleClass().add("error-message");
                messageLabel.setVisible(true);
                emailField.getStyleClass().add("field-error");
                
            } else if (password.length() < 8) {
                messageLabel.setText("❌ Password must be at least 8 characters.");
                messageLabel.getStyleClass().add("error-message");
                messageLabel.setVisible(true);
                passwordField.getStyleClass().add("field-error");
                
            } else if (!password.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) {
                messageLabel.setText("❌ Password must contain at least one special character (!@#$%^&* etc).");
                messageLabel.getStyleClass().add("error-message");
                messageLabel.setVisible(true);
                passwordField.getStyleClass().add("field-error");
                
            } else if (!password.equals(confirmPassword)) {
                messageLabel.setText("❌ Passwords do not match.");
                messageLabel.getStyleClass().add("error-message");
                messageLabel.setVisible(true);
                confirmPasswordField.getStyleClass().add("field-error");
                
            } else if (!termsCheckBox.isSelected()) {
                messageLabel.setText("❌ Please agree to the Terms and Conditions.");
                messageLabel.getStyleClass().add("error-message");
                messageLabel.setVisible(true);
                termsCheckBox.getStyleClass().add("field-error");
                
            } else if (UserDataManager.userExists(username)) {
                messageLabel.setText("❌ Username already exists. Please choose another.");
                messageLabel.getStyleClass().add("error-message");
                messageLabel.setVisible(true);
                usernameField.getStyleClass().add("field-error");
                
            } else {
                // Show loading state
                registerBtn.setText("Creating account...");
                registerBtn.setDisable(true);
                
                UserDataManager.saveUser(username, password, email, gender, "user");
                
                // Show success message
                messageLabel.setText("✅ Account created successfully! Redirecting to login...");
                messageLabel.getStyleClass().removeAll("error-message");
                messageLabel.getStyleClass().add("success-message");
                messageLabel.setVisible(true);
                
                // Delay redirect to show success message
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pause.setOnFinished(event -> {
                    LoginScreen login = new LoginScreen(stage);
                    stage.getScene().setRoot(login.getRoot());
                });
                pause.play();
            }
        });

        // Clear error styling when typing
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            usernameField.getStyleClass().remove("field-error");
            messageLabel.setVisible(false);
        });
        
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            emailField.getStyleClass().remove("field-error");
            messageLabel.setVisible(false);
        });
        
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            passwordField.getStyleClass().remove("field-error");
            messageLabel.setVisible(false);
        });
        
        termsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            termsCheckBox.getStyleClass().remove("field-error");
            messageLabel.setVisible(false);
        });
        
        genderGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            genderGroupBox.getStyleClass().remove("field-error");
            messageLabel.setVisible(false);
        });

        // Back button action
        backBtn.setOnAction(e -> {
            backBtn.getStyleClass().add("btn-pressed");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
            pause.setOnFinished(event -> backBtn.getStyleClass().remove("btn-pressed"));
            pause.play();
            
            LoginScreen login = new LoginScreen(stage);
            stage.getScene().setRoot(login.getRoot());
        });

        // Enter key press to register
        confirmPasswordField.setOnAction(e -> registerBtn.fire());

        formBox.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            titleSeparator,
            usernameGroup,
            emailGroup,
            passwordGroup,
            confirmPasswordGroup,
            genderGroupBox,
            termsCheckBox,
            buttonBox,
            messageLabel,
            footerNote
        );

        root = new VBox(formBox);
        root.getStyleClass().addAll("root", "register-root");
        root.setId("register-screen-root");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setPrefSize(1000, 600);
    }

    public VBox getRoot() {
        return root;
    }
}