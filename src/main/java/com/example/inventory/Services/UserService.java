package com.example.inventory.Services;

import com.example.inventory.DTO.*;
import com.example.inventory.Exceptions.DuplicateUserException;
import com.example.inventory.Exceptions.InvalidUserDataException;
import com.example.inventory.Exceptions.UserNotFoundException;
import com.example.inventory.Model.Role;
import com.example.inventory.Model.User;
import com.example.inventory.Repository.UserRepository;
import com.example.inventory.Security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    // Helper method to find user or throw UserNotFoundException
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username '" + username + "' not found"));
    }

    // Validation helpers
    private void validateUsernameNotTaken(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("Username '" + username + "' already exists");
        }
    }

    private void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("Email '" + email + "' already exists");
        }
    }

    public UserResponseDTO registerUser(RegisterUserDTO registerDTO) {
        validateUsernameNotTaken(registerDTO.getUsername());
        validateEmailNotTaken(registerDTO.getEmail());

        Role role = registerDTO.getRole() != null ? registerDTO.getRole() : Role.USER;

        User user = new User(
                registerDTO.getUsername(),
                passwordEncoder.encode(registerDTO.getPassword()),
                registerDTO.getEmail(),
                role
        );

        User savedUser = userRepository.save(user);

        // Send welcome email asynchronously (non-blocking)
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());

        return mapToUserResponseDTO(savedUser);
    }

    public JwtResponseDTO loginUser(LoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidUserDataException("Invalid username or password");
        }

        User user = findUserByUsername(loginDTO.getUsername());

        String token = jwtUtil.generateToken(user.getUsername());
        UserResponseDTO userResponse = mapToUserResponseDTO(user);

        return new JwtResponseDTO(token, userResponse);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponseDTO)
                .toList();
    }

    public UserResponseDTO getUserByUserName(String username) {
        User user = findUserByUsername(username);
        return mapToUserResponseDTO(user);
    }

    public UserResponseDTO updateUser(String username, RegisterUserDTO updateDTO) {
        User user = findUserByUsername(username);

        // Check for duplicate username if it's being changed
        if (!user.getUsername().equals(updateDTO.getUsername())
                && userRepository.existsByUsername(updateDTO.getUsername())) {
            throw new DuplicateUserException("Username '" + updateDTO.getUsername() + "' already exists");
        }

        // Check for duplicate email if it's being changed
        if (!user.getEmail().equals(updateDTO.getEmail())
                && userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new DuplicateUserException("Email '" + updateDTO.getEmail() + "' already exists");
        }

        user.setUsername(updateDTO.getUsername());
        user.setEmail(updateDTO.getEmail());
        user.setRole(updateDTO.getRole());

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        User savedUser = userRepository.save(user);

        // Send account updated email asynchronously (non-blocking)
        emailService.sendAccountUpdatedEmail(savedUser.getEmail(), savedUser.getUsername());

        return mapToUserResponseDTO(savedUser);
    }

    @Transactional
    public void deleteUser(String username) {
        User user = findUserByUsername(username);
        String email = user.getEmail();

        userRepository.deleteByUsername(username);

        // Send account deleted email asynchronously (non-blocking)
        emailService.sendAccountDeletedEmail(email, username);
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.isActive()
        );
    }
}