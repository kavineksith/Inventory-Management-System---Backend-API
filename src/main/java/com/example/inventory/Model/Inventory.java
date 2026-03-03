package com.example.inventory.Model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "inventory_table")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invent_id", nullable = false, unique = true, insertable = true, updatable = false)
    private UUID inventoryID;

    @Column(name = "plu_code", nullable = false)
    private String PLU;

    @Column(name = "itemName", nullable = false)
    private String itemName;

    @Column(name = "itemQuantity", nullable = false)
    private int QTY;

    @Column(name = "itemPrice", nullable = false)
    private double price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_filename")
    private String imageFilename;

    public Inventory() {
    }

    public Inventory(String PLU, String itemName, int QTY, double price, String imageUrl, String imageFilename) {
        this.PLU = PLU;
        this.itemName = itemName;
        this.QTY = QTY;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imageFilename = imageFilename;
    }

    public Inventory(UUID inventoryID, String PLU, String itemName, int QTY, double price, String imageUrl, String imageFilename) {
        this.inventoryID = inventoryID;
        this.PLU = PLU;
        this.itemName = itemName;
        this.QTY = QTY;
        this.price = price;
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
}
