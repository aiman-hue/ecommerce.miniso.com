/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecommerce;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboard {

    // ============================================
    // CLASS VARIABLES
    // ============================================
    private ObservableList<String> products = FXCollections.observableArrayList();
    private ListView<String> productsList = new ListView<>();
    private BorderPane root;
    private final StackPane contentPane;
    private String adminUsername;
    private Stage primaryStage;

    // ============================================
    // CONSTRUCTOR
    // ============================================
    public AdminDashboard(Stage stage, String username) {
        this.adminUsername = username;
        this.primaryStage = stage;
        root = new BorderPane();
        contentPane = new StackPane();
        contentPane.setPadding(new Insets(20));
        contentPane.setId("admin-content-pane");
        
        setupSidebar(stage);
        showDashboardHome();
        root.setPrefSize(1200, 700);
        root.setId("admin-root");
    }

    // ============================================
    // SIDEBAR SETUP
    // ============================================
    private void setupSidebar(Stage stage) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #1a1a1a;");
        sidebar.setPrefWidth(220);
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setId("admin-sidebar");

        Label menuTitle = new Label("Admin Menu");
        menuTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        menuTitle.setId("admin-menu-title");

        Button homeBtn = new Button("🏠 Dashboard Home");
        Button categoriesBtn = new Button("📦 Manage Products");
        Button statusBtn = new Button("📋 Order Management");
        Button logoutBtn = new Button("🚪 Logout");

        for (Button btn : new Button[]{homeBtn, categoriesBtn, statusBtn, logoutBtn}) {
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.getStyleClass().add("admin-sidebar-btn");
        }

        // Button Actions
        homeBtn.setOnAction(e -> showDashboardHome());
        categoriesBtn.setOnAction(e -> showProductManagement(stage));
        statusBtn.setOnAction(e -> showOrderManagement());
        logoutBtn.setOnAction(e -> {
            LoginScreen login = new LoginScreen(primaryStage);
            primaryStage.getScene().setRoot(login.getRoot());
        });

        sidebar.getChildren().addAll(menuTitle, homeBtn, categoriesBtn, statusBtn, logoutBtn);
        root.setLeft(sidebar);
        root.setCenter(contentPane);
    }

    // ============================================
    // DASHBOARD HOME WITH ANALYTICS
    // ============================================
    private void showDashboardHome() {
        contentPane.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f2f5;");
        
        VBox dashboardBox = new VBox(20);
        dashboardBox.setPadding(new Insets(20));
        dashboardBox.setStyle("-fx-background-color: #f0f2f5;");
        
        // Welcome Header
        VBox welcomeBox = new VBox(5);
        welcomeBox.setAlignment(Pos.CENTER_LEFT);
        welcomeBox.setStyle("-fx-padding: 20; -fx-background-color: #e74c3c; -fx-border-radius: 15; -fx-background-radius: 15;");
        
        Label welcomeLabel = new Label("Welcome back, " + adminUsername + "!");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.8);");
        
        welcomeBox.getChildren().addAll(welcomeLabel, dateLabel);
        
        // Stats Cards Grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(15);
        statsGrid.setAlignment(Pos.CENTER);
        
        // Get real data
        List<Order> allOrders = DatabaseManager.getAllOrders();
        Map<Integer, List<Order>> groupedOrders = allOrders.stream().collect(Collectors.groupingBy(Order::getOrderId));
        List<Product> allProducts = DatabaseManager.loadProducts();
        List<String> categories = DatabaseManager.getCategories();
        
        double totalRevenue = 0;
        int completedOrders = 0;
        
        for (List<Order> orderItems : groupedOrders.values()) {
            totalRevenue += orderItems.get(0).getTotalAmount();
            if ("Completed".equals(orderItems.get(0).getStatus())) completedOrders++;
        }
        
        double avgOrderValue = groupedOrders.size() > 0 ? totalRevenue / groupedOrders.size() : 0;
        double todayRevenue = calculateTodayRevenue(allOrders);
        
        statsGrid.add(createStatCard("Total Revenue", String.format("Rs. %.2f", totalRevenue), "#27ae60", "💰"), 0, 0);
        statsGrid.add(createStatCard("Today's Sales", String.format("Rs. %.2f", todayRevenue), "#e74c3c", "📈"), 1, 0);
        statsGrid.add(createStatCard("Total Orders", String.valueOf(groupedOrders.size()), "#3498db", "📦"), 2, 0);
        statsGrid.add(createStatCard("Avg Order Value", String.format("Rs. %.2f", avgOrderValue), "#f39c12", "💵"), 3, 0);
        statsGrid.add(createStatCard("Total Products", String.valueOf(allProducts.size()), "#9b59b6", "🏷️"), 0, 1);
        statsGrid.add(createStatCard("Categories", String.valueOf(categories.size()), "#1abc9c", "📁"), 1, 1);
        statsGrid.add(createStatCard("Completed Orders", String.valueOf(completedOrders), "#2ecc71", "✅"), 2, 1);
        
        // Order Status Distribution
        VBox statusBox = createStatusDistributionBox(allOrders, groupedOrders.size());
        
        // Recent Orders Table
        VBox recentOrdersBox = createRecentOrdersTable(allOrders);
        
        dashboardBox.getChildren().addAll(welcomeBox, statsGrid, statusBox, recentOrdersBox);
        scrollPane.setContent(dashboardBox);
        
        contentPane.getChildren().setAll(scrollPane);
    }
    
    private VBox createStatCard(String title, String value, String color, String icon) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefWidth(220);
        
        HBox topRow = new HBox(5);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        
        topRow.getChildren().addAll(iconLabel, titleLabel);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(topRow, valueLabel);
        return card;
    }
    
    private double calculateTodayRevenue(List<Order> orders) {
        LocalDate today = LocalDate.now();
        return orders.stream()
            .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().equals(today))
            .mapToDouble(Order::getTotalAmount)
            .sum();
    }
    
    private VBox createStatusDistributionBox(List<Order> orders, int totalOrders) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        
        Label title = new Label("Order Status Distribution");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Map<String, Long> statusCount = orders.stream()
            .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        
        VBox statusList = new VBox(8);
        
        String[] statuses = {"Confirmed", "Dispatched", "Delivered", "Completed"};
        String[] colors = {"#f39c12", "#3498db", "#9b59b6", "#27ae60"};
        
        for (int i = 0; i < statuses.length; i++) {
            String status = statuses[i];
            long count = statusCount.getOrDefault(status, 0L);
            double percentage = totalOrders > 0 ? (count * 100.0) / totalOrders : 0;
            
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            
            Label statusLabel = new Label(status);
            statusLabel.setStyle("-fx-min-width: 90; -fx-font-weight: bold;");
            
            ProgressBar pb = new ProgressBar(percentage / 100);
            pb.setPrefWidth(200);
            pb.setStyle("-fx-accent: " + colors[i] + ";");
            
            Label countLabel = new Label(String.valueOf(count));
            countLabel.setStyle("-fx-min-width: 40; -fx-font-weight: bold; -fx-text-fill: " + colors[i] + ";");
            
            Label percentLabel = new Label(String.format("%.1f%%", percentage));
            percentLabel.setStyle("-fx-min-width: 50; -fx-text-fill: #7f8c8d;");
            
            row.getChildren().addAll(statusLabel, pb, countLabel, percentLabel);
            statusList.getChildren().add(row);
        }
        
        box.getChildren().addAll(title, statusList);
        return box;
    }
    
    private VBox createRecentOrdersTable(List<Order> orders) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        
        Label title = new Label("Recent Orders");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TableView<Map<String, Object>> table = new TableView<>();
        table.setPrefHeight(250);
        
        TableColumn<Map<String, Object>, String> orderIdCol = new TableColumn<>("Order ID");
        TableColumn<Map<String, Object>, String> customerCol = new TableColumn<>("Customer");
        TableColumn<Map<String, Object>, String> amountCol = new TableColumn<>("Amount");
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Date");
        
        orderIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().get("order_id").toString()));
        customerCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().get("customer").toString()));
        amountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.format("Rs. %.2f", c.getValue().get("amount"))));
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().get("status").toString()));
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().get("date").toString()));
        
        orderIdCol.setPrefWidth(80);
        customerCol.setPrefWidth(150);
        amountCol.setPrefWidth(100);
        statusCol.setPrefWidth(100);
        dateCol.setPrefWidth(150);
        
        table.getColumns().addAll(orderIdCol, customerCol, amountCol, statusCol, dateCol);
        
        ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList();
        
        Map<Integer, List<Order>> grouped = orders.stream().collect(Collectors.groupingBy(Order::getOrderId));
        grouped.entrySet().stream().limit(10).forEach(entry -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("order_id", entry.getKey());
            Order first = entry.getValue().get(0);
            row.put("customer", first.getFullName() != null ? first.getFullName() : "Guest");
            row.put("amount", first.getTotalAmount());
            row.put("status", first.getStatus());
            row.put("date", first.getOrderDate() != null ? first.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A");
            tableData.add(row);
        });
        
        table.setItems(tableData);
        box.getChildren().addAll(title, table);
        return box;
    }

    // ============================================
    // PRODUCT MANAGEMENT
    // ============================================
    private void showProductManagement(Stage stage) {
        VBox productContainer = new VBox();
        productContainer.setPadding(new Insets(10));
        
        Button backBtn = new Button("← Back to Dashboard");
        backBtn.getStyleClass().add("secondary-btn");
        backBtn.setOnAction(e -> showDashboardHome());
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setId("admin-tab-pane");

        Tab categoryTab = createCategoryTab();
        Tab productTab = createProductTab(stage);
        
        tabPane.getTabs().addAll(categoryTab, productTab);
        
        productContainer.getChildren().addAll(backBtn, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        contentPane.getChildren().clear();
        contentPane.getChildren().add(productContainer);
    }

    private Tab createCategoryTab() {
        VBox categoryManagementPane = new VBox(10);
        categoryManagementPane.setPadding(new Insets(20));
        categoryManagementPane.setId("category-management-pane");

        Label catLabel = new Label("Add New Category:");
        catLabel.getStyleClass().add("form-label");
        
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category Name");
        categoryField.getStyleClass().add("form-field");

        ListView<String> categoryList = new ListView<>();
        ObservableList<String> categories = FXCollections.observableArrayList(DatabaseManager.getCategories());
        categoryList.setItems(categories);
        categoryList.setId("category-list");

        Button addCategoryBtn = new Button("Add Category");
        addCategoryBtn.getStyleClass().add("primary-btn");
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
        deleteCategoryBtn.getStyleClass().add("danger-btn");
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
        categoryTab.setId("category-tab");
        
        return categoryTab;
    }

    private Tab createProductTab(Stage stage) {
        VBox productPane = new VBox(10);
        productPane.setPadding(new Insets(20));
        productPane.setId("product-management-pane");

        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");
        searchField.getStyleClass().add("search-field");

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Price: Low to High", "Price: High to Low", "Name: A-Z", "Name: Z-A");
        filterBox.setPromptText("Sort By");
        filterBox.getStyleClass().add("filter-combo");

        productsList.setItems(products);
        productsList.setId("products-list");
        loadProductsIntoList();

        filterBox.setOnAction(ev -> applyProductFilter(filterBox));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> 
            productsList.setItems(products.filtered(p -> p.toLowerCase().contains(newVal.toLowerCase())))
        );

        Button deleteProductBtn = new Button("Delete Selected Product");
        deleteProductBtn.getStyleClass().add("danger-btn");
        deleteProductBtn.setOnAction(ev -> deleteSelectedProduct());

        productPane.getChildren().addAll(searchField, filterBox, productsList, new Separator());

        String[] categoryArray = DatabaseManager.getCategories().toArray(new String[0]);
        setupAddProductForm(stage, productPane, categoryArray);
        productPane.getChildren().add(deleteProductBtn);

        Tab productTab = new Tab("Manage Products", productPane);
        productTab.setId("product-tab");
        
        return productTab;
    }

    private void loadProductsIntoList() {
        products.clear();
        List<Product> allProducts = DatabaseManager.loadProducts();
        for (Product product : allProducts) {
            products.add(product.getName() + " - Rs. " + product.getPrice());
        }
    }

    private void applyProductFilter(ComboBox<String> filterBox) {
        String selectedSort = filterBox.getValue();
        if (selectedSort != null) {
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
        }
    }

    private void deleteSelectedProduct() {
        String selected = productsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String productName = selected.split("-")[0].trim();
            DatabaseManager.deleteProduct(productName);
            products.remove(selected);
            productsList.setItems(products);
            showAlert(Alert.AlertType.INFORMATION, "Product deleted.");
        }
    }

    private void setupAddProductForm(Stage stage, VBox parentContainer, String[] SelectedCategories) {
        Label addProductLabel = new Label("Add Product");
        addProductLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        addProductLabel.setId("add-product-label");

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        nameField.getStyleClass().add("form-field");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.getStyleClass().add("form-field");

        TextField discountField = new TextField();
        discountField.setPromptText("Discount %");
        discountField.getStyleClass().add("form-field");

        TextField discountedPriceField = new TextField();
        discountedPriceField.setPromptText("Discounted Price");
        discountedPriceField.getStyleClass().add("form-field");

        TextField aboutField = new TextField();
        aboutField.setPromptText("About Product");
        aboutField.getStyleClass().add("form-field");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Select Category");
        categoryCombo.getItems().addAll(SelectedCategories);
        if (SelectedCategories != null && SelectedCategories.length > 0) {
            categoryCombo.setValue(SelectedCategories[SelectedCategories.length - 1]);
        }
        categoryCombo.setVisibleRowCount(20);
        categoryCombo.getStyleClass().add("form-field");

        Label imageLabel = new Label("No image selected");
        imageLabel.setStyle("-fx-text-fill: #555555;");
        imageLabel.setId("image-label");

        Button imageBtn = new Button("Choose Image");
        imageBtn.getStyleClass().add("secondary-btn");
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
        addProductBtn.getStyleClass().add("primary-btn");
        addProductBtn.setOnAction(ev -> addProductToDatabase(
            nameField, priceField, discountField, discountedPriceField,
            aboutField, categoryCombo, imageLabel, selectedImagePath
        ));

        VBox form = new VBox(10, addProductLabel, nameField, priceField, discountField,
            discountedPriceField, aboutField, categoryCombo, imageBtn, imageLabel, addProductBtn);

        form.setPadding(new Insets(20));
        form.setAlignment(Pos.TOP_LEFT);
        form.setStyle("-fx-background-color: white; -fx-border-color: #e74c3c; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");
        form.setMaxWidth(400);
        form.setId("add-product-form");

        parentContainer.getChildren().add(form);
    }

    private void addProductToDatabase(TextField nameField, TextField priceField, TextField discountField,
                                      TextField discountedPriceField, TextField aboutField,
                                      ComboBox<String> categoryCombo, Label imageLabel, String[] selectedImagePath) {
        try {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String discountText = discountField.getText().trim();
            String discountedPriceText = discountedPriceField.getText().trim();
            String selectedCategory = categoryCombo.getValue();
            String about = aboutField.getText().trim();
            String imagePath = selectedImagePath[0];

            if (name.isEmpty() || priceText.isEmpty() || discountText.isEmpty() || 
                discountedPriceText.isEmpty() || selectedCategory == null || imagePath == null) {
                showAlert(Alert.AlertType.WARNING, "Please fill all required fields.");
                return;
            }

            double price = Double.parseDouble(priceText);
            double discountPercent = Double.parseDouble(discountText);
            double discountedPrice = Double.parseDouble(discountedPriceText);

            Product p = new Product(name, price, selectedCategory, discountedPrice, (int) discountPercent, imagePath, about);
            ProductDataManager.addProduct(p);

            loadProductsIntoList();
            productsList.setItems(products);

            showAlert(Alert.AlertType.INFORMATION, "Product added.");
            
            nameField.clear();
            priceField.clear();
            discountField.clear();
            discountedPriceField.clear();
            aboutField.clear();
            categoryCombo.getSelectionModel().clearSelection();
            imageLabel.setText("No image selected");
            selectedImagePath[0] = null;

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid input. Please check your fields.");
        }
    }

    // ============================================
    // ORDER MANAGEMENT - ENHANCED WITH BEAUTIFUL CARDS
    // ============================================
    private void showOrderManagement() {
        VBox orderContainer = new VBox(15);
        orderContainer.setPadding(new Insets(15));
        orderContainer.setStyle("-fx-background-color: #f5f7fa;");
        
        // Top Bar with Back Button
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        
        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
        backBtn.setOnAction(e -> showDashboardHome());
        
        VBox headerBox = new VBox(5);
        Label heading = new Label("📋 Order Management");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label subheading = new Label("Manage and track customer orders");
        subheading.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        headerBox.getChildren().addAll(heading, subheading);
        
        topBar.getChildren().addAll(backBtn, headerBox);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        
        // Stats Summary Row
        List<Order> allOrders = DatabaseManager.getAllOrders();
        Map<Integer, List<Order>> groupedOrders = allOrders.stream().collect(Collectors.groupingBy(Order::getOrderId));
        
        int totalOrders = groupedOrders.size();
        int confirmedCount = 0, dispatchedCount = 0, deliveredCount = 0, completedCount = 0;
        
        for (List<Order> items : groupedOrders.values()) {
            String status = items.get(0).getStatus();
            switch (status) {
                case "Confirmed": confirmedCount++; break;
                case "Dispatched": dispatchedCount++; break;
                case "Delivered": deliveredCount++; break;
                case "Completed": completedCount++; break;
            }
        }
        
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10, 0, 20, 0));
        
        statsRow.getChildren().addAll(
            createStatBadge("Total Orders", String.valueOf(totalOrders), "#3498db", "📊"),
            createStatBadge("Confirmed", String.valueOf(confirmedCount), "#f39c12", "⏳"),
            createStatBadge("Dispatched", String.valueOf(dispatchedCount), "#3498db", "🚚"),
            createStatBadge("Delivered", String.valueOf(deliveredCount), "#9b59b6", "📦"),
            createStatBadge("Completed", String.valueOf(completedCount), "#27ae60", "✅")
        );
        
        // Orders List
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        VBox ordersList = new VBox(15);
        ordersList.setPadding(new Insets(5, 10, 10, 10));

        if (allOrders == null || allOrders.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15;");
            
            Label emptyLabel = new Label("📭 No orders found");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            
            Label emptySubLabel = new Label("When customers place orders, they will appear here");
            emptySubLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6;");
            
            emptyBox.getChildren().addAll(emptyLabel, emptySubLabel);
            ordersList.getChildren().add(emptyBox);
        } else {
            Map<Integer, List<Order>> grouped = allOrders.stream().collect(Collectors.groupingBy(Order::getOrderId));
            
            for (Map.Entry<Integer, List<Order>> entry : grouped.entrySet()) {
                int orderId = entry.getKey();
                List<Order> items = entry.getValue();
                Order first = items.get(0);
                
                VBox card = createBeautifulOrderCard(orderId, items, first);
                ordersList.getChildren().add(card);
            }
        }
        
        scrollPane.setContent(ordersList);
        
        orderContainer.getChildren().addAll(topBar, statsRow, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        contentPane.getChildren().clear();
        contentPane.getChildren().add(orderContainer);
    }
    
    private HBox createStatBadge(String label, String value, String color, String icon) {
        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(8, 15, 8, 15));
        badge.setStyle("-fx-background-color: white; -fx-border-radius: 25; -fx-background-radius: 25; -fx-border-color: " + color + "; -fx-border-width: 1.5;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        VBox textBox = new VBox(2);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.getChildren().addAll(valueLabel, labelLabel);
        
        badge.getChildren().addAll(iconLabel, textBox);
        return badge;
    }

   private VBox createBeautifulOrderCard(int orderId, List<Order> items, Order first) {
    VBox card = new VBox(12);
    card.setPadding(new Insets(15));
    card.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
    
    // Header Row - Order ID and Status
    HBox headerRow = new HBox(10);
    headerRow.setAlignment(Pos.CENTER_LEFT);
    
    Label orderIdLabel = new Label("#" + orderId);
    orderIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
    
    String status = first.getStatus();
    String statusColor, statusIcon;
    switch (status) {
        case "Confirmed": statusColor = "#f39c12"; statusIcon = "⏳"; break;
        case "Dispatched": statusColor = "#3498db"; statusIcon = "🚚"; break;
        case "Delivered": statusColor = "#9b59b6"; statusIcon = "📦"; break;
        case "Completed": statusColor = "#27ae60"; statusIcon = "✅"; break;
        default: statusColor = "#95a5a6"; statusIcon = "❓";
    }
    
    Label statusBadge = new Label(statusIcon + " " + status);
    statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 12 5 12; -fx-border-radius: 20; -fx-background-radius: 20;");
    
    Label dateLabel = new Label(first.getOrderDate() != null ? first.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "N/A");
    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    headerRow.getChildren().addAll(orderIdLabel, statusBadge, spacer, dateLabel);
    
    // Customer Info Row - NOW WITH CONTACT
    VBox customerInfoBox = new VBox(3);
    customerInfoBox.setPadding(new Insets(5, 0, 5, 0));
    
    String customerName = (first.getFullName() != null && !first.getFullName().equals("N/A") && !first.getFullName().isEmpty()) 
        ? first.getFullName() : "Guest Customer";
    Label customerNameLabel = new Label("👤 " + customerName);
    customerNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
    
    String contactNumber = (first.getContact() != null && !first.getContact().equals("N/A") && !first.getContact().isEmpty()) 
        ? first.getContact() : "No contact provided";
    Label customerContactLabel = new Label("📞 " + contactNumber);
    customerContactLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
    
    String location = (first.getCity() != null && !first.getCity().equals("N/A") ? first.getCity() : "N/A") 
        + ", " + (first.getCountry() != null && !first.getCountry().equals("N/A") ? first.getCountry() : "N/A");
    Label locationLabel = new Label("📍 " + location);
    locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
    
    customerInfoBox.getChildren().addAll(customerNameLabel, customerContactLabel, locationLabel);
    
    // Products Section
    VBox productsBox = new VBox(8);
    productsBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12;");
    
    Label productsTitle = new Label("🛍️ Order Items");
    productsTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
    productsBox.getChildren().add(productsTitle);
    
    for (Order o : items) {
        HBox productRow = new HBox(10);
        productRow.setAlignment(Pos.CENTER_LEFT);
        
        Label qtyBadge = new Label("x" + o.getQuantity());
        qtyBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8 2 8; -fx-border-radius: 12; -fx-background-radius: 12; -fx-min-width: 35; -fx-alignment: center;");
        
        Label productName = new Label(o.getProductName());
        productName.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        productRow.getChildren().addAll(qtyBadge, productName);
        productsBox.getChildren().add(productRow);
    }
    
    // Amount Row
    HBox amountRow = new HBox(10);
    amountRow.setAlignment(Pos.CENTER_RIGHT);
    amountRow.setPadding(new Insets(5, 0, 0, 0));
    
    Label amountLabel = new Label("Total Amount:");
    amountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
    
    Label amountValue = new Label(String.format("Rs. %.2f", first.getTotalAmount()));
    amountValue.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
    
    amountRow.getChildren().addAll(amountLabel, amountValue);
    
    // Address Row
    VBox addressBox = new VBox(5);
    addressBox.setStyle("-fx-padding: 8 0 0 0;");
    
    if (first.getAddress() != null && !first.getAddress().equals("N/A") && !first.getAddress().isEmpty()) {
        Label addressIcon = new Label("🏠 Delivery Address:");
        addressIcon.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        
        Label addressText = new Label(first.getAddress());
        addressText.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-wrap-text: true;");
        
        addressBox.getChildren().addAll(addressIcon, addressText);
    }
    
    // Action Buttons
    HBox actionRow = new HBox(15);
    actionRow.setAlignment(Pos.CENTER_RIGHT);
    actionRow.setPadding(new Insets(10, 0, 5, 0));
    
    Button updateStatusBtn = new Button();
    updateStatusBtn.setStyle("-fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
    
    switch (status) {
        case "Confirmed":
            updateStatusBtn.setText("🚚 Mark as Dispatched");
            updateStatusBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
            break;
        case "Dispatched":
            updateStatusBtn.setText("📦 Mark as Delivered");
            updateStatusBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
            break;
        case "Delivered":
            updateStatusBtn.setText("✅ Mark as Completed");
            updateStatusBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
            break;
        case "Completed":
            updateStatusBtn.setText("✓ Completed");
            updateStatusBtn.setDisable(true);
            updateStatusBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-opacity: 0.6;");
            break;
    }
    
    Button viewDetailsBtn = new Button("📄 View Details");
    viewDetailsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #3498db; -fx-text-fill: #3498db; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
    viewDetailsBtn.setOnAction(e -> showOrderDetails(orderId, items, first));
    
    if (!"Completed".equals(status)) {
        actionRow.getChildren().addAll(viewDetailsBtn, updateStatusBtn);
    } else {
        actionRow.getChildren().add(viewDetailsBtn);
    }
    
    updateStatusBtn.setOnAction(ev -> updateOrderStatus(orderId, items, first, updateStatusBtn));
    
    card.getChildren().addAll(headerRow, customerInfoBox, productsBox, amountRow, addressBox, actionRow);
    return card;
}
   
    private void showOrderDetails(int orderId, List<Order> items, Order first) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details - #" + orderId);
        dialog.getDialogPane().setPrefWidth(500);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label title = new Label("Order #" + orderId);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        // Status
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: bold;");
        Label statusValue = new Label(first.getStatus());
        statusValue.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        statusRow.getChildren().addAll(statusLabel, statusValue);
        
        // Customer Info
        VBox customerBox = new VBox(5);
        customerBox.setStyle("-fx-padding: 10 0;");
        customerBox.getChildren().addAll(
            new Label("👤 Customer Information:"),
            new Label("  Name: " + (first.getFullName() != null ? first.getFullName() : "N/A")),
            new Label("  Contact: " + (first.getContact() != null ? first.getContact() : "N/A"))
        );
        
        // Address
        VBox addressBox = new VBox(5);
        addressBox.setStyle("-fx-padding: 10 0;");
        addressBox.getChildren().addAll(
            new Label("📍 Shipping Address:"),
            new Label("  " + (first.getAddress() != null ? first.getAddress() : "N/A")),
            new Label("  " + (first.getCity() != null ? first.getCity() : "N/A") + ", " + (first.getCountry() != null ? first.getCountry() : "N/A"))
        );
        
        // Products
        VBox productsBox = new VBox(5);
        productsBox.setStyle("-fx-padding: 10 0;");
        Label productsTitle = new Label("🛍️ Products:");
        productsTitle.setStyle("-fx-font-weight: bold;");
        productsBox.getChildren().add(productsTitle);
        for (Order o : items) {
            productsBox.getChildren().add(new Label("  • " + o.getQuantity() + "x " + o.getProductName()));
        }
        
        // Total
        HBox totalRow = new HBox(10);
        totalRow.setAlignment(Pos.CENTER_RIGHT);
        totalRow.setPadding(new Insets(10, 0, 0, 0));
        Label totalLabel = new Label("Total:");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label totalValue = new Label(String.format("Rs. %.2f", first.getTotalAmount()));
        totalValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        totalRow.getChildren().addAll(totalLabel, totalValue);
        
        content.getChildren().addAll(title, statusRow, customerBox, addressBox, productsBox, totalRow);
        
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        dialog.getDialogPane().setContent(content);
        
        dialog.showAndWait();
    }

    private void updateOrderStatus(int orderId, List<Order> items, Order first, Button updateStatusBtn) {
        String newStatus = null;
        String oldStatus = first.getStatus();
        
        switch (oldStatus) {
            case "Confirmed": newStatus = "Dispatched"; break;
            case "Dispatched": newStatus = "Delivered"; break;
            case "Delivered": newStatus = "Completed"; break;
            default:
                showAlert(Alert.AlertType.INFORMATION, "Status update not available.");
                return;
        }

        if (newStatus != null) {
            boolean updated = DatabaseManager.updateOrderStatus(orderId, newStatus);
            if (updated) {
                final String statusToSet = newStatus;
                items.forEach(o -> o.setStatus(statusToSet));
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("✅ Order #" + orderId + " updated from " + oldStatus + " to " + newStatus);
                alert.showAndWait();
                
                showOrderManagement();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to update order status.");
            }
        }
    }

    // ============================================
    // UTILITY METHODS
    // ============================================
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateButtonLabel(Button button, String status) {
        button.setDisable(false);
        button.getStyleClass().removeAll("status-pending", "status-confirmed", "status-dispatched", "status-delivered", "status-completed");
        button.getStyleClass().add("status-update-btn");

        switch (status) {
            case "Pending":
            case "Confirmed":
                button.setText("Mark Dispatched");
                button.getStyleClass().add("status-confirmed");
                break;
            case "Dispatched":
                button.setText("Mark Delivered");
                button.getStyleClass().add("status-dispatched");
                break;
            case "Delivered":
                button.setText("Mark Completed");
                button.getStyleClass().add("status-delivered");
                break;
            case "Completed":
                button.setText("✓ Completed");
                button.setDisable(true);
                button.getStyleClass().add("status-completed");
                break;
            default:
                button.setText("Unknown (" + status + ")");
                button.setDisable(true);
                button.getStyleClass().add("status-unknown");
        }
    }

    // ============================================
    // GETTER
    // ============================================
    public BorderPane getRoot() {
        return root;
    }
}