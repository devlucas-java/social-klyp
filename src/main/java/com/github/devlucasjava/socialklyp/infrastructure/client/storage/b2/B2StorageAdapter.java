package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2;

import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2GetUploadUrlResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class B2StorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(B2StorageAdapter.class);

    private final B2UploadUrlClient  uploadUrlClient;
    private final B2UploadFileClient uploadFileClient;
    private final B2DeleteFileClient deleteFileClient;


    @Override
    public B2UploadFileResponse upload(byte[] fileData, String fileName, String contentType) {
        log.info("Starting B2 upload. fileName={} contentType={} sizeBytes={}",
                fileName, contentType, fileData.length);

        B2GetUploadUrlResponse uploadUrl = uploadUrlClient.fetchUploadUrl();

        B2UploadFileResponse result = uploadFileClient.uploadFile(uploadUrl, fileData, fileName, contentType);

        log.info("B2 upload complete. fileId={} fileName={}", result.fileId(), result.fileName());
        return result;
    }


    @Override
    public void delete(String fileId, String fileName) {
        log.info("Starting B2 delete. fileId={} fileName={}", fileId, fileName);
        deleteFileClient.deleteFile(fileId, fileName);
        log.info("B2 delete complete. fileId={} fileName={}", fileId, fileName);
    }
}