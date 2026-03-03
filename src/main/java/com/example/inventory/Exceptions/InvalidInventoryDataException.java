package com.example.inventory.Exceptions;

public class InvalidInventoryDataException extends InventoryException {
    public InvalidInventoryDataException(String message) {
        super(message);
    }
}
