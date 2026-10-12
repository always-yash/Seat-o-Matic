package com.lostfound.member2_items.util;

import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility for Secure Image File Uploads.
 * Enforces file size limits, MIME type inspection, extension whitelist,
 * and random UUID renaming to prevent arbitrary file upload vulnerabilities.
 */
public class ImageUploadUtil {

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "webp")
    );

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/webp")
    );

    private ImageUploadUtil() {
        // Utility class
    }

    /**
     * Validates and saves an uploaded file Part.
     *
     * @param part The uploaded Part from HttpServletRequest
     * @param uploadDirRealPath The absolute real path to upload directory (e.g., .../uploads/items)
     * @return The relative web path (e.g. "uploads/items/abc.jpg") or null if no file provided
     * @throws IllegalArgumentException on security or format validation error
     * @throws IOException on I/O error
     */
    public static String saveUploadedImage(Part part, String uploadDirRealPath) throws IOException {
        if (part == null || part.getSize() == 0 || part.getSubmittedFileName() == null || part.getSubmittedFileName().trim().isEmpty()) {
            return null;
        }

        // 1. Validate file size
        if (part.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Uploaded file exceeds the maximum allowed size of 5 MB.");
        }

        // 2. Extract and sanitize file extension
        String submittedFilename = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        String extension = "";
        int dotIndex = submittedFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < submittedFilename.length() - 1) {
            extension = submittedFilename.substring(dotIndex + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file format ." + extension + ". Allowed formats: JPG, JPEG, PNG, WEBP.");
        }

        // 3. Validate MIME type
        String contentType = part.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid MIME type (" + contentType + "). Only image files are permitted.");
        }

        // 4. Ensure destination directory exists
        if (uploadDirRealPath == null || uploadDirRealPath.trim().isEmpty()) {
            uploadDirRealPath = System.getProperty("java.io.tmpdir") + File.separator + "lostfound_uploads";
        }
        File uploadDir = new File(uploadDirRealPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 5. Generate secure random filename
        String safeFileName = UUID.randomUUID().toString() + "." + extension;
        Path targetPath = Paths.get(uploadDirRealPath, safeFileName);

        // 6. Write file stream safely
        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return relative database path
        return "uploads/items/" + safeFileName;
    }
}
