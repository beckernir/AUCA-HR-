package com.auca_hr.AUCA_HR_System.services;

import com.auca_hr.AUCA_HR_System.dtos.*;
import com.auca_hr.AUCA_HR_System.entities.Education;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.entities.WorkExperience;
import com.auca_hr.AUCA_HR_System.enums.UserRole;
import com.auca_hr.AUCA_HR_System.exceptions.DuplicateResourceException;
import com.auca_hr.AUCA_HR_System.exceptions.FileValidationException;
import com.auca_hr.AUCA_HR_System.exceptions.InvalidFormatException;
import com.auca_hr.AUCA_HR_System.exceptions.ResourceNotFoundException;
import com.auca_hr.AUCA_HR_System.repositories.UserRepository;
import com.auca_hr.AUCA_HR_System.utils.FileStorageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private Validator validator;
    private EmailService emailService;
    private final FileStorageService fileStorageService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       Validator validator,
                       EmailService emailService, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
    }

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile(
            "^[0-9]{16}$" // Assuming Rwanda national ID format
    );

    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
            "^[0-9]{10,20}$"
    );

    private static final Pattern RSSB_NUMBER_PATTERN = Pattern.compile(
            "^[0-9]{10,15}$"
    );

    // Constants
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 65;
    private static final int MIN_PASSWORD_LENGTH = 8;

    public List<UserRegistrationDTO> searchUsers(String query, Long currentUserId) {
        List<User> matchedUsers = userRepository.findByFullNamesContainingIgnoreCase(query);

        return matchedUsers.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .map(this::buildUserDTO) // Use a lambda instead of method reference
                .collect(Collectors.toList());

    }


    /**
     * Create a new user with comprehensive validation
     */
    public User createUser(UserRegistrationDTO registrationDTO) {
        validateUserRegistration(registrationDTO);
        User user = buildUserFromRegistrationDTO(registrationDTO);
        // Generate password if not provided
        String password = registrationDTO.getPassword() != null ? registrationDTO.getPassword() : generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(password)); // IMPORTANT

        // IMPORTANT: Use the helper method, don't set education list directly
        if (registrationDTO.getEducation() != null) {
            for (EducationDTO eduDto : registrationDTO.getEducation()) {
                Education education = new Education();
                education.setInstitution(eduDto.getInstitution());
                education.setDepartment(eduDto.getDepartment());
                education.setProgram(eduDto.getProgram());
                education.setPeriod(eduDto.getPeriod());

                // THIS IS THE KEY - use addEducation(), not user.getEducation().add()
                user.addEducation(education);
            }
        }
        // IMPORTANT: Use the helper method, don't set education list directly
        if (registrationDTO.getWorkExperienceDTO() != null) {
            for (WorkExperienceDTO eduDto : registrationDTO.getWorkExperienceDTO()) {
                WorkExperience education = new WorkExperience();
                education.setCompany(eduDto.getCompany());
                education.setPosition(eduDto.getPosition());
                education.setExperience(eduDto.getExperience());
                education.setYear(eduDto.getYear());
                education.setLogo(education.getLogo());

                // THIS IS THE KEY - use addEducation(), not user.getEducation().add()
                user.addWorkExperience(education);
            }
        }



        User savedUser = userRepository.save(user);
        sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullNames(), password);


        return savedUser;
    }

    /**
     * Update existing user
     */
    public User updateUser(Long userId, UserUpdateDTO updateDTO) {
        User existingUser = getUserById(userId);
        validateUserUpdate(updateDTO, existingUser);
        User updatedUser = buildUpdatedUser(existingUser, updateDTO);
        return userRepository.save(updatedUser);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }
    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            email = null;
        }

        if (email == null) {
            throw new RuntimeException("Unable to determine user email from authentication");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return user;
    }
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if national ID exists
     */
    @Transactional(readOnly = true)
    public boolean nationalIdExists(String nationalId) {
        return userRepository.existsByNationalId(nationalId);
    }

    /**
     * Check if phone number exists
     */
    @Transactional(readOnly = true)
    public boolean phoneNumberExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    // Private validation methods
    private void validateUserRegistration(UserRegistrationDTO dto) {
        // Bean validation
        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<UserRegistrationDTO> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new ValidationException("Registration validation failed: " + sb.toString());
        }

        // Custom validations
        validateEmail(dto.getEmail());
        validatePhoneNumber(dto.getPhoneNumber());
        validateNationalId(dto.getNationalId());
        validateDateOfBirth(dto.getDateOfBirth());
//        validatePassword(dto.getPassword());
        validateSalary(dto.getSalary());
        validateAccountNumber(dto.getAccountNumber());
        validateRssbNumber(dto.getRssbNumber());
        validatePhoto(dto.getPhoto());

        // Check uniqueness
        if (emailExists(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
        if (nationalIdExists(dto.getNationalId())) {
            throw new DuplicateResourceException("National ID already exists: " + dto.getNationalId());
        }
        if (phoneNumberExists(dto.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists: " + dto.getPhoneNumber());
        }
    }

    private void validateUserUpdate(UserUpdateDTO dto, User existingUser) {
        // Bean validation
        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<UserUpdateDTO> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new ValidationException("Update validation failed: " + sb.toString());
        }

        // Custom validations
        if (StringUtils.hasText(dto.getEmail())) {
            validateEmail(dto.getEmail());
            if (!dto.getEmail().equals(existingUser.getEmail()) && emailExists(dto.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
            }
        }

        if (StringUtils.hasText(dto.getPhoneNumber())) {
            validatePhoneNumber(dto.getPhoneNumber());
            if (!dto.getPhoneNumber().equals(existingUser.getPhoneNumber()) && phoneNumberExists(dto.getPhoneNumber())) {
                throw new DuplicateResourceException("Phone number already exists: " + dto.getPhoneNumber());
            }
        }

        if (StringUtils.hasText(dto.getNationalId())) {
            validateNationalId(dto.getNationalId());
            if (!dto.getNationalId().equals(existingUser.getNationalId()) && nationalIdExists(dto.getNationalId())) {
                throw new DuplicateResourceException("National ID already exists: " + dto.getNationalId());
            }
        }

        if (dto.getDateOfBirth() != null) {
            validateDateOfBirth(dto.getDateOfBirth());
        }

        if (dto.getSalary() != null) {
            validateSalary(dto.getSalary());
        }

        if (StringUtils.hasText(dto.getAccountNumber())) {
            validateAccountNumber(dto.getAccountNumber());
        }

        if (StringUtils.hasText(dto.getRssbNumber())) {
            validateRssbNumber(dto.getRssbNumber());
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidFormatException("Invalid email format");
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new ValidationException("Phone number is required");
        }
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new InvalidFormatException("Invalid phone number format");
        }
    }
    /**
     * Validate photo upload using FileStorageService validation logic
     * @param photo The multipart file containing the user's photo
     * @throws ValidationException if photo validation fails
     */
    private void validatePhoto(MultipartFile photo) {
        // Skip validation if photo is not provided (optional upload)
        if (photo == null || photo.isEmpty()) {
            return; // Photo is optional, so we don't throw an error
        }

        try {
            // Use FileStorageService to validate the image
            fileStorageService.uploadImage(photo);
        } catch (FileValidationException e) {
            throw new ValidationException("Photo validation failed: " + e.getMessage());
        }
    }

    private void validateNationalId(String nationalId) {
        if (!StringUtils.hasText(nationalId)) {
            throw new ValidationException("National ID is required");
        }
        if (!NATIONAL_ID_PATTERN.matcher(nationalId).matches()) {
            throw new InvalidFormatException("Invalid national ID format. Must be 16 digits");
        }
    }

    private void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new ValidationException("Date of birth is required");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future");
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < MIN_AGE) {
            throw new ValidationException("User must be at least " + MIN_AGE + " years old");
        }
        if (age > MAX_AGE) {
            throw new ValidationException("User cannot be older than " + MAX_AGE + " years");
        }
    }

//    private void validatePassword(String password) {
////        if (!StringUtils.hasText(password)) {
////            throw new ValidationException("Password is required");
////        }
//        if (password.length() < MIN_PASSWORD_LENGTH) {
//            throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
//        }
//        if (!password.matches(".*[A-Z].*")) {
//            throw new ValidationException("Password must contain at least one uppercase letter");
//        }
//        if (!password.matches(".*[a-z].*")) {
//            throw new ValidationException("Password must contain at least one lowercase letter");
//        }
//        if (!password.matches(".*[0-9].*")) {
//            throw new ValidationException("Password must contain at least one digit");
//        }
//        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
//            throw new ValidationException("Password must contain at least one special character");
//        }
//    }

    private void validateSalary(BigDecimal salary) {
        if (salary != null && salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Salary cannot be negative");
        }
    }

    private void validateAccountNumber(String accountNumber) {
        if (StringUtils.hasText(accountNumber) && !ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            throw new InvalidFormatException("Account number must be 10-20 digits");
        }
    }

    private void validateRssbNumber(String rssbNumber) {
        if (StringUtils.hasText(rssbNumber) && !RSSB_NUMBER_PATTERN.matcher(rssbNumber).matches()) {
            throw new InvalidFormatException("RSSB number must be 10-15 digits");
        }
    }

    // Builder methods for creating users from DTOs
    private UserRegistrationDTO buildUserDTO(User user) {
        UserRegistrationDTO dto = new UserRegistrationDTO();

        dto.setFullNames(user.getFullNames());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setNationalId(user.getNationalId());
        dto.setNationality(user.getNationality());
        dto.setMaritalStatus(user.getMaritalStatus());
        dto.setReligion(user.getReligion());
        dto.setContractType(user.getContractType());
        dto.setAcademicRank(user.getAcademicRank());
        dto.setWorkingPosition(user.getWorkingPosition());
        dto.setSalary(user.getSalary());
        dto.setBankAccount(user.getBankAccount());
        dto.setAccountNumber(user.getAccountNumber());
        dto.setRssbNumber(user.getRssbNumber());
        dto.setRole(user.getRole());
        dto.setTotalAllowances(user.getTotalAllowances());
        dto.setTprLevel(user.getTprLevel());
//
//        String photoUrl = user.getPhoto();
//        if (photoUrl == null || photoUrl.trim().isEmpty()) {
//            photoUrl = fileStorageService.getDefaultImageUrl();
//        }
//        dto.setPhoto(photoUrl);

        return dto;
    }

    private User buildUserFromRegistrationDTO(UserRegistrationDTO dto) {
        User user = new User();

        // Required fields
        user.setFullNames(dto.getFullNames());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setGender(dto.getGender());
        user.setNationalId(dto.getNationalId());
        user.setNationality(dto.getNationality());
        user.setMaritalStatus(dto.getMaritalStatus());
        user.setReligion(dto.getReligion());
        user.setContractType(dto.getContractType());
        user.setAcademicRank(dto.getAcademicRank());
        user.setWorkingPosition(dto.getWorkingPosition());
        user.setSalary(dto.getSalary());
        user.setBankAccount(dto.getBankAccount());
        user.setAccountNumber(dto.getAccountNumber());
        user.setRssbNumber(dto.getRssbNumber());
        user.setRole(dto.getRole());
        user.setTotalAllowances(dto.getTotalAllowances());
        user.setTprLevel(dto.getTprLevel());

        String photoUrl = user.getPhoto();
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            photoUrl = fileStorageService.getDefaultImageUrl();
        }
        user.setPhoto(photoUrl);
        return user;
    }

    private User buildUpdatedUser(User existingUser, UserUpdateDTO dto) {
        // Update only non-null fields
        if (StringUtils.hasText(dto.getFullNames())) {
            existingUser.setFullNames(dto.getFullNames());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            existingUser.setEmail(dto.getEmail());
        }
        if (StringUtils.hasText(dto.getPhoneNumber())) {
            existingUser.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getGender() != null) {
            existingUser.setGender(dto.getGender());
        }
        if (StringUtils.hasText(dto.getNationalId())) {
            existingUser.setNationalId(dto.getNationalId());
        }
        if (StringUtils.hasText(dto.getNationality())) {
            existingUser.setNationality(dto.getNationality());
        }
        if (dto.getMaritalStatus() != null) {
            existingUser.setMaritalStatus(dto.getMaritalStatus());
        }
        if (dto.getReligion() != null) {
            existingUser.setReligion(dto.getReligion());
        }
        if (dto.getContractType() != null) {
            existingUser.setContractType(dto.getContractType());
        }
        if (dto.getAcademicRank() != null) {
            existingUser.setAcademicRank(dto.getAcademicRank());
        }
        if (StringUtils.hasText(dto.getWorkingPosition())) {
            existingUser.setWorkingPosition(dto.getWorkingPosition());
        }
        if (dto.getSalary() != null) {
            existingUser.setSalary(dto.getSalary());
        }
        if (StringUtils.hasText(dto.getBankAccount())) {
            existingUser.setBankAccount(dto.getBankAccount());
        }
        if (StringUtils.hasText(dto.getAccountNumber())) {
            existingUser.setAccountNumber(dto.getAccountNumber());
        }
        if (StringUtils.hasText(dto.getRssbNumber())) {
            existingUser.setRssbNumber(dto.getRssbNumber());
        }
        if (dto.getRole() != null) {
            existingUser.setRole(dto.getRole());
        }

        return existingUser;
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    private void sendWelcomeEmail(String email, String firstName, String rawPassword) {
        String htmlTemplate = String.format(
                "<!DOCTYPE html>" +
                        "<html lang=\"en\">" +
                        "<head>" +
                        "<meta charset=\"UTF-8\" />" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                        "<title>Welcome to AUCA HR System</title>" +
                        "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\" />" +
                        "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin />" +
                        "<link href=\"https://fonts.googleapis.com/css2?family=Lato:wght@400;700&display=swap\" rel=\"stylesheet\" />" +
                        "</head>" +
                        "<body style=\"font-family: 'Lato', sans-serif; background-color: #F2F8FC; padding: 20px;\">" +
                        "<div style=\"max-width: 600px; margin: auto; background: #FFFFFF; border: 1px solid #D1E3F8; border-radius: 8px; padding: 20px;\">" +
                        "<div style=\"text-align: center; margin-bottom: 20px;\">" +
                        "<img src=\"https://www.auca.ac.rw/images/auca-logo.png\" alt=\"AUCA Logo\" style=\"width: 150px;\"/>" +
                        "</div>" +
                        "<h1 style=\"font-size: 20px; color: #003366;\">Hi %s,</h1>" +
                        "<p>Welcome to the <strong>AUCA HR Management System</strong>! Your account has been successfully created by our administrator. You can now access the system to manage your HR tasks efficiently.</p>" +
                        "<h2 style=\"font-size: 16px; color: #003366;\">Your login credentials:</h2>" +
                        "<ul style=\"list-style: none; padding: 0;\">" +
                        "<li><strong>Email:</strong> %s</li>" +
                        "<li><strong>Password:</strong> %s</li>" +
                        "</ul>" +
                        "<p>For security purposes, please update your password immediately after your first login.</p>" +
                        "<div style=\"text-align: center;\">" +
                        "<a href=\"#\" style=\"display: inline-block; margin-top: 15px; padding: 10px 20px; background: #003366; color: #FFFFFF; text-decoration: none; border-radius: 5px;\">Access HR System</a>" +
                        "</div>" +
                        "<p style=\"margin-top: 30px; color: #666666; font-size: 14px;\">If you have any questions, feel free to contact the HR department.</p>" +
                        "<p style=\"color: #999999; font-size: 12px; text-align: center;\">&copy; 2025 AUCA HR Management System. All rights reserved.</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                firstName, email, rawPassword
        );


        // Replace placeholders with real values
        String htmlContent = htmlTemplate
                .replace("{{firstName}}", firstName)
                .replace("{{email}}", email)
                .replace("{{link}}", rawPassword);

        SendEmailDto emailDto = SendEmailDto.builder()
                .to(email)
                .subject("Welcome to AUCA HR System")
                .body(htmlContent)
                .build();

        emailService.sendEmail(emailDto);
    }

}
//    /**
//     * Deactivate user (soft delete)
//     */
//    public User deactivateUser(Long userId) {
//        User user = getUserById(userId);
//        User deactivatedUser = User.builder()
//                .from(user)
//                .active(false)
//                .build();
//        return userRepository.save(deactivatedUser);
//    }
//
//    /**
//     * Activate user
//     */
//    public User activateUser(Long userId) {
//        User user = getUserById(userId);
//        User activatedUser = User.builder()
//                .from(user)
//                .active(true)
//                .build();
//        return userRepository.save(activatedUser);
//    }

//    /**
//     * Change user password
//     */
//    public void changePassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
//        User user = getUserById(userId);
//
//        // Validate current password
//        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPassword())) {
//            throw new InvalidFormatException("Current password is incorrect");
//        }
//
//        // Validate new password
//        validatePassword(passwordChangeDTO.getNewPassword());
//
//        // Confirm new password
//        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
//            throw new InvalidFormatException("New password and confirmation do not match");
//        }
//
//        User updatedUser = User.builder()
//                .from(user)
//                .password(passwordEncoder.encode(passwordChangeDTO.getNewPassword()))
//                .build();
//
//        userRepository.save(updatedUser);
//    }
//
//    /**
//     * Reset user password (admin function)
//     */
//    public String resetPassword(Long userId) {
//        User user = getUserById(userId);
//        String temporaryPassword = generateTemporaryPassword();
//
//        User updatedUser = User.builder()
//                .from(user)
//                .password(passwordEncoder.encode(temporaryPassword))
//                .credentialsNonExpired(false) // Force password change on next login
//                .build();
//
//        userRepository.save(updatedUser);
//        return temporaryPassword;
//    }
