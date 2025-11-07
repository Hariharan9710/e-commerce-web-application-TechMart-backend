package com.example.demo.dto;

public class CartItemDTO {
    private Long id;
    private Long productId;
    private String name;
    private String description;
    private String category;
    private String brand;
    private Double price;
    private String image;
    private Integer quantity;

    public CartItemDTO() {}

    public CartItemDTO(Long id, Long productId, String name, String description, 
                       String category, String brand, Double price, String image, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}