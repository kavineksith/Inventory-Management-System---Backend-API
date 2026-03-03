package com.example.inventory.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateInventoryDTO extends BaseInventoryDTO {

    public UpdateInventoryDTO() {
        super(); // Explicit call to parent constructor
    }

    public UpdateInventoryDTO(String itemName, int QTY, double price) {
        super(itemName, QTY, price);
    }

    // Add toString for debugging
    @Override
    public String toString() {
        return "UpdateInventoryDTO{" +
                "itemName='" + itemName + '\'' +
                ", QTY=" + QTY +
                ", price=" + price +
                '}';
    }
}