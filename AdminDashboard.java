/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecommerce;
import ecommerce.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboard {

    private ObservableList<String> products = FXCollections.observableArrayList();
    private ListView<String> productsList = new ListView<>();
    private BorderPane root;
    private final StackPane contentPane;
    private int id;

    public AdminDashboard(Stage stage, String username) {
        root = new BorderPane();
        contentPane = new StackPane();
        contentPane.setPadding(new Insets(20));
        showWelcomeMessage();

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #1a1a1a;");

        sidebar.setPrefWidth(220);
        sidebar.setAlignment(Pos.TOP_LEFT);

        Label menuTitle = new Label("Admin Menu");
        menuTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button categoriesBtn = new Button("Manage");
        Button statusBtn = new Button("Status");
        Button logoutBtn = new Button("Logout");

        for (Button btn : new Button[]{categoriesBtn, statusBtn, logoutBtn}) {
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        }

        sidebar.getChildren().addAll(menuTitle, categoriesBtn, statusBtn, logoutBtn);
        root.setLeft(sidebar);
        root.setCenter(contentPane);
        root.setPrefSize(1000, 600);

        categoriesBtn.setOnAction(e -> {
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // CATEGORY TAB
            VBox categoryManagementPane = new VBox(10);
            categoryManagementPane.setPadding(new Insets(20));

            Label catLabel = new Label("Add New Category:");
            TextField categoryField = new TextField();
            categoryField.setPromptText("Category Name");

            ListView<String> categoryList = new ListView<>();
            ObservableList<String> categories = FXCollections.observableArrayList(DatabaseManager.getCategories());
            categoryList.setItems(categories);

            Button addCategoryBtn = new Button("Add Category");
            addCategoryBtn.setOnAction(ev -> {
                String newCat = categoryField.getText().trim();
                if (!newCat.isEmpty() && !categories.contains(newCat)) {
                    DatabaseManager.addCategory(newCat);
                    categories.add(newCat);
                    categoryField.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Category added.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Invalid or duplicate category.");
                }
            });

            Button deleteCategoryBtn = new Button("Delete Selected Category");
            deleteCategoryBtn.setOnAction(ev -> {
                String selected = categoryList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    DatabaseManager.deleteCategory(selected);
                    categories.remove(selected);
                    showAlert(Alert.AlertType.INFORMATION, "Category deleted.");
                }
            });

            categoryManagementPane.getChildren().addAll(catLabel, categoryField, addCategoryBtn, categoryList, deleteCategoryBtn);
            Tab categoryTab = new Tab("Manage Categories", categoryManagementPane);

            VBox productPane = new VBox(10);
            productPane.setPadding(new Insets(20));

            TextField searchField = new TextField();
            searchField.setPromptText("Search product...");

            ComboBox<String> filterBox = new ComboBox<>();
            filterBox.getItems().addAll("Price: Low to High", "Price: High to Low", "Name: A-Z", "Name: Z-A");
            filterBox.setPromptText("Sort By");

            productsList.setItems(products);
            List<Product> allProducts = DatabaseManager.loadProducts();
            products.clear();
            allProducts.forEach((product) -> {
             products.add(product.getName() + " - Rs. " + product.getPrice());
            
            });


            filterBox.setOnAction(ev -> {
                String selectedSort = filterBox.getValue();
                if (selectedSort.equals("Price: Low to High")) {
                    products.sort(Comparator.comparingDouble(p -> {
                        try {
                            return Double.parseDouble(p.split("Rs.")[1].trim());
                        } catch (Exception ex) {
                            return Double.MAX_VALUE;
                        }
                    }));
                } else if (selectedSort.equals("Price: High to Low")) {
                    products.sort((a, b) -> {
                        try {
                            return Double.compare(
                                Double.parseDouble(b.split("Rs.")[1].trim()),
                                Double.parseDouble(a.split("Rs.")[1].trim()));
                        } catch (Exception ex) {
                            return 0;
                        }
                    });
                } else if (selectedSort.equals("Name: A-Z")) {
                    FXCollections.sort(products);
                } else if (selectedSort.equals("Name: Z-A")) {
                    FXCollections.sort(products, Comparator.reverseOrder());
                }
                productsList.setItems(products);
            });

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                productsList.setItems(products.filtered(p -> p.toLowerCase().contains(newVal.toLowerCase())));
            });

            Button deleteProductBtn = new Button("Delete Selected Product");
            deleteProductBtn.setOnAction(ev -> {
                String selected = productsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String productName = selected.split("-")[0].trim();
                    DatabaseManager.deleteProduct(productName);
                    products.remove(selected);
                    productsList.setItems(products);
                    showAlert(Alert.AlertType.INFORMATION, "Product deleted.");
                }
            });

            productPane.getChildren().addAll(searchField, filterBox, productsList, new Separator());

            String[] categoryArray = DatabaseManager.getCategories().toArray(new String[0]);
            setupAddProductForm(stage, productPane, categoryArray);

            productPane.getChildren().add(deleteProductBtn);

            Tab productTab = new Tab("Manage Products", productPane);
            tabPane.getTabs().addAll(categoryTab, productTab);
            root.setCenter(tabPane);
        });

       


statusBtn.setOnAction(e -> {
    VBox contentPane = new VBox(10);
    contentPane.setPadding(new Insets(20));
    Label heading = new Label("Manage Order Status");
    heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);

    VBox ordersList = new VBox(10);
    ordersList.setPadding(new Insets(10));

    List<Order> orders = DatabaseManager.getAllOrders();

    if (orders == null || orders.isEmpty()) {
        showAlert(Alert.AlertType.INFORMATION, "No orders found.");
        return;
    }
    Map<Integer, List<Order>> grouped = orders.stream().collect(Collectors.groupingBy(Order::getOrderId));

    for (Map.Entry<Integer, List<Order>> entry : grouped.entrySet()) {
        int orderId = entry.getKey();
        List<Order> items = entry.getValue();

        Order first = items.get(0);

        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-border-radius: 6; -fx-background-radius: 6;");

        StringBuilder productsInfo = new StringBuilder();
        for (Order o : items) {
    productsInfo.append(String.format("%dx %s\n", o.getQuantity(), o.getProductName()));
}


        String dateStr = (first.getOrderDate() != null) ? 
            first.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";

        Label info = new Label(String.format(
            "Order #%d\nProducts:\n%sAmount: Rs. %.2f\nStatus: %s\nDate: %s\nUser: %s\nAddress: %s, %s, %s",
            orderId,
            productsInfo,
            first.getTotalAmount(),
            first.getStatus(),
            dateStr,
            first.getFullName() == null ? "N/A" : first.getFullName(),
            first.getAddress() == null ? "N/A" : first.getAddress(),
            first.getCity() == null ? "N/A" : first.getCity(),
            first.getCountry() == null ? "N/A" : first.getCountry()
        ));

        info.setStyle("-fx-font-size: 14px;");

        Button updateStatusBtn = new Button();
        updateStatusBtn.setMaxWidth(Double.MAX_VALUE);

        updateButtonLabel(updateStatusBtn, first.getStatus());

        updateStatusBtn.setOnAction(ev -> {
            String newStatus = null;
            switch (first.getStatus()) {
                case "Confirmed":
                    newStatus = "Dispatched";
                    break;
                case "Dispatched":
                    newStatus = "Delivered";
                    break;
                case "Delivered":
                    newStatus = "Completed";
                    break;
                default:
                    showAlert(Alert.AlertType.INFORMATION, "Status update not available.");
                    return;
            }

            if (newStatus != null) {
                boolean updated = false;
                try {
                    updated = DatabaseManager.updateOrderStatus(orderId, newStatus);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database error occurred.");
                }

                if (updated) {
                    final String statusToSet = newStatus;
                    items.forEach(o -> o.setStatus(statusToSet));
                    showAlert(Alert.AlertType.INFORMATION, "Order #" + orderId + " updated to " + newStatus);
                    updateStatusBtn.setDisable(true); // disable to avoid rapid re-fire
                    statusBtn.fire(); // refresh UI
                    updateStatusBtn.setDisable(false);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed to update order status.");
                }
            }
        });

        card.getChildren().addAll(info, updateStatusBtn);
        ordersList.getChildren().add(card);
    }
    scrollPane.setContent(ordersList);
    contentPane.getChildren().addAll(heading, scrollPane);
    root.setCenter(contentPane);
});

        logoutBtn.setOnAction(e -> {
            LoginScreen login = new LoginScreen(stage);
            stage.getScene().setRoot(login.getRoot());  // <-- just swap the root node

        });
    }


    private void setupAddProductForm(Stage stage, VBox parentContainer, String[] SelectedCategories) {
        Label addProductLabel = new Label("Add Product");

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        TextField discountField = new TextField();
        discountField.setPromptText("Discount %");

        TextField discountedPriceField = new TextField();
        discountedPriceField.setPromptText("Discounted Price");

        TextField aboutField = new TextField();
        aboutField.setPromptText("About Product");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Select Category");
        categoryCombo.getItems().addAll(SelectedCategories);
        if (SelectedCategories != null && SelectedCategories.length > 0) {
            categoryCombo.setValue(SelectedCategories[SelectedCategories.length - 1]);
        }
        categoryCombo.setVisibleRowCount(20);

        Label imageLabel = new Label("No image selected");
        Button imageBtn = new Button("Choose Image");
        final String[] selectedImagePath = {null};

        imageBtn.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImagePath[0] = file.getAbsolutePath();
                imageLabel.setText("Selected: " + file.getName());
            }
        });

        Button addProductBtn = new Button("Add Product");
        addProductBtn.setOnAction(ev -> {
            try {
                String name = nameField.getText().trim();
                String priceText = priceField.getText().trim();
                String discountText = discountField.getText().trim();
                String discountedPriceText = discountedPriceField.getText().trim();
                String selectedCategory = categoryCombo.getValue();
                String about = aboutField.getText().trim();
                String imagePath = selectedImagePath[0];

                if (name.isEmpty() || priceText.isEmpty() || discountText.isEmpty() || discountedPriceText.isEmpty() || selectedCategory == null || imagePath == null) {
                    showAlert(Alert.AlertType.WARNING, "Please fill all required fields.");
                    return;
                }

                double price = Double.parseDouble(priceText);
                double discountPercent = Double.parseDouble(discountText);
                double discountedPrice = Double.parseDouble(discountedPriceText);

               Product p = new Product(name, price, selectedCategory, discountedPrice, (int)discountPercent, imagePath, about);
               ProductDataManager.addProduct(p);

                ObservableList<String> products = FXCollections.observableArrayList();
                List<Product> allProducts = DatabaseManager.loadProducts();

                products.clear(); // Clear old items
                allProducts.forEach((product) -> {
                products.add(product.getName() + " - Rs. " + p.getPrice());
                });

                productsList.setItems(products); // Assuming productsList is a ListView<String>


                showAlert(Alert.AlertType.INFORMATION, "Product added.");
                nameField.clear(); priceField.clear(); discountField.clear();
                discountedPriceField.clear(); aboutField.clear();
                categoryCombo.getSelectionModel().clearSelection();
                imageLabel.setText("No image selected"); selectedImagePath[0] = null;

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid input. Please check your fields.");
            }
        });
        VBox form = new VBox(10, addProductLabel, nameField, priceField, discountField,
                discountedPriceField, aboutField, categoryCombo, imageBtn, imageLabel, addProductBtn);
        form.setPadding(new Insets(10));
        form.setAlignment(Pos.TOP_LEFT);
        parentContainer.getChildren().add(form);
    }
   private void showWelcomeMessage() {
    Label welcome = new Label("Welcome to Admin Dashboard!");
    welcome.setStyle(
        "-fx-font-size: 24px;" +
        "-fx-font-weight: bold;" +
        "-fx-text-fill: white;" +
        "-fx-background-color: #e74c3c;" +           // Red MINISO-style background
        "-fx-padding: 15 30 15 30;" +                 // Padding inside label
        "-fx-background-radius: 12;" +                // Rounded corners
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);"
    );

    contentPane.setAlignment(Pos.CENTER); // Center the message in the VBox
    contentPane.getChildren().setAll(welcome);
}


    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public BorderPane getRoot() {
        return root;
    }

   // This sets the status button text and enables/disables based on current status
private void updateButtonLabel(Button button, String status) {
    button.setDisable(false);

    switch (status) {
        case "Pending":
        case "Confirmed":
            button.setText("Mark Dispatched");
            break;
        case "Dispatched":
            button.setText("Mark Delivered");
            break;
        case "Delivered":
            button.setText("Mark Completed");
            break;
        case "Completed":
            button.setText("✓ Completed");
            button.setDisable(true); // Disable button when status is final
            break;
        default:
            button.setText("Unknown (" + status + ")");
            button.setDisable(true);
    }
}

// Optional helper if you want to update based on an Order object directly
private void updateButtonTextAndEnable(Button button, Order order) {
    updateButtonLabel(button, order.getStatus());
}
}