package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.response.media.MediaResponse;
import com.github.devlucasjava.socialklyp.application.mapper.MediaMapper;
import com.github.devlucasjava.socialklyp.application.service.MediaService;
import com.github.devlucasjava.socialklyp.application.validator.FileValidator;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.FileReadException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.InvalidFileException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Media;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.enums.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.MediaRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @InjectMocks
    private MediaService mediaService;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private MediaMapper mediaMapper;

    @Mock
    private StoragePort storagePort;

    @Mock
    private MultipartFile multipartFile;

    private Post post;
    private Media media;
    private MediaResponse mediaResponse;

    private final UUID postId = UUID.randomUUID();
    private final UUID mediaId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());

        post = new Post();
        post.setId(postId);
        post.setContent("Post content");
        post.setProfile(profile);

        media = new Media();
        media.setId(mediaId);
        media.setMediaId("b2-file-id");
        media.setMediaName("image.jpg");
        media.setMediaUrl("https://cdn.example.com/image.jpg");
        media.setMediaType(MediaType.IMAGE);
        media.setPost(post);
        media.setCreatedAt(LocalDateTime.now());
        media.setUpdatedAt(LocalDateTime.now());

        mediaResponse = new MediaResponse();
        mediaResponse.setId(mediaId);
        mediaResponse.setMediaUrl("https://cdn.example.com/image.jpg");
        mediaResponse.setMediaName("image.jpg");
        mediaResponse.setMediaType("IMAGE");
        mediaResponse.setPostId(postId);
    }

    // -------------------- FIND ALL BY POST --------------------

    @Test
    void shouldFindAllMediaByPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(mediaRepository.findAllByPostId(postId)).thenReturn(List.of(media));
        when(mediaMapper.toResponse(media)).thenReturn(mediaResponse);

        List<MediaResponse> result = mediaService.findAllByPost(postId);

        assertEquals(1, result.size());
        assertEquals(mediaId, result.get(0).getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoMediaForPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(mediaRepository.findAllByPostId(postId)).thenReturn(List.of());

        List<MediaResponse> result = mediaService.findAllByPost(postId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenPostNotFoundOnFindAll() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> mediaService.findAllByPost(postId));
    }

    // -------------------- UPLOAD TO POST --------------------

    @Test
    void shouldUploadMediaToPostSuccessfully() throws IOException {
        B2UploadFileResponse b2Response = new B2UploadFileResponse(
                "b2-file-id", "image.jpg", "https://cdn.example.com/image.jpg",
                "account-id", "bucket-id", 1024L, "sha1", "image/jpeg"
        );

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(storagePort.upload(any(InputStream.class), anyLong(), anyString(), anyString(), anyBoolean(), eq(MediaType.IMAGE)))
                .thenReturn(b2Response);
        when(mediaRepository.save(any(Media.class))).thenReturn(media);
        when(mediaMapper.toResponse(media)).thenReturn(mediaResponse);

        MediaResponse result = mediaService.uploadToPost(postId, multipartFile);

        assertNotNull(result);
        assertEquals(mediaId, result.getId());
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void shouldThrowWhenPostNotFoundOnUpload() {
        // fileValidator passes, but post is not found
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> mediaService.uploadToPost(postId, multipartFile));

        verify(mediaRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenFileIsInvalidOnUpload() {
        doThrow(new InvalidFileException("Only images allowed"))
                .when(fileValidator).validateImageOnly(multipartFile);

        assertThrows(InvalidFileException.class,
                () -> mediaService.uploadToPost(postId, multipartFile));

        verify(postRepository, never()).findById(any());
        verify(mediaRepository, never()).save(any());
    }

    @Test
    void shouldThrowFileReadExceptionWhenIOExceptionOccurs() throws IOException {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(multipartFile.getInputStream()).thenThrow(new IOException("disk error"));

        assertThrows(FileReadException.class,
                () -> mediaService.uploadToPost(postId, multipartFile));

        verify(mediaRepository, never()).save(any());
    }

    // -------------------- DELETE --------------------

    @Test
    void shouldDeleteMediaSuccessfully() {
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

        mediaService.delete(mediaId);

        verify(storagePort).delete(media.getMediaId(), media.getMediaName());
        verify(mediaRepository).delete(media);
    }

    @Test
    void shouldThrowWhenMediaNotFoundOnDelete() {
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> mediaService.delete(mediaId));

        verify(storagePort, never()).delete(anyString(), anyString());
        verify(mediaRepository, never()).delete(any());
    }
}
