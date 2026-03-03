package com.example.inventory.Controllers;

import com.example.inventory.DTO.*;
import com.example.inventory.Exceptions.DuplicateUserException;
import com.example.inventory.Exceptions.InvalidUserDataException;
import com.example.inventory.Exceptions.UserNotFoundException;
import com.example.inventory.Services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterUserDTO registerDTO) {
        UserResponseDTO user = userService.registerUser(registerDTO);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        JwtResponseDTO response = userService.loginUser(loginDTO);
        return ResponseEntity.ok(response);
    }

    // Exception handling (same pattern as InventoryController)

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateUserException(DuplicateUserException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("DUPLICATE_USER", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("USER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidUserDataException(InvalidUserDataException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("INVALID_USER_DATA", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
