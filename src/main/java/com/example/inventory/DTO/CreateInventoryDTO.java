package com.example.inventory.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateInventoryDTO extends BaseInventoryDTO {
    @NotBlank(message = "PLU code is required")
    @Pattern(regexp = "^[A-Z0-9]{4,20}$", message = "PLU code must be 4-20 characters, alphanumeric uppercase only")
    @JsonProperty("PLU") // Ensure JSON mapping works correctly
    private String PLU;

    public CreateInventoryDTO() {
        super(); // Important: call parent constructor
    }

    // Fixed constructor parameter order to match JSON structure
    public CreateInventoryDTO(String PLU, String itemName, int QTY, double price) {
        super(itemName, QTY, price);
        this.PLU = PLU;
    }

    public String getPLU() {
        return PLU;
    }

    public void setPLU(String PLU) {
        this.PLU = PLU;
    }

    // Add toString for debugging
    @Override
    public String toString() {
        return "CreateInventoryDTO{" +
                "PLU='" + PLU + '\'' +
                ", itemName='" + itemName + '\'' +
                ", QTY=" + QTY +
                ", price=" + price +
                '}';
    }
}