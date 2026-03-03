package com.example.inventory.DTO;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InventoryListDTO extends BaseInventoryDTO {
    @JsonProperty("inventoryID")
    private UUID inventoryID;

    @JsonProperty("PLU")
    private String PLU;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("imageFilename")
    private String imageFilename;

    public InventoryListDTO() {
        super(); // Explicit call to parent constructor
    }

    // Fixed constructor parameter order to be more logical
    public InventoryListDTO(UUID inventoryID, String PLU, String itemName, int QTY, double price, String imageUrl, String imageFilename) {
        super(itemName, QTY, price);
        this.inventoryID = inventoryID;
        this.PLU = PLU;
        this.imageUrl = imageUrl;
        this.imageFilename = imageFilename;
    }

    public UUID getInventoryID() {
        return inventoryID;
    }

    public void setInventoryID(UUID inventoryID) {
        this.inventoryID = inventoryID;
    }

    public String getPLU() {
        return PLU;
    }

    public void setPLU(String PLU) {
        this.PLU = PLU;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    // Add toString for debugging
    @Override
    public String toString() {
        return "InventoryListDTO{" +
                "inventoryID=" + inventoryID +
                ", PLU='" + PLU + '\'' +
                ", itemName='" + itemName + '\'' +
                ", QTY=" + QTY +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageFilename='" + imageFilename + '\'' +
                '}';
    }
}