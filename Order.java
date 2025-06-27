package ecommerce;

import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private int userId;
    private double totalAmount;
    private String address;
    private String contact;
    private String country;
    private String city;
    private String paymentMode;
    private String receiptPath;
    private LocalDateTime orderDate;

    // Product-level fields (for JOINed views)
    private String productName;
    private int quantity;
    private String status;

    // Replace username with fullName
    private String fullName;

    // Default constructor
    public Order() {
    }

    // Getters and Setters

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getReceiptPath() {
        return receiptPath;
    }

    public void setReceiptPath(String receiptPath) {
        this.receiptPath = receiptPath;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
       return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // fullName getter/setter
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
