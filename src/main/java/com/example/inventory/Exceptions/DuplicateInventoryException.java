package com.example.inventory.Exceptions;

public class DuplicateInventoryException extends InventoryException{
    public DuplicateInventoryException(String message) {
        super(message);
    }
}
