package com.github.devlucasjava.socialklyp.application.validator;

import com.github.devlucasjava.socialklyp.delivery.rest.advice.InvalidFileException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;

    public void validateImageOrVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File must not be empty");
        }

        String contentType = file.getContentType();
        long size = file.getSize();

        if (contentType == null) {
            throw new InvalidFileException("Invalid file type");
        }

        if (contentType.startsWith("video")) {
            if (size > MAX_VIDEO_SIZE) {
                throw new InvalidFileException("Video exceeds 50MB limit");
            }
        } else if (contentType.startsWith("image")) {
            if (size > MAX_IMAGE_SIZE) {
                throw new InvalidFileException("Image exceeds 5MB limit");
            }
        } else {
            throw new InvalidFileException("Only image and video are allowed");
        }
    }

    public void validateImageOnly(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File must not be empty");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidFileException("Image exceeds 5MB limit");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image")) {
            throw new InvalidFileException("Only images allowed");
        }
    }
}