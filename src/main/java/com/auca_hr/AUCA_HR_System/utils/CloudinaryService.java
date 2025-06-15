package com.auca_hr.AUCA_HR_System.utils;

import com.auca_hr.AUCA_HR_System.exceptions.FileValidationException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads images or documents to Cloudinary
     */
    public String uploadFile(MultipartFile file) {
        File tempFile = null;
        try {
            logger.info("Starting upload: name={}, size={}, type={}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            // Create temp file with a unique name
            String extension = getExtension(file);
            tempFile = File.createTempFile(UUID.randomUUID().toString(), extension);
            file.transferTo(tempFile);

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "auto",   // Let Cloudinary auto-detect
                    "unique_filename", true,
                    "overwrite", false
            );

            Map<String, Object> result = cloudinary.uploader().upload(tempFile, uploadParams);

            logger.info("Upload successful: {}", result.get("secure_url"));
            return (String) result.get("secure_url");

        } catch (IOException e) {
            logger.error("Upload failed", e);
            throw new FileValidationException("Error uploading file: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    logger.warn("Failed to delete temp file", e);
                }
            }
        }
    }

    /**
     * Gets file extension from content type or filename
     */
    private String getExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return ".dat";
        }

        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "application/pdf" -> ".pdf";
            case "application/msword" -> ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            case "text/plain" -> ".txt";
            default -> ".dat";
        };
    }
}
