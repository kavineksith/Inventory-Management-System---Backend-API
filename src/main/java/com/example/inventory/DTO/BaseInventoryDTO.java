package com.example.inventory.DTO;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BaseInventoryDTO {
    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 100, message = "Item name must be between 2 and 100 characters")
    @JsonProperty("itemName")
    protected String itemName;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 999999, message = "Quantity cannot exceed 999999")
    @JsonProperty("QTY")
    protected int QTY;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "9999999.99", message = "Price cannot exceed 9999999.99")
    @Digits(integer = 7, fraction = 2, message = "Price format is invalid")
    @JsonProperty("price")
    protected double price;

    public BaseInventoryDTO() {
    }

    public BaseInventoryDTO(String itemName, int QTY, double price) {
        this.itemName = itemName;
        this.QTY = QTY;
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQTY() {
        return QTY;
    }

    public void setQTY(int QTY) {
        this.QTY = QTY;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}