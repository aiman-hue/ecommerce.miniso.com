package ecommerce;

// Remove any existing iText imports and add these:
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        sidebar.setStyle("-fx-background-color: #1a1a1a; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 4, 0);");
        sidebar.setPrefWidth(220);
        sidebar.setAlignment(Pos.TOP_LEFT);

        Label menuTitle = new Label("MINISO");
        menuTitle.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 20 0;");
        
        Label userLabel = new Label("👤 " + username);
        userLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-padding: 0 0 15 0;");

        Button homeBtn = createSidebarButton("🏠 Home");
        Button ordersBtn = createSidebarButton("📦 My Orders");
        Button cartBtn = createSidebarButton("🛒 Cart");
        Button wishlistBtn = createSidebarButton("❤️ Wishlist");
        Button settingsBtn = createSidebarButton("⚙️ Settings");
        Button invoicesBtn = createSidebarButton("📄 Invoices");
        Button logoutBtn = createSidebarButton("🚪 Logout");

        sidebar.getChildren().addAll(menuTitle, userLabel, homeBtn, ordersBtn, cartBtn, wishlistBtn, settingsBtn, invoicesBtn, logoutBtn);

        contentPane = new StackPane();
        contentPane.setPadding(new Insets(20));
        contentPane.setStyle("-fx-background-color: #f5f7fa;");
        showHome();

        homeBtn.setOnAction(e -> showHome());
        ordersBtn.setOnAction(e -> showConfirmedOrders(username));
        cartBtn.setOnAction(e -> showCart());
        wishlistBtn.setOnAction(e -> showWishlist());
        settingsBtn.setOnAction(e -> showSettings());
        invoicesBtn.setOnAction(e -> showInvoices());
        logoutBtn.setOnAction(e -> {
            LoginScreen login = new LoginScreen(stage);
            stage.getScene().setRoot(login.getRoot());
        });

        root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentPane);
        root.setPrefSize(1200, 700);
        root.setStyle("-fx-background-color: #f5f7fa;");
    }
    
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e0; -fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 20 12 20; -fx-cursor: hand; -fx-border-radius: 12; -fx-background-radius: 12;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(231, 76, 60, 0.2); -fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 20 12 20; -fx-cursor: hand; -fx-border-radius: 12; -fx-background-radius: 12;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e0; -fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 20 12 20; -fx-cursor: hand; -fx-border-radius: 12; -fx-background-radius: 12;"));
        return btn;
    }

    // INVOICE GENERATION METHOD WITH SHIPPING FEE
    private void generateInvoice(int orderId, String customerName, List<Product> items, double subtotal, double shippingFee, double totalAmount, String paymentMode, String shippingAddress) {
        try {
            File invoiceDir = new File("invoices");
            if (!invoiceDir.exists()) {
                invoiceDir.mkdirs();
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "invoice_" + orderId + "_" + timestamp + ".pdf";
            String filePath = invoiceDir.getAbsolutePath() + File.separator + fileName;
            
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            
            Paragraph header = new Paragraph("MINISO E-COMMERCE STORE", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            
            Paragraph subheader = new Paragraph("Official Tax Invoice", headerFont);
            subheader.setAlignment(Element.ALIGN_CENTER);
            document.add(subheader);
            
            document.add(new Paragraph(" "));
            
            document.add(new Paragraph("Invoice #: " + orderId, normalFont));
            document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont));
            document.add(new Paragraph("Customer: " + customerName, normalFont));
            document.add(new Paragraph("Payment Mode: " + paymentMode, normalFont));
            document.add(new Paragraph("Shipping Address: " + shippingAddress, normalFont));
            
            document.add(new Paragraph(" "));
            
            // Products Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            
            PdfPCell cell1 = new PdfPCell(new Phrase("Product", headerFont));
            PdfPCell cell2 = new PdfPCell(new Phrase("Quantity", headerFont));
            PdfPCell cell3 = new PdfPCell(new Phrase("Unit Price", headerFont));
            PdfPCell cell4 = new PdfPCell(new Phrase("Total", headerFont));
            
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            
            for (Product p : items) {
                table.addCell(new PdfPCell(new Phrase(p.getName(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(p.getQuantity()), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("Rs. %.2f", p.getDiscountedPrice()), normalFont)));
                double itemTotal = p.getDiscountedPrice() * p.getQuantity();
                table.addCell(new PdfPCell(new Phrase(String.format("Rs. %.2f", itemTotal), normalFont)));
            }
            
            document.add(table);
            
            document.add(new Paragraph(" "));
            
            // Order Summary with Shipping Fee
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            // Subtotal
            PdfPCell subtotalLabelCell = new PdfPCell(new Phrase("Subtotal:", normalFont));
            subtotalLabelCell.setBorder(PdfPCell.NO_BORDER);
            subtotalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            PdfPCell subtotalValueCell = new PdfPCell(new Phrase(String.format("Rs. %.2f", subtotal), normalFont));
            subtotalValueCell.setBorder(PdfPCell.NO_BORDER);
            subtotalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            // Shipping Fee
            PdfPCell shippingLabelCell = new PdfPCell(new Phrase("Shipping Fee:", normalFont));
            shippingLabelCell.setBorder(PdfPCell.NO_BORDER);
            shippingLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            PdfPCell shippingValueCell = new PdfPCell(new Phrase(String.format("Rs. %.2f", shippingFee), boldFont));
            shippingValueCell.setBorder(PdfPCell.NO_BORDER);
            shippingValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            // Total
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL AMOUNT:", headerFont));
            totalLabelCell.setBorder(PdfPCell.NO_BORDER);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabelCell.setPaddingTop(10f);
            
            PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("Rs. %.2f", totalAmount), headerFont));
            totalValueCell.setBorder(PdfPCell.NO_BORDER);
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValueCell.setPaddingTop(10f);
            
            summaryTable.addCell(subtotalLabelCell);
            summaryTable.addCell(subtotalValueCell);
            summaryTable.addCell(shippingLabelCell);
            summaryTable.addCell(shippingValueCell);
            summaryTable.addCell(totalLabelCell);
            summaryTable.addCell(totalValueCell);
            
            document.add(summaryTable);
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            
            Paragraph footer = new Paragraph("Thank you for shopping with MINISO!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            Paragraph contact = new Paragraph("For any queries, contact: support@miniso.com", normalFont);
            contact.setAlignment(Element.ALIGN_CENTER);
            document.add(contact);
            
            document.close();
            
            DatabaseManager.saveInvoicePath(orderId, filePath);
            
            showAlert(Alert.AlertType.INFORMATION, "Invoice Generated", 
                "Invoice saved to: " + filePath + "\nYou can download it from 'My Invoices' section.");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Invoice Error", "Failed to generate invoice: " + e.getMessage());
        }
    }
    
    private void showInvoices() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_CENTER);
        
        Label title = new Label("📄 My Invoices");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 20 0;");
        
        List<Map<String, Object>> invoices = DatabaseManager.getUserInvoices(username);
        
        if (invoices == null || invoices.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
            
            Label emptyLabel = new Label("📭 No invoices found");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            
            Label emptySubLabel = new Label("Place an order to generate an invoice");
            emptySubLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6;");
            
            emptyBox.getChildren().addAll(emptyLabel, emptySubLabel);
            box.getChildren().addAll(title, emptyBox);
        } else {
            VBox invoicesList = new VBox(10);
            
            for (Map<String, Object> invoice : invoices) {
                VBox invoiceCard = new VBox(8);
                invoiceCard.setPadding(new Insets(15));
                invoiceCard.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-color: #e74c3c; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
                
                int orderId = (int) invoice.get("order_id");
                String orderDate = invoice.get("order_date").toString();
                double totalAmount = (double) invoice.get("total_amount");
                String status = (String) invoice.get("status");
                String invoicePath = (String) invoice.get("invoice_path");
                
                String statusColor;
                switch(status) {
                    case "Completed": statusColor = "#27ae60"; break;
                    case "Confirmed": statusColor = "#f39c12"; break;
                    case "Dispatched": statusColor = "#3498db"; break;
                    default: statusColor = "#95a5a6";
                }
                
                Label statusBadge = new Label(status);
                statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12 4 12; -fx-border-radius: 20; -fx-background-radius: 20;");
                
                HBox headerRow = new HBox(10);
                headerRow.setAlignment(Pos.CENTER_LEFT);
                Label orderInfo = new Label(String.format("Order #%d", orderId));
                orderInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                headerRow.getChildren().addAll(orderInfo, statusBadge);
                
                Label dateLabel = new Label("📅 " + orderDate);
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                
                Label amountLabel = new Label(String.format("Total: Rs. %.2f", totalAmount));
                amountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
                
                Button downloadBtn = new Button("📥 Download Invoice");
                downloadBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
                
                if (invoicePath != null && !invoicePath.isEmpty()) {
                    File invoiceFile = new File(invoicePath);
                    if (invoiceFile.exists()) {
                        downloadBtn.setOnAction(e -> {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setInitialFileName("invoice_" + orderId + ".pdf");
                            File saveFile = fileChooser.showSaveDialog(stage);
                            if (saveFile != null) {
                                try {
                                    java.nio.file.Files.copy(invoiceFile.toPath(), saveFile.toPath(), 
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                    showAlert(Alert.AlertType.INFORMATION, "Download Complete", 
                                        "Invoice saved to: " + saveFile.getAbsolutePath());
                                } catch (Exception ex) {
                                    showAlert(Alert.AlertType.ERROR, "Download Failed", ex.getMessage());
                                }
                            }
                        });
                    } else {
                        downloadBtn.setText("❌ Invoice Missing");
                        downloadBtn.setDisable(true);
                        downloadBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25;");
                    }
                } else {
                    downloadBtn.setText("⚠️ No Invoice Available");
                    downloadBtn.setDisable(true);
                    downloadBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25;");
                }
                
                invoiceCard.getChildren().addAll(headerRow, dateLabel, amountLabel, downloadBtn);
                invoicesList.getChildren().add(invoiceCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(invoicesList);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            box.getChildren().addAll(title, scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
        }
        
        contentPane.getChildren().setAll(box);
    }

    private void showHome() {
        VBox homeBox = new VBox(20);
        homeBox.setPadding(new Insets(20));
        homeBox.setAlignment(Pos.TOP_CENTER);
        homeBox.setStyle("-fx-background-color: #f5f7fa;");

        Label heading = new Label("MINISO");
        heading.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        Label tagline = new Label("Discover amazing products at best prices");
        tagline.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 20 0;");

        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setStyle("-fx-padding: 0 0 20 0;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search products...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: white; -fx-border-radius: 50; -fx-background-radius: 50; -fx-padding: 12 20 12 20; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-font-size: 14px;");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().add("All Categories");
        categoryBox.getItems().addAll(DatabaseManager.loadCategories());
        categoryBox.getSelectionModel().selectFirst();
        categoryBox.setStyle("-fx-background-color: white; -fx-border-radius: 50; -fx-background-radius: 50; -fx-padding: 8 15 8 15; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-font-size: 14px; -fx-min-width: 150;");

        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 50; -fx-background-radius: 50; -fx-cursor: hand;");
        searchBtn.setOnMouseEntered(e -> searchBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 50; -fx-background-radius: 50; -fx-cursor: hand;"));
        searchBtn.setOnMouseExited(e -> searchBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 50; -fx-background-radius: 50; -fx-cursor: hand;"));

        searchContainer.getChildren().addAll(searchField, categoryBox, searchBtn);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        FlowPane productPane = new FlowPane();
        productPane.setHgap(25);
        productPane.setVgap(25);
        productPane.setPadding(new Insets(10));
        productPane.setAlignment(Pos.CENTER);

        List<Product> allProducts = DatabaseManager.loadProducts();
        displayFilteredProducts(allProducts, productPane, "", "All Categories");

        categoryBox.setOnAction(e -> {
            String search = searchField.getText().toLowerCase().trim();
            String selectedCategory = categoryBox.getValue();
            displayFilteredProducts(allProducts, productPane, search, selectedCategory);
        });

        searchBtn.setOnAction(e -> {
            String search = searchField.getText().toLowerCase().trim();
            String selectedCategory = categoryBox.getValue();
            displayFilteredProducts(allProducts, productPane, search, selectedCategory);
        });
        
        searchField.setOnAction(e -> searchBtn.fire());

        scrollPane.setContent(productPane);
        homeBox.getChildren().addAll(heading, tagline, searchContainer, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        contentPane.getChildren().setAll(homeBox);
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(240);
        card.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-cursor: hand;");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 12, 0, 0, 3); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-cursor: hand;"));

        StackPane imageContainer = new StackPane();
        imageContainer.setAlignment(Pos.TOP_RIGHT);
        
        ImageView imageView;
        try {
            imageView = new ImageView(new Image(new File(p.getImagePath()).toURI().toString()));
        } catch (Exception e) {
            imageView = new ImageView();
        }

        imageView.setFitHeight(180);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-radius: 12;");
        
        if (p.getDiscountPercent() > 0) {
            Label discountBadge = new Label("-" + p.getDiscountPercent() + "%");
            discountBadge.setStyle(
                "-fx-background-color: #27ae60;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 5px 10px;" +
                "-fx-border-radius: 20px;" +
                "-fx-background-radius: 20px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);"
            );
            StackPane.setMargin(discountBadge, new Insets(8, 8, 0, 0));
            imageContainer.getChildren().addAll(imageView, discountBadge);
        } else {
            imageContainer.getChildren().add(imageView);
        }

        Label nameLabel = new Label(p.getName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);
        nameLabel.setAlignment(Pos.CENTER);

        VBox priceContainer = new VBox(5);
        priceContainer.setAlignment(Pos.CENTER);
        
        if (p.getDiscountPercent() > 0 && p.getDiscountedPrice() < p.getPrice()) {
            Label originalPriceLabel = new Label("Rs. " + String.format("%.2f", p.getPrice()));
            originalPriceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999; -fx-strikethrough: true;");
            
            Label discountedPriceLabel = new Label("Rs. " + String.format("%.2f", p.getDiscountedPrice()));
            discountedPriceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            
            priceContainer.getChildren().addAll(originalPriceLabel, discountedPriceLabel);
        } else {
            Label priceLabel = new Label("Rs. " + String.format("%.2f", p.getPrice()));
            priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            priceContainer.getChildren().add(priceLabel);
        }

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button addToCartBtn = new Button("🛒");
        Button addToWishlistBtn = new Button("❤");
        Button viewBtn = new Button("View");
        
        String btnStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 12px;";
        String outlineBtnStyle = "-fx-background-color: transparent; -fx-border-color: #e74c3c; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 12px;";
        
        addToCartBtn.setStyle(btnStyle);
        addToWishlistBtn.setStyle(outlineBtnStyle);
        viewBtn.setStyle(outlineBtnStyle);
        
        addToCartBtn.setOnMouseEntered(e -> addToCartBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 12px;"));
        addToCartBtn.setOnMouseExited(e -> addToCartBtn.setStyle(btnStyle));
        
        buttonBox.getChildren().addAll(addToCartBtn, addToWishlistBtn, viewBtn);

        addToCartBtn.setOnAction(e -> {
            DatabaseManager.addToCart(username, p.getId());
            showToast("🛒 Added to cart: " + p.getName());
        });

        addToWishlistBtn.setOnAction(e -> {
            DatabaseManager.addToWishlist(username, p.getId());
            showToast("❤️ Added to wishlist: " + p.getName());
        });

        viewBtn.setOnAction(e -> openProductDetail(p));

        card.getChildren().addAll(imageContainer, nameLabel, priceContainer, buttonBox);
        return card;
    }
    
    private void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showCart() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f5f7fa;");
        
        Label title = new Label("🛒 Your Shopping Cart");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        List<Product> cartItems = DatabaseManager.loadCart(username);

        if (cartItems == null || cartItems.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
            
            Label emptyLabel = new Label("🛒 Your cart is empty");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            
            Label shopLabel = new Label("Start shopping to add items");
            shopLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6;");
            
            Button shopBtn = new Button("Continue Shopping");
            shopBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
            shopBtn.setOnAction(e -> showHome());
            
            emptyBox.getChildren().addAll(emptyLabel, shopLabel, shopBtn);
            box.getChildren().addAll(title, emptyBox);
        } else {
            VBox itemsBox = new VBox(10);
            itemsBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
            
            Label totalLabel = new Label();
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #27ae60;");

            for (Product p : cartItems) {
                int[] quantity = {DatabaseManager.getCartQuantity(username, p.getId())};
                p.setQuantity(quantity[0]);

                HBox productRow = new HBox(15);
                productRow.setAlignment(Pos.CENTER_LEFT);
                productRow.setStyle("-fx-padding: 10; -fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");
                
                Label nameLabel = new Label(p.getName());
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-min-width: 200;");
                
                Label priceLabel = new Label("Rs. " + p.getDiscountedPrice());
                priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-min-width: 100;");
                
                Label quantityLabel = new Label(String.valueOf(quantity[0]));
                quantityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 30; -fx-alignment: center;");

                Button decreaseBtn = new Button("-");
                Button increaseBtn = new Button("+");
                
                String qtyBtnStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 12 5 12; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;";
                decreaseBtn.setStyle(qtyBtnStyle);
                increaseBtn.setStyle(qtyBtnStyle);
                
                Button removeBtn = new Button("Remove");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15 5 15;");

                HBox controls = new HBox(8, decreaseBtn, quantityLabel, increaseBtn);
                
                increaseBtn.setOnAction(e -> {
                    quantity[0]++;
                    quantityLabel.setText(String.valueOf(quantity[0]));
                    DatabaseManager.updateCartQuantity(username, p.getId(), quantity[0]);
                    updateCartTotal(itemsBox, totalLabel);
                });

                decreaseBtn.setOnAction(e -> {
                    if (quantity[0] > 1) {
                        quantity[0]--;
                        quantityLabel.setText(String.valueOf(quantity[0]));
                        DatabaseManager.updateCartQuantity(username, p.getId(), quantity[0]);
                        updateCartTotal(itemsBox, totalLabel);
                    }
                });
                
                removeBtn.setOnAction(e -> {
                    DatabaseManager.removeFromCart(username, p.getId());
                    showCart();
                });
                
                productRow.getChildren().addAll(nameLabel, priceLabel, controls, removeBtn);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                itemsBox.getChildren().add(productRow);
            }

            updateCartTotal(itemsBox, totalLabel);

            Button checkout = new Button("Proceed to Checkout →");
            checkout.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30 12 30; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 14px;");
            checkout.setOnMouseEntered(e -> checkout.setStyle("-fx-background-color: #219a52; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30 12 30; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 14px;"));
            checkout.setOnMouseExited(e -> checkout.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30 12 30; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 14px;"));
            checkout.setOnAction(e -> showCheckoutDialog(itemsBox, totalLabel));

            box.getChildren().addAll(title, itemsBox, totalLabel, checkout);
        }

        contentPane.getChildren().setAll(box);
    }
    
    private void updateCartTotal(VBox itemsBox, Label totalLabel) {
        double total = 0;
        for (Node node : itemsBox.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                double price = 0;
                int qty = 1;
                for (Node child : row.getChildren()) {
                    if (child instanceof Label) {
                        String text = ((Label) child).getText();
                        if (text.startsWith("Rs.")) {
                            price = Double.parseDouble(text.replace("Rs.", "").trim());
                        }
                        if (text.matches("\\d+")) {
                            qty = Integer.parseInt(text);
                        }
                    }
                }
                total += price * qty;
            }
        }
        totalLabel.setText(String.format("Total: Rs. %.2f", total));
        totalLabel.setUserData(total);
    }

    private void showCheckoutDialog(VBox itemsBox, Label totalLabel) {
        double subtotal = (double) totalLabel.getUserData();
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Complete your order");
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15;");
        dialog.setResizable(true);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(650);
        scrollPane.setPrefWidth(650);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: white;");
        
        // Customer Information Section
        Label section1 = new Label("📋 Customer Information");
        section1.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 0 0 10 0;");
        grid.add(section1, 0, 0, 2, 1);
        
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        
        TextField addressField = new TextField();
        addressField.setPromptText("Shipping Address");
        addressField.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        grid.add(new Label("Shipping Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");
        contactField.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        grid.add(new Label("Contact Number:"), 0, 3);
        grid.add(contactField, 1, 3);
        
        // Delivery Location Section
        Label section2 = new Label("📍 Delivery Location");
        section2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 15 0 10 0;");
        grid.add(section2, 0, 4, 2, 1);
        
        ComboBox<String> countryBox = new ComboBox<>(FXCollections.observableArrayList("Pakistan", "India", "UAE", "USA", "UK"));
        countryBox.setValue("Pakistan");
        countryBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        grid.add(new Label("Country:"), 0, 5);
        grid.add(countryBox, 1, 5);
        
        ComboBox<String> cityBox = new ComboBox<>(FXCollections.observableArrayList("Karachi", "Lahore", "Islamabad", "Multan", "Rawalpindi"));
        cityBox.setValue("Karachi");
        cityBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        grid.add(new Label("City:"), 0, 6);
        grid.add(cityBox, 1, 6);
        
        // Payment Method Section
        Label section3 = new Label("💳 Payment Method");
        section3.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 15 0 10 0;");
        grid.add(section3, 0, 7, 2, 1);
        
        ToggleGroup paymentGroup = new ToggleGroup();
        VBox paymentOptionsBox = new VBox(10);
        paymentOptionsBox.setStyle("-fx-padding: 5 0 15 0;");
        
        RadioButton codRadio = new RadioButton("💵 Cash on Delivery (COD)");
        codRadio.setToggleGroup(paymentGroup);
        codRadio.setSelected(true);
        codRadio.setStyle("-fx-font-size: 13px; -fx-cursor: hand;");
        
        RadioButton cardRadio = new RadioButton("💳 Credit / Debit Card");
        cardRadio.setToggleGroup(paymentGroup);
        cardRadio.setStyle("-fx-font-size: 13px; -fx-cursor: hand;");
        
        RadioButton jazzCashRadio = new RadioButton("📱 JazzCash");
        jazzCashRadio.setToggleGroup(paymentGroup);
        jazzCashRadio.setStyle("-fx-font-size: 13px; -fx-cursor: hand;");
        
        RadioButton easyPaisaRadio = new RadioButton("📱 EasyPaisa");
        easyPaisaRadio.setToggleGroup(paymentGroup);
        easyPaisaRadio.setStyle("-fx-font-size: 13px; -fx-cursor: hand;");
        
        paymentOptionsBox.getChildren().addAll(codRadio, cardRadio, jazzCashRadio, easyPaisaRadio);
        grid.add(paymentOptionsBox, 0, 8, 2, 1);
        
        // Card Details (hidden initially)
        VBox cardDetailsBox = new VBox(8);
        cardDetailsBox.setVisible(false);
        cardDetailsBox.setManaged(false);
        cardDetailsBox.setStyle("-fx-padding: 10 0 10 20; -fx-border-color: #e74c3c; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #fef9e7;");
        
        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("Card Number (16 digits)");
        cardNumberField.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        
        HBox expiryCvvBox = new HBox(10);
        TextField expiryField = new TextField();
        expiryField.setPromptText("MM/YY");
        expiryField.setPrefWidth(100);
        expiryField.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        
        TextField cvvField = new TextField();
        cvvField.setPromptText("CVV");
        cvvField.setPrefWidth(80);
        cvvField.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        
        expiryCvvBox.getChildren().addAll(expiryField, cvvField);
        cardDetailsBox.getChildren().addAll(new Label("Card Details:"), cardNumberField, expiryCvvBox);
        grid.add(cardDetailsBox, 0, 9, 2, 1);
        
        // Mobile Account Details (hidden initially)
        VBox mobileDetailsBox = new VBox(8);
        mobileDetailsBox.setVisible(false);
        mobileDetailsBox.setManaged(false);
        mobileDetailsBox.setStyle("-fx-padding: 10 0 10 20; -fx-border-color: #e74c3c; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #fef9e7;");
        
        TextField mobileAccountField = new TextField();
        mobileAccountField.setPromptText("Mobile Number (e.g., 03xxxxxxxxx)");
        mobileAccountField.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        
        TextField mobileOtpField = new TextField();
        mobileOtpField.setPromptText("OTP / PIN");
        mobileOtpField.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        
        mobileDetailsBox.getChildren().addAll(new Label("Mobile Account Details:"), mobileAccountField, mobileOtpField);
        grid.add(mobileDetailsBox, 0, 10, 2, 1);
        
        // Show/hide payment details
        cardRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            cardDetailsBox.setVisible(newVal);
            cardDetailsBox.setManaged(newVal);
            mobileDetailsBox.setVisible(false);
            mobileDetailsBox.setManaged(false);
        });
        
        jazzCashRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            mobileDetailsBox.setVisible(newVal);
            mobileDetailsBox.setManaged(newVal);
            cardDetailsBox.setVisible(false);
            cardDetailsBox.setManaged(false);
        });
        
        easyPaisaRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            mobileDetailsBox.setVisible(newVal);
            mobileDetailsBox.setManaged(newVal);
            cardDetailsBox.setVisible(false);
            cardDetailsBox.setManaged(false);
        });
        
        codRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            cardDetailsBox.setVisible(false);
            cardDetailsBox.setManaged(false);
            mobileDetailsBox.setVisible(false);
            mobileDetailsBox.setManaged(false);
        });
        
        // Order Summary Section
        Label section4 = new Label("💰 Order Summary");
        section4.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 15 0 10 0;");
        grid.add(section4, 0, 11, 2, 1);
        
        VBox orderSummaryBox = new VBox(8);
        orderSummaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 15;");
        
        Label subtotalLabel = new Label(String.format("Subtotal: Rs. %.2f", subtotal));
        subtotalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        // Delivery charge based on city
        double[] deliveryCharge = {200};
        Label shippingLabel = new Label(String.format("Shipping Fee: Rs. %.2f", deliveryCharge[0]));
        shippingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        Label grandTotalLabel = new Label(String.format("Grand Total: Rs. %.2f", subtotal + deliveryCharge[0]));
        grandTotalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        cityBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            switch (newVal) {
                case "Karachi": deliveryCharge[0] = 200; break;
                case "Lahore": deliveryCharge[0] = 150; break;
                case "Islamabad": deliveryCharge[0] = 150; break;
                case "Rawalpindi": deliveryCharge[0] = 150; break;
                case "Multan": deliveryCharge[0] = 100; break;
                default: deliveryCharge[0] = 250;
            }
            shippingLabel.setText(String.format("Shipping Fee: Rs. %.2f", deliveryCharge[0]));
            grandTotalLabel.setText(String.format("Grand Total: Rs. %.2f", subtotal + deliveryCharge[0]));
        });
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e0e0e0;");
        
        orderSummaryBox.getChildren().addAll(subtotalLabel, shippingLabel, separator, grandTotalLabel);
        grid.add(orderSummaryBox, 0, 12, 2, 1);
        
        scrollPane.setContent(grid);
        dialog.getDialogPane().setContent(scrollPane);
        
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30 12 30; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 14px;");
        okButton.setText("Place Order");
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30 12 30; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 14px;");
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String fullName = fullNameField.getText().trim();
            String address = addressField.getText().trim();
            String contact = contactField.getText().trim();
            String country = countryBox.getValue();
            String city = cityBox.getValue();
            
            if (fullName.isEmpty() || address.isEmpty() || contact.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Data", "Please fill all required fields.");
                return;
            }
            
            String paymentMode = "";
            if (codRadio.isSelected()) {
                paymentMode = "Cash on Delivery";
            } else if (cardRadio.isSelected()) {
                String cardNum = cardNumberField.getText().trim();
                String expiry = expiryField.getText().trim();
                String cvv = cvvField.getText().trim();
                
                if (cardNum.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Data", "Please fill all card details.");
                    return;
                }
                if (cardNum.length() < 15 || cardNum.length() > 16) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Card", "Please enter a valid card number.");
                    return;
                }
                paymentMode = "Credit/Debit Card";
            } else if (jazzCashRadio.isSelected()) {
                String mobileNum = mobileAccountField.getText().trim();
                String otp = mobileOtpField.getText().trim();
                if (mobileNum.isEmpty() || otp.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Data", "Please fill JazzCash details.");
                    return;
                }
                paymentMode = "JazzCash";
            } else if (easyPaisaRadio.isSelected()) {
                String mobileNum = mobileAccountField.getText().trim();
                String otp = mobileOtpField.getText().trim();
                if (mobileNum.isEmpty() || otp.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Data", "Please fill EasyPaisa details.");
                    return;
                }
                paymentMode = "EasyPaisa";
            } else {
                paymentMode = "Cash on Delivery";
            }
            
            double finalTotal = subtotal + deliveryCharge[0];
            
            try {
                int userId = DatabaseManager.getUserIdByUsername(username);
                List<Product> currentCart = DatabaseManager.loadCart(username);
                
                int orderId = insertOrderWithPayment(userId, fullName, finalTotal, address, contact, country, city, null, paymentMode);
                
                if (orderId <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Order Failed", "Order could not be placed.");
                    return;
                }
                
                List<Product> orderedItems = new ArrayList<>();
                for (Product p : currentCart) {
                    int productId = p.getId();
                    int quantity = DatabaseManager.getCartQuantity(username, productId);
                    DatabaseManager.insertOrderItem(orderId, productId, quantity, p.getDiscountedPrice());
                    DatabaseManager.removeFromCart(username, productId);
                    
                    Product orderedProduct = new Product(p.getName(), p.getPrice(), p.getCategory(), 
                        p.getDiscountedPrice(), p.getDiscountPercent(), p.getImagePath(), p.getAbout());
                    orderedProduct.setId(productId);
                    orderedProduct.setQuantity(quantity);
                    orderedItems.add(orderedProduct);
                }
                
                // Generate invoice with shipping fee
                generateInvoice(orderId, fullName, orderedItems, subtotal, deliveryCharge[0], finalTotal, paymentMode, address + ", " + city + ", " + country);
                
                showAlert(Alert.AlertType.INFORMATION, "Order Placed", 
                    "Your order #" + orderId + " has been placed successfully!\n" +
                    "Payment Method: " + paymentMode + "\n" +
                    "Shipping Fee: Rs. " + deliveryCharge[0] + "\n" +
                    "Total: Rs. " + finalTotal + "\n" +
                    "An invoice has been generated.");
                showCart();
                showConfirmedOrders(username);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Checkout", "An error occurred during checkout: " + ex.getMessage());
            }
        }
    }
    
    // Helper method to insert order with payment mode
    private int insertOrderWithPayment(int userId, String fullName, double totalAmount, String address, String contact, String country, String city, File receipt, String paymentMode) {
        String sql = "INSERT INTO orders (user_id, full_name, total_amount, address, contact, country, city, payment_mode, receipt_path, status, order_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, fullName);
            ps.setDouble(3, totalAmount);
            ps.setString(4, address);
            ps.setString(5, contact);
            ps.setString(6, country);
            ps.setString(7, city);
            ps.setString(8, paymentMode);
            ps.setString(9, null);
            ps.setString(10, "Confirmed");

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
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
        totalLabel.setUserData(total);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWishlist() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f5f7fa;");
        
        Label title = new Label("❤️ My Wishlist");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        List<Product> wishlistItems = DatabaseManager.loadWishlist(username);
        
        if (wishlistItems == null || wishlistItems.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
            
            Label emptyLabel = new Label("❤️ Your wishlist is empty");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            
            Label shopLabel = new Label("Save items you love to your wishlist");
            shopLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6;");
            
            Button shopBtn = new Button("Start Shopping");
            shopBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
            shopBtn.setOnAction(e -> showHome());
            
            emptyBox.getChildren().addAll(emptyLabel, shopLabel, shopBtn);
            box.getChildren().addAll(title, emptyBox);
        } else {
            FlowPane wishlistPane = new FlowPane();
            wishlistPane.setHgap(20);
            wishlistPane.setVgap(20);
            wishlistPane.setPadding(new Insets(10));
            wishlistPane.setAlignment(Pos.CENTER);
            
            for (Product p : wishlistItems) {
                VBox productCard = new VBox(8);
                productCard.setPadding(new Insets(15));
                productCard.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
                productCard.setPrefWidth(220);
                
                Label nameLabel = new Label(p.getName());
                nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                
                Label priceLabel = new Label("Rs. " + p.getDiscountedPrice());
                priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
                
                Button addToCartBtn = new Button("🛒 Add to Cart");
                Button removeBtn = new Button("Remove");
                
                addToCartBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #e74c3c; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
                
                addToCartBtn.setOnAction(e -> {
                    DatabaseManager.addToCart(username, p.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Cart", p.getName() + " added to cart.");
                });
                
                removeBtn.setOnAction(e -> {
                    DatabaseManager.removeFromWishlist(username, p.getId());
                    showWishlist();
                });
                
                HBox btnBox = new HBox(10, addToCartBtn, removeBtn);
                btnBox.setAlignment(Pos.CENTER);
                
                productCard.getChildren().addAll(nameLabel, priceLabel, btnBox);
                wishlistPane.getChildren().add(productCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(wishlistPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent");
            
            box.getChildren().addAll(title, scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
        }
        contentPane.getChildren().setAll(box);
    }

    private void showSettings() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("-fx-background-color: #f5f7fa;");
        
        Label title = new Label("⚙️ Account Settings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox formBox = new VBox(12);
        formBox.setMaxWidth(450);
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        
        Label passLabel = new Label("New Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter new password");
        passField.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm new password");
        confirmPassField.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        Button update = new Button("Update Profile");
        update.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25 12 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 14px;");
        update.setOnMouseEntered(e -> update.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25 12 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 14px;"));
        update.setOnMouseExited(e -> update.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25 12 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 14px;"));
        
        formBox.getChildren().addAll(emailLabel, emailField, passLabel, passField, confirmLabel, confirmPassField, update);
        
        update.setOnAction(e -> {
            String newEmail = emailField.getText().trim();
            String newPass = passField.getText();
            String confirmPass = confirmPassField.getText();
            
            if (!newPass.equals(confirmPass)) {
                showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match.");
                return;
            }
            
            boolean updated = DatabaseManager.updateUserProfile(username, newEmail, newPass);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                emailField.clear();
                passField.clear();
                confirmPassField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile.");
            }
        });
        
        box.getChildren().addAll(title, formBox);
        contentPane.getChildren().setAll(box);
    }

    public BorderPane getRoot() {
        return root;
    }

    private void displayFilteredProducts(List<Product> allProducts, FlowPane pane, String search, String category) {
        pane.getChildren().clear();
        if (allProducts == null) return;
        
        for (Product p : allProducts) {
            String productName = p.getName() != null ? p.getName() : "";
            String productCategory = p.getCategory() != null ? p.getCategory() : "";
            boolean matchSearch = productName.toLowerCase().contains(search);
            boolean matchCategory = category.equalsIgnoreCase("All Categories") || productCategory.equalsIgnoreCase(category);
            if (matchSearch && matchCategory) {
                pane.getChildren().add(createProductCard(p));
            }
        }
    }

    private void showConfirmedOrders(String username) {
        VBox ordersContainer = new VBox(15);
        ordersContainer.setPadding(new Insets(20));
        ordersContainer.setStyle("-fx-background-color: #f5f7fa;");
        
        Label title = new Label("📦 My Orders");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        List<ecommerce.Order> confirmedOrders = DatabaseManager.loadConfirmedOrders(username);
        
        if (confirmedOrders == null || confirmedOrders.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
            
            Label emptyLabel = new Label("📦 No orders found");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            
            Label shopLabel = new Label("Start shopping to place your first order");
            shopLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6;");
            
            Button shopBtn = new Button("Start Shopping");
            shopBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
            shopBtn.setOnAction(e -> showHome());
            
            emptyBox.getChildren().addAll(emptyLabel, shopLabel, shopBtn);
            ordersContainer.getChildren().addAll(title, emptyBox);
        } else {
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            VBox ordersList = new VBox(15);
            
            Map<Integer, List<ecommerce.Order>> groupedOrders = confirmedOrders.stream()
                .collect(Collectors.groupingBy(ecommerce.Order::getOrderId));
                
            for (Map.Entry<Integer, List<ecommerce.Order>> entry : groupedOrders.entrySet()) {
                int orderId = entry.getKey();
                List<ecommerce.Order> products = entry.getValue();
                double orderTotal = products.get(0).getTotalAmount();
                String status = products.get(0).getStatus();
                
                String statusColor, statusIcon;
                switch(status) {
                    case "Confirmed": statusColor = "#f39c12"; statusIcon = "⏳"; break;
                    case "Dispatched": statusColor = "#3498db"; statusIcon = "🚚"; break;
                    case "Delivered": statusColor = "#9b59b6"; statusIcon = "📦"; break;
                    case "Completed": statusColor = "#27ae60"; statusIcon = "✅"; break;
                    default: statusColor = "#95a5a6"; statusIcon = "❓";
                }
                
                VBox orderBox = new VBox(10);
                orderBox.setPadding(new Insets(15));
                orderBox.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
                
                HBox headerRow = new HBox(10);
                headerRow.setAlignment(Pos.CENTER_LEFT);
                
                Label orderIdLabel = new Label("Order #" + orderId);
                orderIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                
                Label statusBadge = new Label(statusIcon + " " + status);
                statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12 4 12; -fx-border-radius: 20; -fx-background-radius: 20;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label dateLabel = new Label(products.get(0).getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                
                headerRow.getChildren().addAll(orderIdLabel, statusBadge, spacer, dateLabel);
                
                VBox productsBox = new VBox(8);
                productsBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12;");
                
                for (ecommerce.Order product : products) {
                    HBox productRow = new HBox(10);
                    productRow.setAlignment(Pos.CENTER_LEFT);
                    Label qtyBadge = new Label("x" + product.getQuantity());
                    qtyBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8 2 8; -fx-border-radius: 12; -fx-background-radius: 12;");
                    Label productName = new Label(product.getProductName());
                    productName.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
                    productRow.getChildren().addAll(qtyBadge, productName);
                    productsBox.getChildren().add(productRow);
                }
                
                HBox totalRow = new HBox(15);
                totalRow.setAlignment(Pos.CENTER_RIGHT);
                totalRow.setPadding(new Insets(10, 0, 0, 0));
                
                Label totalLabel = new Label("Total Amount:");
                totalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                
                Label totalValue = new Label(String.format("Rs. %.2f", orderTotal));
                totalValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
                
                Region spacer2 = new Region();
                HBox.setHgrow(spacer2, Priority.ALWAYS);
                
                Button invoiceBtn = new Button("📄 Download Invoice");
                invoiceBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 12px;");
                invoiceBtn.setOnMouseEntered(e -> invoiceBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 12px;"));
                invoiceBtn.setOnMouseExited(e -> invoiceBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 12px;"));
                
                List<Map<String, Object>> userInvoices = DatabaseManager.getUserInvoices(username);
                boolean hasInvoice = false;
                String invoicePath = null;
                for (Map<String, Object> inv : userInvoices) {
                    if ((int) inv.get("order_id") == orderId) {
                        hasInvoice = true;
                        invoicePath = (String) inv.get("invoice_path");
                        break;
                    }
                }
                
                if (hasInvoice && invoicePath != null) {
                    File invoiceFile = new File(invoicePath);
                    if (invoiceFile.exists()) {
                        invoiceBtn.setOnAction(e -> {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setInitialFileName("invoice_" + orderId + ".pdf");
                            File saveFile = fileChooser.showSaveDialog(stage);
                            if (saveFile != null) {
                                try {
                                    java.nio.file.Files.copy(invoiceFile.toPath(), saveFile.toPath(), 
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                    showAlert(Alert.AlertType.INFORMATION, "Download Complete", 
                                        "Invoice saved to: " + saveFile.getAbsolutePath());
                                } catch (Exception ex) {
                                    showAlert(Alert.AlertType.ERROR, "Download Failed", ex.getMessage());
                                }
                            }
                        });
                    } else {
                        invoiceBtn.setText("⚠️ Invoice Missing");
                        invoiceBtn.setDisable(true);
                        invoiceBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-opacity: 0.6;");
                    }
                } else {
                    invoiceBtn.setOnAction(e -> {
                        List<Product> orderItems = new ArrayList<>();
                        for (ecommerce.Order product : products) {
                            Product prod = new Product(
                                product.getProductName(),
                                0, "", 0, 0, "", ""
                            );
                            prod.setQuantity(product.getQuantity());
                            orderItems.add(prod);
                        }
                        String customerName = username;
                        String shippingAddress = "Address on file";
                        // Generate invoice with subtotal = orderTotal, shipping = 0 (since already included)
                        generateInvoice(orderId, customerName, orderItems, orderTotal, 0, orderTotal, "COD", shippingAddress);
                        showConfirmedOrders(username);
                    });
                }
                
                totalRow.getChildren().addAll(totalLabel, totalValue, spacer2, invoiceBtn);
                orderBox.getChildren().addAll(headerRow, productsBox, totalRow);
                ordersList.getChildren().add(orderBox);
            }
            
            scrollPane.setContent(ordersList);
            ordersContainer.getChildren().addAll(title, scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
        }
        
        contentPane.getChildren().setAll(ordersContainer);
    }

    private void openProductDetail(Product p) {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(0, 0, 15, 0));
        topBar.setAlignment(Pos.TOP_RIGHT);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("secondary-btn");
        refreshBtn.setOnAction(e -> openProductDetail(p));
        topBar.getChildren().add(refreshBtn);

        VBox detailBox = new VBox(15);
        detailBox.setPadding(new Insets(20));
        detailBox.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        detailBox.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        try {
            String imagePath = p.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            // Image loading failed
        }

        imageView.setFitHeight(250);
        imageView.setFitWidth(250);
        imageView.setPreserveRatio(true);

        Label name = new Label(p.getName());
        name.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox detailPriceContainer = new VBox(8);
        detailPriceContainer.setAlignment(Pos.CENTER_LEFT);
        detailPriceContainer.setStyle("-fx-padding: 10 0 15 0;");

        double originalPrice = p.getPrice();
        double discountedPrice = p.getDiscountedPrice();
        int discountPercent = p.getDiscountPercent();

        if (discountPercent > 0 && discountedPrice < originalPrice) {
            HBox originalPriceBox = new HBox(5);
            Label originalLabel = new Label("Original Price:");
            originalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            Label originalPriceDetail = new Label("Rs. " + String.format("%.2f", originalPrice));
            originalPriceDetail.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-strikethrough: true;");
            originalPriceBox.getChildren().addAll(originalLabel, originalPriceDetail);
            
            HBox discountedPriceBox = new HBox(5);
            Label discountedLabel = new Label("Sale Price:");
            discountedLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
            Label discountedPriceDetail = new Label("Rs. " + String.format("%.2f", discountedPrice));
            discountedPriceDetail.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            discountedPriceBox.getChildren().addAll(discountedLabel, discountedPriceDetail);
            
            Label percentDetail = new Label("🔥 " + discountPercent + "% OFF");
            percentDetail.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 6px 15px; -fx-border-radius: 25px; -fx-background-radius: 25px;");
            
            double savedAmount = originalPrice - discountedPrice;
            Label savedDetail = new Label("💎 You Save: Rs. " + String.format("%.2f", savedAmount));
            savedDetail.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            
            detailPriceContainer.getChildren().addAll(originalPriceBox, discountedPriceBox, percentDetail, savedDetail);
        } else {
            HBox regularPriceBox = new HBox(5);
            Label priceLabel = new Label("Price:");
            priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
            Label regularPriceDetail = new Label("Rs. " + String.format("%.2f", originalPrice));
            regularPriceDetail.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            regularPriceBox.getChildren().addAll(priceLabel, regularPriceDetail);
            detailPriceContainer.getChildren().add(regularPriceBox);
        }

        Label aboutTitle = new Label("📝 Product Description");
        aboutTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 10 0 5 0;");
        
        String aboutText = p.getAbout();
        if (aboutText == null || aboutText.isEmpty()) {
            aboutText = "No description available for this product.";
        }
        Label about = new Label(aboutText);
        about.setWrapText(true);
        about.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        VBox reviewList = new VBox(10);
        reviewList.setPadding(new Insets(10));
        reviewList.setStyle("-fx-background-color: #f9f9f9; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label reviewsTitle = new Label("⭐ Customer Reviews");
        reviewsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        reviewList.getChildren().add(reviewsTitle);

        List<String> productReviews = DatabaseManager.getReviewsForProduct(p.getId());
        if (productReviews == null || productReviews.isEmpty()) {
            Label noReviews = new Label("No reviews yet. Be the first to review!");
            noReviews.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            reviewList.getChildren().add(noReviews);
        } else {
            for (String reviewText : productReviews) {
                Label reviewLabel = new Label(reviewText);
                reviewLabel.setWrapText(true);
                reviewLabel.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-border-color: #e74c3c; -fx-border-width: 1px;");
                reviewList.getChildren().add(reviewLabel);
            }
        }

        Label ratingLabel = new Label("Rate this Product:");
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 10 0 5 0;");
        
        HBox starBox = new HBox(5);
        starBox.setAlignment(Pos.CENTER_LEFT);

        Label[] stars = new Label[5];
        int[] selectedRating = {0};

        for (int i = 0; i < 5; i++) {
            Label star = new Label("☆");
            star.setStyle("-fx-font-size: 28px; -fx-cursor: hand; -fx-text-fill: #f39c12;");
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

        Label feedbackLabel = new Label("Write Feedback:");
        feedbackLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Write your feedback about the product...");
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefRowCount(3);

        Label reviewLabelText = new Label("Write Review:");
        reviewLabelText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Write your detailed review...");
        reviewArea.setWrapText(true);
        reviewArea.setPrefRowCount(2);

        Button submitReview = new Button("✅ Submit Review");
        submitReview.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
        submitReview.setOnAction(e -> {
            int rating = selectedRating[0];
            String feedback = feedbackArea.getText().trim();
            String review = reviewArea.getText().trim();

            if (rating == 0) {
                showAlert(Alert.AlertType.WARNING, "Missing Rating", "Please select a rating.");
                return;
            }
            if (feedback.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Feedback", "Please write your feedback.");
                return;
            }
            if (review.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Review", "Please write your review.");
                return;
            }

            boolean success = DatabaseManager.insertReview(p.getId(), username, rating, review, feedback);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Your review has been submitted!");
                openProductDetail(DatabaseManager.getProductById(p.getId()));
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit review.");
            }
        });

        HBox buttonBar = new HBox(20);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(20, 0, 10, 0));

        Button addToCart = new Button("🛒 Add to Cart");
        Button addToWishlist = new Button("❤️ Add to Wishlist");
        Button back = new Button("← Back to Shop");

        addToCart.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
        addToWishlist.setStyle("-fx-background-color: transparent; -fx-border-color: #e74c3c; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");
        back.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25 10 25; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand;");

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

        VBox formBox = new VBox(10);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setMaxWidth(600);
        formBox.setStyle("-fx-padding: 15; -fx-background-color: #fef9e7; -fx-border-radius: 10; -fx-background-radius: 10;");

        formBox.getChildren().addAll(ratingLabel, starBox, feedbackLabel, feedbackArea, reviewLabelText, reviewArea, submitReview);

        detailBox.getChildren().addAll(topBar, imageView, name, detailPriceContainer, aboutTitle, about, reviewList, formBox, buttonBar);

        ScrollPane scroll = new ScrollPane(detailBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        contentPane.getChildren().setAll(scroll);
    }
}