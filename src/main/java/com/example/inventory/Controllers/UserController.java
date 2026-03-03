package com.example.inventory.Controllers;

import com.example.inventory.DTO.*;
import com.example.inventory.Exceptions.DuplicateUserException;
import com.example.inventory.Exceptions.InvalidUserDataException;
import com.example.inventory.Exceptions.UserNotFoundException;
import com.example.inventory.Services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody RegisterUserDTO registerDTO) {
        UserResponseDTO user = userService.registerUser(registerDTO);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUserName(@PathVariable String username) {
        UserResponseDTO user = userService.getUserByUserName(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/modify/{username}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String username,
                                                      @Valid @RequestBody RegisterUserDTO updateDTO) {
        UserResponseDTO user = userService.updateUser(username, updateDTO);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/destroy/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
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
