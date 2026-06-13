package ecommerce;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginScreen {
    private VBox root;

    public LoginScreen(Stage stage) {
        root = new VBox();
        root.getStyleClass().add("login-root");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(0));
        
        // Create HBox for 50/50 split - this will take full width
        HBox splitLayout = new HBox();
        splitLayout.getStyleClass().add("split-horizontal");
        splitLayout.setPrefWidth(1000);
        splitLayout.setPrefHeight(600);
        
        // LEFT PANEL - Login Form (50%)
        VBox leftPanel = new VBox();
        leftPanel.getStyleClass().add("left-panel");
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPrefWidth(500);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        
        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("auth-box");
        formBox.setAlignment(Pos.TOP_LEFT);
        formBox.setMaxWidth(400);
        
        // MINISO Badge
        Label minisoBadge = new Label("✦ MINISO ✦");
        minisoBadge.getStyleClass().add("miniso-badge");
        
        Label titleLabel = new Label("MINISO");
        titleLabel.getStyleClass().add("screen-title");
        
        Label welcomeMsg = new Label("Welcome back! Please login to your account");
        welcomeMsg.getStyleClass().add("welcome-message");
        
        // Username
        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("input-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("form-field");
        VBox usernameGroup = new VBox(5, usernameLabel, usernameField);
        usernameGroup.getStyleClass().add("input-group");
        
        // Password
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("input-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("form-field");
        VBox passwordGroup = new VBox(5, passwordLabel, passwordField);
        passwordGroup.getStyleClass().add("input-group");
        
        // Forgot password
        Hyperlink forgotLink = new Hyperlink("Forgot Password?");
        forgotLink.getStyleClass().add("forgot-link");
        HBox forgotBox = new HBox();
        forgotBox.setAlignment(Pos.CENTER_RIGHT);
        forgotBox.getChildren().add(forgotLink);
        
        // Buttons
        Button loginBtn = new Button("LOGIN");
        Button registerBtn = new Button("CREATE ACCOUNT");
        loginBtn.getStyleClass().add("primary-btn");
        registerBtn.getStyleClass().add("secondary-btn");
        
        HBox buttonBox = new HBox(15, loginBtn, registerBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getStyleClass().add("button-group");
        
        // Message Label
        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setVisible(false);
        
        formBox.getChildren().addAll(
            minisoBadge, titleLabel, welcomeMsg,
            usernameGroup, passwordGroup, forgotBox,
            buttonBox, messageLabel
        );
        
        leftPanel.getChildren().add(formBox);
        
        // RIGHT PANEL - MINISO Branding (50%)
        VBox rightPanel = new VBox();
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPrefWidth(500);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        VBox brandContainer = new VBox(25);
        brandContainer.getStyleClass().add("brand-container");
        brandContainer.setAlignment(Pos.CENTER);
        
        Label minisoLogo = new Label("MINISO");
        minisoLogo.getStyleClass().add("miniso-logo");
        
        Label minisoSub = new Label("LIFESTYLE PRODUCTS");
        minisoSub.getStyleClass().add("miniso-subtitle");
        
        Label cartEmoji = new Label("🛍️");
        cartEmoji.getStyleClass().add("shopping-cart-icon");
        
        // Stats Container
        HBox statsContainer = new HBox(30);
        statsContainer.getStyleClass().add("stats-container");
        statsContainer.setAlignment(Pos.CENTER);
        
        VBox stat1 = new VBox(5);
        stat1.setAlignment(Pos.CENTER);
        Label stat1Num = new Label("5000+");
        stat1Num.getStyleClass().add("stat-number");
        Label stat1Label = new Label("Products");
        stat1Label.getStyleClass().add("stat-label-text");
        stat1.getChildren().addAll(stat1Num, stat1Label);
        
        VBox stat2 = new VBox(5);
        stat2.setAlignment(Pos.CENTER);
        Label stat2Num = new Label("1M+");
        stat2Num.getStyleClass().add("stat-number");
        Label stat2Label = new Label("Happy Customers");
        stat2Label.getStyleClass().add("stat-label-text");
        stat2.getChildren().addAll(stat2Num, stat2Label);
        
        VBox stat3 = new VBox(5);
        stat3.setAlignment(Pos.CENTER);
        Label stat3Num = new Label("50+");
        stat3Num.getStyleClass().add("stat-number");
        Label stat3Label = new Label("Countries");
        stat3Label.getStyleClass().add("stat-label-text");
        stat3.getChildren().addAll(stat3Num, stat3Label);
        
        statsContainer.getChildren().addAll(stat1, stat2, stat3);
        
        Label quote = new Label("\"Life is for fun, enjoy the joy of quality living\"");
        quote.getStyleClass().add("brand-quote-text");
        quote.setWrapText(true);
        quote.setAlignment(Pos.CENTER);
        
        brandContainer.getChildren().addAll(minisoLogo, minisoSub, cartEmoji, statsContainer, quote);
        rightPanel.getChildren().add(brandContainer);
        
        // Add both panels to HBox
        splitLayout.getChildren().addAll(leftPanel, rightPanel);
        
        // Add HBox to root VBox - this fills the entire scene
        root.getChildren().add(splitLayout);
        VBox.setVgrow(splitLayout, Priority.ALWAYS);
        
        // Login Action
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("❌ Please enter both fields.");
                messageLabel.getStyleClass().add("error-message");
                messageLabel.setVisible(true);
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
                        messageLabel.setText("❌ Invalid username or password");
                        messageLabel.getStyleClass().add("error-message");
                        messageLabel.setVisible(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    messageLabel.setText("❌ An error occurred");
                    messageLabel.getStyleClass().add("error-message");
                    messageLabel.setVisible(true);
                }
            }
        });
        
        registerBtn.setOnAction(e -> {
            RegisterScreen registerScreen = new RegisterScreen(stage);
            stage.getScene().setRoot(registerScreen.getRoot());
        });
        
        forgotLink.setOnAction(e -> {
            messageLabel.setText("📧 Contact admin@miniso.com to reset password");
            messageLabel.getStyleClass().add("info-message");
            messageLabel.setVisible(true);
        });
    }
    
    public VBox getRoot() {
        return root;
    }
}