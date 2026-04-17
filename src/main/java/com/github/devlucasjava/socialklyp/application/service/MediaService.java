package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.response.media.MediaResponse;
import com.github.devlucasjava.socialklyp.application.mapper.MediaMapper;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Media;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.enuns.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.MediaRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final PostRepository postRepository;
    private final MediaMapper mediaMapper;
    private final StoragePort storagePort;
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB

    @Transactional(readOnly = true)
    public List<MediaResponse> findAllByPost(UUID postId) {

        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        return mediaRepository.findAllByPostId(postId).stream()
                .map(mediaMapper::toResponse)
                .toList();
    }

    @Transactional
    public MediaResponse uploadToPost(UUID postId, MultipartFile file) {
        validateFile(file);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        MediaType mediaType = resolveMediaType(file.getContentType());

        B2UploadFileResponse result;
        try {
            result = storagePort.upload(
                    file.getInputStream(),
                    file.getSize(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    false,
                    mediaType
            );
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file", e);
        }

        Media media = new Media();
        media.setPost(post);
        media.setMediaId(result.fileId());
        media.setMediaUrl(result.fileUrl());
        media.setMediaName(result.fileName());
        media.setMediaType(mediaType);

        return mediaMapper.toResponse(mediaRepository.save(media));
    }

    @Transactional
    public void delete(UUID mediaId) {

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));

        storagePort.delete(media.getMediaId(), media.getMediaName());
        mediaRepository.delete(media);
    }


    private MediaType resolveMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("video")) {
            return MediaType.VIDEO;
        }
        return MediaType.IMAGE;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        String contentType = file.getContentType();
        long size = file.getSize();

        if (contentType != null && contentType.startsWith("video")) {
            if (size > MAX_VIDEO_SIZE) {
                throw new IllegalArgumentException("Video exceeds 50MB limit");
            }
        } else {
            if (size > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("Image exceeds 5MB limit");
            }
        }
    }
}