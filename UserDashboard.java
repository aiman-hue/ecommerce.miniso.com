package ecommerce;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.stage.FileChooser;

public class UserDashboard {

    private BorderPane root;
    private StackPane contentPane;
    private List<Product> cart = new ArrayList<>();
    private List<Product> wishlist = new ArrayList<>();
    private String username;
    private Stage stage;

    public UserDashboard(Stage stage, String username) {
        this.stage = stage;
        this.username = username;

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #1a1a1a;");

        sidebar.setPrefWidth(200);
        sidebar.setAlignment(Pos.TOP_LEFT);

        Label menuTitle = new Label("Welcome " + username);
        menuTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button homeBtn = new Button("Home");
        Button ordersBtn = new Button("My Orders");
        Button cartBtn = new Button("Cart");
        Button wishlistBtn = new Button("Wishlist");
        Button settingsBtn = new Button("Account Settings");
        Button logoutBtn = new Button("Logout");

        for (Button btn : new Button[]{homeBtn, ordersBtn, cartBtn, wishlistBtn, settingsBtn, logoutBtn}) {
            btn.setMaxWidth(Double.MAX_VALUE);
           btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        }

        sidebar.getChildren().addAll(menuTitle, homeBtn, ordersBtn, cartBtn, wishlistBtn, settingsBtn, logoutBtn);

        contentPane = new StackPane();
        contentPane.setPadding(new Insets(20));
        showHome();  // Load products initially

        homeBtn.setOnAction(e -> showHome());
        ordersBtn.setOnAction(e -> showConfirmedOrders(username));

        cartBtn.setOnAction(e -> showCart());
        wishlistBtn.setOnAction(e -> showWishlist());
        settingsBtn.setOnAction(e -> showSettings());
        logoutBtn.setOnAction(e -> {
            LoginScreen login = new LoginScreen(stage);
            stage.getScene().setRoot(login.getRoot());  // <-- just swap the root node

        });


        root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentPane);
        root.setPrefSize(1000, 600);
    }

    private void showHome() {
    VBox homeBox = new VBox(20);
    homeBox.setPadding(new Insets(20));
    homeBox.setAlignment(Pos.TOP_CENTER);
    homeBox.setStyle("-fx-background-color: #ffffff;");

    // Heading
    Label heading = new Label("MINISO");
    heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");


    // Search + Filter UI
    HBox filterBar = new HBox(10);
    filterBar.setAlignment(Pos.CENTER);

    TextField searchField = new TextField();
    searchField.setPromptText("Search product name...");
    searchField.setPrefWidth(200);

    ComboBox<String> categoryBox = new ComboBox<>();
    categoryBox.getItems().add("All Categories");
    categoryBox.getItems().addAll(DatabaseManager.loadCategories());
    categoryBox.getSelectionModel().selectFirst();

    Button searchBtn = new Button("Search");

    filterBar.getChildren().addAll(searchField, categoryBox, searchBtn);

    // Product Container
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background-color: transparent;");

    FlowPane productPane = new FlowPane(); // ✅ Defined before lambda
    productPane.setHgap(30);
    productPane.setVgap(20);
    productPane.setPadding(new Insets(10));
    productPane.setAlignment(Pos.CENTER);

    List<Product> allProducts = DatabaseManager.loadProducts(); // ✅ Defined before lambda
    displayFilteredProducts(allProducts, productPane, "", "All Categories");

    // ✅ NOW add the category change listener
    categoryBox.setOnAction(e -> {
        String search = searchField.getText().toLowerCase().trim();
        String selectedCategory = categoryBox.getValue();
        displayFilteredProducts(allProducts, productPane, search, selectedCategory);
    });

    // Search button logic
    searchBtn.setOnAction(e -> {
        String search = searchField.getText().toLowerCase().trim();
        String selectedCategory = categoryBox.getValue();
        displayFilteredProducts(allProducts, productPane, search, selectedCategory);
    });

    scrollPane.setContent(productPane);
    homeBox.getChildren().addAll(heading, filterBar, scrollPane);
    contentPane.getChildren().setAll(homeBox);
}



  private VBox createProductCard(Product p) {
    VBox card = new VBox(10);
    card.setAlignment(Pos.CENTER);
    card.setStyle(
    "-fx-background-color: white;" +
    "-fx-border-color: #e74c3c;" +
    "-fx-border-width: 1.5px;" +
    "-fx-border-radius: 10px;" +
    "-fx-background-radius: 10px;" +
    "-fx-padding: 12px;" +
    "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.1), 6, 0, 0, 4);" +
    "-fx-cursor: hand;"
);

    card.setPrefWidth(220);

    ImageView imageView;
    try {
        imageView = new ImageView(new Image(new File(p.getImagePath()).toURI().toString()));
    } catch (Exception e) {
        imageView = new ImageView(); // fallback
    }

    imageView.setFitHeight(200);
    imageView.setFitWidth(200);
    imageView.setPreserveRatio(true);

    Label nameLabel = new Label(p.getName());
    nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    Label priceLabel = new Label("Rs. " + String.format("%.2f", p.getPrice()));
    priceLabel.setStyle("-fx-text-fill: #555;");

    Button addToCartBtn = new Button("🛒");
    Button addToWishlistBtn = new Button("❤");
    Button viewBtn = new Button("View");

    HBox buttonBox = new HBox(10, addToCartBtn, addToWishlistBtn, viewBtn);
    buttonBox.setAlignment(Pos.CENTER);

  addToCartBtn.setOnAction(e -> {
    DatabaseManager.addToCart(username, p.getId());
    showAlert(Alert.AlertType.INFORMATION, "Cart", p.getName() + " added to cart.");
    
});

addToWishlistBtn.setOnAction(e -> {
    DatabaseManager.addToWishlist(username, p.getId());
    showAlert(Alert.AlertType.INFORMATION, "Wishlist", p.getName() + " added to wishlist.");
    
});


    viewBtn.setOnAction(e -> openProductDetail(p));

    card.getChildren().addAll(imageView, nameLabel, priceLabel, buttonBox);
    return card;
}


  private void openProductDetail(Product p) {
    // Top bar with Refresh button
    HBox topBar = new HBox();
    topBar.setPadding(new Insets(0, 0, 15, 0));
    topBar.setAlignment(Pos.TOP_RIGHT);

    Button refreshBtn = new Button("Refresh");
    refreshBtn.setOnAction(e -> openProductDetail(p));
    topBar.getChildren().add(refreshBtn);

    VBox detailBox = new VBox(15);
    detailBox.setPadding(new Insets(20));
    detailBox.setStyle("-fx-background-color: white;");
    detailBox.setAlignment(Pos.TOP_CENTER);

    // Product image
    ImageView imageView = new ImageView();
    try {
        File file = new File(p.getImagePath());
        imageView.setImage(file.exists() ? new Image(file.toURI().toString())
            : new Image(getClass().getResource("/resources/images/placeholder.png").toString()));
    } catch (Exception e) {
        imageView.setImage(new Image(getClass().getResource("/resources/images/placeholder.png").toString()));
    }

    imageView.setFitHeight(300);
    imageView.setFitWidth(300);
    imageView.setPreserveRatio(true);

    Label name = new Label(p.getName() != null ? p.getName() : "Unnamed Product");
    name.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

    Label price = new Label("Price: Rs. " + p.getPrice());
    Label discounted = new Label("Discounted Price: Rs. " + p.getDiscountedPrice());
    discounted.setStyle("-fx-text-fill: #27ae60;");
    Label percent = new Label("Discount: " + p.getDiscountPercent() + "%");

    Label about = new Label("About: " + (p.getAbout() != null ? p.getAbout() : "Not available"));
    about.setWrapText(true);

    // Show list of reviews
    VBox reviewList = new VBox(10);
    reviewList.setPadding(new Insets(10));
    reviewList.setStyle("-fx-background-color: #f9f9f9;");

    List<String> productReviews = DatabaseManager.getReviewsForProduct(p.getId());
    if (productReviews.isEmpty()) {
        reviewList.getChildren().add(new Label("No reviews yet."));
    } else {
        for (String reviewText : productReviews) {
            Label reviewLabel = new Label(reviewText);
            reviewLabel.setWrapText(true);
            reviewLabel.getStyleClass().add("review-entry");
            reviewList.getChildren().add(reviewLabel);
        }
    }

    // Rating stars
    Label ratingLabel = new Label("Give Rating:");
    HBox starBox = new HBox(5);
    starBox.setAlignment(Pos.CENTER_LEFT);

    Label[] stars = new Label[5];
    int[] selectedRating = {0};

    for (int i = 0; i < 5; i++) {
        Label star = new Label("☆");
        star.setStyle("-fx-font-size: 24px; -fx-cursor: hand;");
        int index = i;

        star.setOnMouseEntered(e -> {
            for (int j = 0; j <= index; j++) stars[j].setText("★");
            for (int j = index + 1; j < 5; j++) stars[j].setText("☆");
        });

        star.setOnMouseExited(e -> {
            for (int j = 0; j < 5; j++) stars[j].setText(j < selectedRating[0] ? "★" : "☆");
        });

        star.setOnMouseClicked(e -> {
            selectedRating[0] = index + 1;
            for (int j = 0; j < 5; j++) stars[j].setText(j < selectedRating[0] ? "★" : "☆");
        });

        stars[i] = star;
        starBox.getChildren().add(star);
    }

    // Feedback and review input
    Label feedbackLabel = new Label("Write Feedback:");
    TextArea feedbackArea = new TextArea();
    feedbackArea.setPromptText("Write your feedback...");
    feedbackArea.setWrapText(true);
    feedbackArea.setPrefRowCount(3);

    Label reviewLabel = new Label("Write Review:");
    TextArea reviewArea = new TextArea();
    reviewArea.setPromptText("Write your review...");
    reviewArea.setWrapText(true);
    reviewArea.setPrefRowCount(2);

    Button submitReview = new Button("Submit Review");
    submitReview.setOnAction(e -> {
        int rating = selectedRating[0];
        String feedback = feedbackArea.getText().trim();
        String review = reviewArea.getText().trim();

        if (rating == 0 || feedback.isEmpty() || review.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please provide rating, feedback, and review.");
            return;
        }

        boolean success = DatabaseManager.insertReview(p.getId(), username, rating, review, feedback);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your feedback has been submitted.");
            openProductDetail(DatabaseManager.getProductById(p.getId())); // Reload
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit feedback.");
        }
    });

    // Button bar
    HBox buttonBar = new HBox(20);
    buttonBar.setAlignment(Pos.CENTER);

    Button addToCart = new Button("Add to Cart");
    Button addToWishlist = new Button("Add to Wishlist");
    Button back = new Button("Back");

    addToCart.setOnAction(e -> {
        DatabaseManager.addToCart(username, p.getId());
        showAlert(Alert.AlertType.INFORMATION, "Cart", p.getName() + " added to cart.");
    });

    addToWishlist.setOnAction(e -> {
        DatabaseManager.addToWishlist(username, p.getId());
        showAlert(Alert.AlertType.INFORMATION, "Wishlist", p.getName() + " added to wishlist.");
    });

    back.setOnAction(e -> showHome());

    buttonBar.getChildren().addAll(addToCart, addToWishlist, back);

    // Add everything to detailBox
    detailBox.getChildren().addAll(
        topBar, imageView, name, price, discounted, percent, about,
        reviewList,
        ratingLabel, starBox,
        feedbackLabel, feedbackArea,
        reviewLabel, reviewArea,
        submitReview,
        buttonBar
    );

    // Apply styling (optional CSS classes)
    for (Label star : stars) star.getStyleClass().add("star");
    name.getStyleClass().add("product-title");

    ScrollPane scroll = new ScrollPane(detailBox);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");
    contentPane.getChildren().setAll(scroll);
}



private void showCart() {
    VBox box = new VBox(10);
    box.setPadding(new Insets(10));
    Label title = new Label("Your Cart:");
    title.setStyle("-fx-font-size: 18px;");
    box.getChildren().add(title);

    List<Product> cartItems = DatabaseManager.loadCart(username);

    if (cartItems == null || cartItems.isEmpty()) {
        box.getChildren().add(new Label("Cart is empty."));
    } else {
        VBox itemsBox = new VBox(10);
        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        for (Product p : cartItems) {
            int[] quantity = {1};

            Label nameLabel = new Label(p.getName());
            Label priceLabel = new Label("Price: Rs. " + p.getDiscountedPrice());
            Label quantityLabel = new Label("Qty: 1");

            Button increaseBtn = new Button("+");
            Button decreaseBtn = new Button("-");

            HBox controls = new HBox(5, decreaseBtn, quantityLabel, increaseBtn);
            VBox productBox = new VBox(5, nameLabel, priceLabel, controls);
            productBox.setStyle("-fx-border-color: #ccc; -fx-padding: 10; -fx-background-color: #f5f5f5;");

            increaseBtn.setOnAction(e -> {
                quantity[0]++;
                quantityLabel.setText("Qty: " + quantity[0]);
                DatabaseManager.updateCartQuantity(username, p.getId(), quantity[0]);
                updateTotal(itemsBox, totalLabel);
            });

            decreaseBtn.setOnAction(e -> {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityLabel.setText("Qty: " + quantity[0]);
                    DatabaseManager.updateCartQuantity(username, p.getId(), quantity[0]);
                    updateTotal(itemsBox, totalLabel);
                } else {
                    itemsBox.getChildren().remove(productBox);
                    DatabaseManager.removeFromCart(username, p.getId());
                    updateTotal(itemsBox, totalLabel);
                }
            });

            productBox.setUserData(new Object[]{p, quantity});
            itemsBox.getChildren().add(productBox);
        }

        updateTotal(itemsBox, totalLabel);

        Button checkout = new Button("Checkout");
checkout.setOnAction(e -> {
    checkout.setDisable(true);

    List<Product> currentCart = DatabaseManager.loadCart(username);
    if (currentCart == null || currentCart.isEmpty()) {
        showAlert(Alert.AlertType.WARNING, "Checkout", "Your cart is empty!");
        checkout.setDisable(false);
        return;
    }

    // Get the total using quantity per item
    double totalAmount = 0;
    for (Product p : currentCart) {
        int qty = DatabaseManager.getCartQuantity(username, p.getId());
        totalAmount += p.getDiscountedPrice() * qty;
    }

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Checkout Details");
    dialog.setHeaderText("Please provide shipping details.");

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField fullNameField = new TextField();
    TextField addressField = new TextField();
    TextField contactField = new TextField();
    ComboBox<String> countryBox = new ComboBox<>(FXCollections.observableArrayList("Pakistan"));
    ComboBox<String> cityBox = new ComboBox<>(FXCollections.observableArrayList("Karachi", "Lahore", "Islamabad", "Multan"));

    grid.add(new Label("Full Name:"), 0, 0);
    grid.add(fullNameField, 1, 0);
    grid.add(new Label("Shipping Address:"), 0, 1);
    grid.add(addressField, 1, 1);
    grid.add(new Label("Contact Number:"), 0, 2);
    grid.add(contactField, 1, 2);
    grid.add(new Label("Country:"), 0, 3);
    grid.add(countryBox, 1, 3);
    grid.add(new Label("City:"), 0, 4);
    grid.add(cityBox, 1, 4);
    grid.add(new Label("Payment Mode:"), 0, 5);
    grid.add(new Label("Cash on Delivery"), 1, 5);

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Optional<ButtonType> result = dialog.showAndWait();

    if (result.isPresent() && result.get() == ButtonType.OK) {
        String fullName = fullNameField.getText().trim();
        String address = addressField.getText().trim();
        String contact = contactField.getText().trim();
        String country = countryBox.getValue();
        String city = cityBox.getValue();

        if (fullName.isEmpty() || address.isEmpty() || contact.isEmpty() || country == null || city == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Data", "Please fill all required fields.");
            checkout.setDisable(false);
            return;
        }

        try {
            int userId = DatabaseManager.getUserIdByUsername(username);

            // Step 1: Insert the order
            int orderId = DatabaseManager.insertOrder(
                    userId,
                    fullName,
                    totalAmount,
                    address,
                    contact,
                    country,
                    city,
                    null // No receipt for COD
            );

            if (orderId <= 0) {
                showAlert(Alert.AlertType.ERROR, "Order Failed", "Order could not be placed.");
                checkout.setDisable(false);
                return;
            }

            // Step 2: Insert each item with actual quantity
            for (Product p : currentCart) {
                int productId = p.getId();
                int quantity = DatabaseManager.getCartQuantity(username, productId);
                DatabaseManager.insertOrderItem(orderId, productId, quantity, p.getDiscountedPrice());
                DatabaseManager.removeFromCart(username, productId);
            }

            // Step 3: Confirmation
            showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Your order #" + orderId + " has been placed successfully!");
            showCart();
            showConfirmedOrders(username);

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Checkout", "An error occurred during checkout.");
        }
    }

    checkout.setDisable(false);
});


        box.getChildren().addAll(itemsBox, totalLabel, checkout);
    }

    contentPane.getChildren().setAll(box);
}

private void updateTotal(VBox itemsBox, Label totalLabel) {
    double total = 0;

    for (Node node : itemsBox.getChildren()) {
        if (node instanceof VBox) {
            Object[] data = (Object[]) node.getUserData();
            if (data != null && data.length >= 2) {
                Product p = (Product) data[0];
                int qty = ((int[]) data[1])[0];
                total += p.getDiscountedPrice() * qty;
            }
        }
    }

    totalLabel.setText("Total: Rs. " + String.format("%.2f", total));
    totalLabel.setUserData(total); // optional: useful if you want to retrieve total during checkout
}

private void showAlert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
private void showWishlist() {
    VBox box = new VBox(10);
    box.setPadding(new Insets(10));
    Label title = new Label("Your Wishlist:");
    title.setStyle("-fx-font-size: 18px;");
    box.getChildren().add(title);
    List<Product> wishlistItems = DatabaseManager.loadWishlist(username);
    if (wishlistItems == null || wishlistItems.isEmpty()) {
        box.getChildren().add(new Label("Wishlist is empty."));
    } else {
        for (Product p : wishlistItems) {
            VBox productCard = new VBox(5);
            productCard.setPadding(new Insets(10));
            productCard.setStyle("-fx-border-color: #ccc; -fx-background-color: #f5f5f5; -fx-border-radius: 8px; -fx-background-radius: 8px;");
            Label nameLabel = new Label(p.getName());
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label priceLabel = new Label("Price: Rs. " + p.getDiscountedPrice());
            priceLabel.setStyle("-fx-font-size: 14px;");
            Button removeBtn = new Button("❌ Clear Wishlist");
            removeBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
            removeBtn.setOnAction(e -> {
                DatabaseManager.removeFromWishlist(username, p.getId());
                showWishlist(); // Refresh
            });
            productCard.getChildren().addAll(nameLabel, priceLabel, removeBtn);
            box.getChildren().add(productCard);
        }
    }
    contentPane.getChildren().setAll(box);
}
    private void showSettings() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        Label title = new Label("Account Settings");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("New Password");

        Button update = new Button("Update");
        box.getChildren().addAll(title, emailField, passField, update);

        contentPane.getChildren().setAll(box);
    }
    private void showText(String msg) {
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-font-size: 18px;");
        contentPane.getChildren().setAll(lbl);
    }
    public BorderPane getRoot() {
        return root;
    }
   private void displayFilteredProducts(List<Product> allProducts, FlowPane pane, String search, String category) {
    pane.getChildren().clear();

    for (Product p : allProducts) {
        boolean matchSearch = p.getName().toLowerCase().contains(search);
        boolean matchCategory = category.equals("All Categories") || p.getCategory().equalsIgnoreCase(category);

        if (matchSearch && matchCategory) {
            VBox card = createProductCard(p);
            pane.getChildren().add(card);
        }
    }
}
private void showConfirmedOrders(String username) {
        contentPane.getChildren().clear();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f9f9f9;");
        List<ecommerce.Order> confirmedOrders = DatabaseManager.loadConfirmedOrders(username);
        if (confirmedOrders == null || confirmedOrders.isEmpty()) {
            Label noOrdersLabel = new Label("No confirmed orders found.");
            noOrdersLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
            root.getChildren().add(noOrdersLabel);
        } else {
            Map<Integer, List<ecommerce.Order>> groupedOrders = confirmedOrders.stream()
                .collect(Collectors.groupingBy(ecommerce.Order::getOrderId));
            for (Map.Entry<Integer, List<ecommerce.Order>> entry : groupedOrders.entrySet()) {
                int orderId = entry.getKey();
                List<ecommerce.Order> products = entry.getValue();
                double orderTotal = products.get(0).getTotalAmount();
                VBox orderBox = new VBox(10);
                orderBox.setPadding(new Insets(15));
                orderBox.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #cccccc;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
                );
                Label orderHeader = new Label("Order ID: " + orderId + "   |   Date: " + products.get(0).getOrderDate());
orderHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
Label statusLabel = new Label("Status: " + products.get(0).getStatus());
statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3b3b3b;");
orderBox.getChildren().addAll(orderHeader, statusLabel); // ✅ only add once!

                for (ecommerce.Order product : products) {
                    HBox productCard = new HBox(15);
                    productCard.setPadding(new Insets(10));
                    productCard.setStyle(
                        "-fx-background-color: #fdfdfd;" +
                        "-fx-border-color: #dddddd;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
                    );
                    VBox productDetails = new VBox(5);
                    productDetails.getChildren().addAll(
                        new Label("Product: " + product.getProductName()),
                        new Label("Quantity: " + product.getQuantity())
                    );
                    productDetails.getChildren().forEach(label -> {
                        ((Label) label).setStyle("-fx-font-size: 14px;");
                    });
                    productCard.getChildren().add(productDetails);
                    orderBox.getChildren().add(productCard);
                }
                Label totalLabel = new Label("Total Amount: Rs. " + orderTotal);
                totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                orderBox.getChildren().add(totalLabel);
                root.getChildren().add(orderBox);   }
        }
        Button refreshBtn = new Button("Refresh");
refreshBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #3498db; -fx-text-fill: white;");
refreshBtn.setOnAction(e -> showConfirmedOrders(username)); // Re-call itself

root.getChildren().add(0, refreshBtn); // Add at the top

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        contentPane.getChildren().add(scrollPane); // ✅ inject into existing layout
}
}