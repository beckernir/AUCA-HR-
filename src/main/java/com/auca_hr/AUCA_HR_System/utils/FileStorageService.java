package com.auca_hr.AUCA_HR_System.utils;

import com.auca_hr.AUCA_HR_System.exceptions.FileValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class FileStorageService {

    private static final String DEFAULT_IMAGE = "https://placeholder.com/default-image.jpg";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    // Allow any common image formats
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(
            Arrays.asList(
                    "image/jpeg",
                    "image/jpg",
                    "image/png",
                    "image/gif",
                    "image/bmp",
                    "image/webp",
                    "image/tiff",
                    "image/svg+xml"
            ));

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Process and upload an image file
     * @param file The multipart file to upload
     * @return URL of the uploaded image or default image URL if file is null/empty
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return DEFAULT_IMAGE;
        }

        validateImageFile(file);
        return uploadFile(file);
    }

    /**
     * Validate image file type and size
     * @param file The file to validate
     * @throws FileValidationException if validation fails
     */
    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new FileValidationException("Cannot determine file type.");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new FileValidationException("Invalid image format. Allowed formats: JPEG, PNG, GIF, BMP, WEBP, TIFF, SVG");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new FileValidationException("Image size exceeds 5MB limit.");
        }
    }

    /**
     * Upload file to cloud storage
     * @param file The file to upload
     * @return URL of the uploaded file
     * @throws FileValidationException if upload fails
     */
    private String uploadFile(MultipartFile file) {
        try {
            return cloudinaryService.uploadFile(file);
        } catch (Exception e) {
            throw new FileValidationException("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * Get the default image URL
     * @return Default image URL
     */
    public String getDefaultImageUrl() {
        return DEFAULT_IMAGE;
    }
}