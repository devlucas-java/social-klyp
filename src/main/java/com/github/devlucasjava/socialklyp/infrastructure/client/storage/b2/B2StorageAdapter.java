package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2;

import com.github.devlucasjava.socialklyp.domain.enums.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2GetUploadUrlResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;


@Component
@RequiredArgsConstructor
public class B2StorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(B2StorageAdapter.class);

    private final B2UploadUrlClient uploadUrlClient;
    private final B2UploadFileClient uploadFileClient;
    private final B2DeleteFileClient deleteFileClient;


    @Override
    public B2UploadFileResponse upload(
            InputStream fileStream,
            long contentLength,
            String fileName,
            String contentType,
            boolean business,
            MediaType mediaType
    ) {
        log.info("Starting B2 upload. fileName={} contentType={} sizeBytes={}",
                fileName, contentType, contentLength);

        B2GetUploadUrlResponse uploadUrl = uploadUrlClient.fetchUploadUrl();

        String storageKey = resolve(mediaType, business, fileName);

        return uploadFileClient.uploadFile(
                uploadUrl,
                fileStream,
                contentLength,
                storageKey,
                contentType
        );
    }


    @Override
    public void delete(String fileId, String fileName) {
        log.info("Starting B2 delete. fileId={} fileName={}", fileId, fileName);

        deleteFileClient.deleteFile(fileId, fileName);

        log.info("B2 delete complete. fileId={} fileName={}", fileId, fileName);
    }

    @Override
    public String getPublicUrl(String fileId, String fileName) {

        String url = "https://f000.backblazeb2.com/file/" + fileId + "/" + fileName;
        log.info("B2 Create url complete. fileId={} fileName={}, url={}", fileId, fileName, url);
        return url;
    }


    public static String resolve(MediaType mediaType, boolean business, String fileName) {

        String baseFolder = resolveBaseFolder(mediaType, business);

        return baseFolder + "/" + fileName;
    }

    private static String resolveBaseFolder(MediaType mediaType, boolean business) {

        String prefix = business ? "business" : "users";

        return switch (mediaType) {
            case IMAGE -> prefix + "/photos";
            default -> prefix + "/misc";
        };
    }

}