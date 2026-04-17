package com.github.devlucasjava.socialklyp.infrastructure.client.port;

import com.github.devlucasjava.socialklyp.domain.enuns.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;

public interface StoragePort {


    B2UploadFileResponse upload(byte[] fileData, String fileName, String contentType, boolean business, MediaType mediaType);

    void delete(String fileId, String fileName);

    String getPublicUrl(String fileId, String fileName);
}