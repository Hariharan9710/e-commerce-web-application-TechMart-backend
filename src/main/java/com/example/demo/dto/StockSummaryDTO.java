package com.example.demo.dto;

public class StockSummaryDTO {
    private String category;
    private Integer totalStock;
    private Integer productCount;

    public StockSummaryDTO() {}

    public StockSummaryDTO(String category, Integer totalStock, Integer productCount) {
        this.category = category;
        this.totalStock = totalStock;
        this.productCount = productCount;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }

    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }
}