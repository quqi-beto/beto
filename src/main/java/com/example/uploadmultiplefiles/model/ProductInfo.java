package com.example.uploadmultiplefiles.model;

public class ProductInfo {

    private String productName;
    private String variationName;
    private String price;
    private String quantity;

    public ProductInfo(String productName, String variationName, String price, String quantity)
    {
        this.productName = productName;
        this.variationName = variationName;
        this.price = price;
        this.quantity = quantity;
    }

    public ProductInfo()
    {
    }

    public String getProductName() {
        return this.productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getVariationName() {
        return this.variationName;
    }
    public void setVariationName(String variationName) {
        this.variationName = variationName;
    }
    public String getPrice() {
        return this.price;
    }
    public void setPrice(String itemDetails) {
        this.price = itemDetails;
    }
    public String getQuantity() {
        return this.quantity;
    }
    public void setQuantity(String string) {
        this.quantity = string;
    }

    public String toString()
    {
        return "ProductInfo [productName=" + this.productName + ", variationName=" + this.variationName + ", price=" + this.price +
                ", quantity=" + this.quantity + "]";
    }
}
