package com.example.inventory.Exceptions;

public class DuplicateUserException extends UserException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
