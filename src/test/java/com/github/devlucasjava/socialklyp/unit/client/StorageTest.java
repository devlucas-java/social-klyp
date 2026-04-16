//package com.github.devlucasjava.socialklyp.unit.client;
//
//import com.github.devlucasjava.socialklyp.domain.enuns.MediaType;
//import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class StorageTest {
//
//    @Autowired
//    private StoragePort storagePort;
//
//    @BeforeEach
//    public void setup() {
//
//
//    }
//
//    @Test
//    void uploadFileVideoSuccess() {
//        byte[] data = "hello world".getBytes();
//
//        var response = storagePort.upload(
//                data,
//                "test-file.txt",
//                "text/plain",
//                false, MediaType.VIDEO
//        );
//
//        assertNotNull(response.fileId());
//        assertNotNull(response.fileName());
//    }
//    @Test
//    void uploadFileImageSuccess() {
//        byte[] data = "hello world".getBytes();
//
//        var response = storagePort.upload(
//                data,
//                "test-file.txt",
//                "text/plain",
//                false, MediaType.IMAGE
//        );
//
//        assertNotNull(response.fileId());
//        assertNotNull(response.fileName());
//    }
//
//    @Test
//    void deleteFileSuccess() {
//        byte[] data = "delete test".getBytes();
//
//        var upload = storagePort.upload(
//                data,
//                "delete-me.txt",
//                "text/plain",
//                false, MediaType.IMAGE
//        );
//
//        storagePort.delete(upload.fileId(), upload.fileName(), false, MediaType.IMAGE);
//    }
//
//    @Test
//    void getPublicUrlSuccess() {
//        String url = storagePort.getPublicUrl("fileId", "file.txt", false, MediaType.IMAGE);
//
//        assertTrue(url.contains("https://"));
//    }
//}
//
