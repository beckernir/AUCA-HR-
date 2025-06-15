package com.auca_hr.AUCA_HR_System.utils;

import com.auca_hr.AUCA_HR_System.enums.FileType;
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
    private static final String DEFAULT_DOCUMENT = "https://placeholder.com/default-document.pdf";

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;     // 5MB
    private static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20MB

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"));

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = new HashSet<>(
            Arrays.asList("application/pdf", "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "text/plain"));

    @Autowired
    private CloudinaryService cloudinaryService;

    public String processImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return DEFAULT_IMAGE;
        }

        validateFile(file, FileType.IMAGE);
        return uploadFile(file);
    }

    public String processDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return DEFAULT_DOCUMENT;
        }

        validateFile(file, FileType.DOCUMENT);
        return uploadFile(file);
    }

    private void validateFile(MultipartFile file, FileType fileType) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileValidationException("Cannot determine file type.");
        }

        switch (fileType) {
            case IMAGE:
                if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                    throw new FileValidationException("Invalid image format.");
                }
                if (file.getSize() > MAX_IMAGE_SIZE) {
                    throw new FileValidationException("Image size exceeds 5MB limit.");
                }
                break;

            case DOCUMENT:
                if (!ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
                    throw new FileValidationException("Invalid document format.");
                }
                if (file.getSize() > MAX_DOCUMENT_SIZE) {
                    throw new FileValidationException("Document size exceeds 20MB limit.");
                }
                break;
        }
    }

    private String uploadFile(MultipartFile file) {
        try {
            return cloudinaryService.uploadFile(file);
        } catch (Exception e) {
            throw new FileValidationException("File upload failed: " + e.getMessage());
        }
    }
}
