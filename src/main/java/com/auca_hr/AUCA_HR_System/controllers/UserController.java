package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.dtos.ApiResponse;
import com.auca_hr.AUCA_HR_System.dtos.UserRegistrationDTO;
import com.auca_hr.AUCA_HR_System.dtos.UserUpdateDTO;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.enums.UserRole;
import com.auca_hr.AUCA_HR_System.exceptions.DuplicateResourceException;
import com.auca_hr.AUCA_HR_System.exceptions.InvalidFormatException;
import com.auca_hr.AUCA_HR_System.exceptions.ResourceNotFoundException;
import com.auca_hr.AUCA_HR_System.services.UserService;
import com.auca_hr.AUCA_HR_System.utils.FileStorageService;
import com.auca_hr.AUCA_HR_System.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Validated
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, FileStorageService fileStorageService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.jwtUtil = jwtUtil;
    }
    @GetMapping("/search")
    public ResponseEntity<List<UserRegistrationDTO>> searchUsers(
            @RequestParam("q") String query,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        Long currentUserId = jwtUtil.extractUserId(token);

        List<UserRegistrationDTO> users = userService.searchUsers(query, currentUserId);
        return ResponseEntity.ok(users);
    }
    /**
     * Create a new user
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<User>> createUser(@ModelAttribute UserRegistrationDTO registrationDTO) {
        try {
            User createdUser = userService.createUser(registrationDTO);
            ApiResponse<User> response = new ApiResponse<>(
                    true,
                    "User created successfully",
                    createdUser,
                    HttpStatus.CREATED.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (InvalidFormatException e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "Invalid format: " + e.getMessage(),
                    null,
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (DuplicateResourceException e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "Duplicate resource: " + e.getMessage(),
                    null,
                    HttpStatus.CONFLICT.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } catch (Exception e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "An error occurred while creating user: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse<User>> createUser(
//            @RequestPart("userData") UserRegistrationDTO registrationDTO,
//            @RequestPart(value = "photo", required = false) MultipartFile photo) {
//        try {
//            // Upload photo if provided
//            String photoUrl = null;
//            if (photo != null && !photo.isEmpty()) {
//                photoUrl = fileStorageService.uploadImage(photo);
//            }
//
//            // Set the photo URL in the DTO
//            registrationDTO.setPhoto(photoUrl);
//
//            User createdUser = userService.createUser(registrationDTO);
//            ApiResponse<User> response = new ApiResponse<>(
//                    true,
//                    "User created successfully",
//                    createdUser,
//                    HttpStatus.CREATED.value()
//            );
//            return new ResponseEntity<>(response, HttpStatus.CREATED);
//        } catch (FileValidationException e) {
//            ApiResponse<User> response = new ApiResponse<>(
//                    false,
//                    "File validation error: " + e.getMessage(),
//                    null,
//                    HttpStatus.BAD_REQUEST.value()
//            );
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        } catch (InvalidFormatException e) {
//            ApiResponse<User> response = new ApiResponse<>(
//                    false,
//                    "Invalid format: " + e.getMessage(),
//                    null,
//                    HttpStatus.BAD_REQUEST.value()
//            );
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        } catch (DuplicateResourceException e) {
//            ApiResponse<User> response = new ApiResponse<>(
//                    false,
//                    "Duplicate resource: " + e.getMessage(),
//                    null,
//                    HttpStatus.CONFLICT.value()
//            );
//            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
//        } catch (Exception e) {
//            ApiResponse<User> response = new ApiResponse<>(
//                    false,
//                    "An error occurred while creating user: " + e.getMessage(),
//                    null,
//                    HttpStatus.INTERNAL_SERVER_ERROR.value()
//            );
//            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    /**
     * Update an existing user
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        try {
            User updatedUser = userService.updateUser(id, updateDTO);
            ApiResponse<User> response = new ApiResponse<>(
                    true,
                    "User updated successfully",
                    updatedUser,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "User not found: " + e.getMessage(),
                    null,
                    HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (InvalidFormatException e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "Invalid format: " + e.getMessage(),
                    null,
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (DuplicateResourceException e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "Duplicate resource: " + e.getMessage(),
                    null,
                    HttpStatus.CONFLICT.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } catch (Exception e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "An error occurred while updating user: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            ApiResponse<User> response = new ApiResponse<>(
                    true,
                    "User retrieved successfully",
                    user,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "User not found: " + e.getMessage(),
                    null,
                    HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "An error occurred while retrieving user: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile() {
        try {
            User currentUser = userService.getCurrentUser();
            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            ApiResponse<User> response = new ApiResponse<>(
                    true,
                    "User retrieved successfully",
                    user,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "User not found: " + e.getMessage(),
                    null,
                    HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<User> response = new ApiResponse<>(
                    false,
                    "An error occurred while retrieving user: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all users with pagination
     */
    // Add this endpoint to your UserController

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<User> users = userService.getAllUsers();

            // Simple pagination logic
            int totalUsers = users.size();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalUsers);

            List<User> paginatedUsers = users.subList(startIndex, endIndex);

            ApiResponse<List<User>> response = new ApiResponse<>(
                    true,
                    "Users retrieved successfully. Total: " + totalUsers + ", Page: " + page + ", Size: " + paginatedUsers.size(),
                    paginatedUsers,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<User>> response = new ApiResponse<>(
                    false,
                    "An error occurred while retrieving users: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable UserRole role) {
        try {
            List<User> users = userService.getUsersByRole(role);
            ApiResponse<List<User>> response = new ApiResponse<>(
                    true,
                    "Users retrieved successfully for role: " + role,
                    users,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<User>> response = new ApiResponse<>(
                    false,
                    "An error occurred while retrieving users by role: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete user by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            ApiResponse<Void> response = new ApiResponse<>(
                    true,
                    "User deleted successfully",
                    null,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            ApiResponse<Void> response = new ApiResponse<>(
                    false,
                    "User not found: " + e.getMessage(),
                    null,
                    HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<Void> response = new ApiResponse<>(
                    false,
                    "An error occurred while deleting user: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {
        try {
            boolean exists = userService.emailExists(email);
            ApiResponse<Boolean> response = new ApiResponse<>(
                    true,
                    "Email existence check completed",
                    exists,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Boolean> response = new ApiResponse<>(
                    false,
                    "An error occurred while checking email: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if national ID exists
     */
    @GetMapping("/check-national-id/{nationalId}")
    public ResponseEntity<ApiResponse<Boolean>> checkNationalIdExists(@PathVariable String nationalId) {
        try {
            boolean exists = userService.nationalIdExists(nationalId);
            ApiResponse<Boolean> response = new ApiResponse<>(
                    true,
                    "National ID existence check completed",
                    exists,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Boolean> response = new ApiResponse<>(
                    false,
                    "An error occurred while checking national ID: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if phone number exists
     */
    @GetMapping("/check-phone/{phoneNumber}")
    public ResponseEntity<ApiResponse<Boolean>> checkPhoneNumberExists(@PathVariable String phoneNumber) {
        try {
            boolean exists = userService.phoneNumberExists(phoneNumber);
            ApiResponse<Boolean> response = new ApiResponse<>(
                    true,
                    "Phone number existence check completed",
                    exists,
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Boolean> response = new ApiResponse<>(
                    false,
                    "An error occurred while checking phone number: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}