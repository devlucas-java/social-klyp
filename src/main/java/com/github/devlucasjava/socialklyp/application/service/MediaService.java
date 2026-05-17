package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.response.media.MediaResponse;
import com.github.devlucasjava.socialklyp.application.mapper.MediaMapper;
import com.github.devlucasjava.socialklyp.application.validator.FileValidator;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.FileReadException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Media;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.enums.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.MediaRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final PostRepository postRepository;
    private final FileValidator fileValidator;
    private final MediaMapper mediaMapper;
    private final StoragePort storagePort;

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
        fileValidator.validateImageOnly(file);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        B2UploadFileResponse result;
        try {
            result = storagePort.upload(
                    file.getInputStream(),
                    file.getSize(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    false,
                    MediaType.IMAGE
            );
        } catch (IOException e) {
            throw new FileReadException("Error uploading file");
        }

        Media media = new Media();
        media.setPost(post);
        media.setMediaId(result.fileId());
        media.setMediaUrl(result.fileUrl());
        media.setMediaName(result.fileName());
        media.setMediaType(MediaType.IMAGE);

        return mediaMapper.toResponse(mediaRepository.save(media));
    }

    @Transactional
    public void delete(UUID mediaId) {

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));

        storagePort.delete(media.getMediaId(), media.getMediaName());
        mediaRepository.delete(media);
    }
}