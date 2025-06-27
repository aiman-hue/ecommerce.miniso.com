package ecommerce;

import javafx.collections.ObservableList;

public class Product {

    static ObservableList<String> getitems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private int id;
    private String name;
    private double price;
    private String category;
    private double discounted_Price;
    private int discount_Percent;
    private String image_Path;
    private String about;
    private double rating;
    private String reviews;
    private String feedback;
    private int quantity;

    public Product(String name, double price, String category, double discounted_Price, int discount_Percent, String image_Path, String about) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.discounted_Price = discounted_Price;
        this.discount_Percent = discount_Percent;
        this.image_Path = image_Path;
        this.about = about;
    }
    public Product(int id, String name, double price, String category,
               double discounted_Price, int discount_Percent,
               String image_Path, String about) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.category = category;
    this.discounted_Price = discounted_Price;
    this.discount_Percent = discount_Percent;
    this.image_Path = image_Path;
    this.about = about;
}
     public Product(int id, String name, double price, double discounted_Price, int discount_Percent,
                   double rating, String reviews, String about, String feedback, String image_Path) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.discounted_Price = discounted_Price;
        this.discount_Percent = discount_Percent;
        this.rating = rating;
        this.reviews = reviews;
        this.about = about;
        this.feedback = feedback;
        this.image_Path = image_Path;
    }
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getDiscountedPrice() { return discounted_Price; }
    public void setDiscountedPrice(double discountedPrice) { this.discounted_Price = discountedPrice; }

    public int getDiscountPercent() { return discount_Percent; }
    public void setDiscountPercent(int discountPercent) { this.discount_Percent = discountPercent; }

    public String getImagePath() { return image_Path; }
    public void setImagePath(String imagePath) { this.image_Path = imagePath; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    
    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
