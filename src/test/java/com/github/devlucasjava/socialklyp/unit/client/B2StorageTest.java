package com.github.devlucasjava.socialklyp.unit.client;

import com.github.devlucasjava.socialklyp.domain.enums.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class B2StorageTest {

    @Autowired
    private StoragePort storagePort;

    @Test
    void uploadFileImageSuccess() {
        byte[] data = "hello world".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);

        var response = storagePort.upload(
                inputStream,
                data.length,
                "test-file.txt",
                "text/plain",
                false,
                MediaType.IMAGE
        );

        assertNotNull(response);
        assertNotNull(response.fileId());
        assertNotNull(response.fileName());
    }

    @Test
    void deleteFileSuccess() {
        byte[] data = "delete test".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);

        var upload = storagePort.upload(
                inputStream,
                data.length,
                "delete-me.txt",
                "text/plain",
                false,
                MediaType.IMAGE
        );

        assertNotNull(upload.fileId());

        assertDoesNotThrow(() ->
                storagePort.delete(upload.fileId(), upload.fileName())
        );
    }

    @Test
    void getPublicUrlSuccess() {
        String url = storagePort.getPublicUrl("fileId", "file.txt");

        assertNotNull(url);
        assertTrue(url.startsWith("https://"));
    }
}