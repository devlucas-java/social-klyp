package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;


@Configuration
class B2StorageConfig {

    @Value("${b2.key-id}")
    private String keyId;

    @Value("${b2.key}")
    private String key;

    @Value("${b2.bucket-id}")
    private String bucketId;


    @Bean
    HttpClient b2HttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    B2AuthAccountClient b2AuthAccountClient(HttpClient b2HttpClient,
                                            ObjectMapper objectMapper) {
        return new B2AuthAccountClient(keyId, key, b2HttpClient, objectMapper);
    }

    @Bean
    B2UploadUrlClient b2UploadUrlClient(B2AuthAccountClient authClient,
                                        HttpClient b2HttpClient,
                                        ObjectMapper objectMapper) {
        return new B2UploadUrlClient(bucketId, authClient, b2HttpClient, objectMapper);
    }

    @Bean
    B2UploadFileClient b2UploadFileClient(HttpClient b2HttpClient,
                                          ObjectMapper objectMapper) {
        return new B2UploadFileClient(b2HttpClient, objectMapper);
    }

    @Bean
    B2DeleteFileClient b2DeleteFileClient(B2AuthAccountClient authClient,
                                          HttpClient b2HttpClient,
                                          ObjectMapper objectMapper) {
        return new B2DeleteFileClient(authClient, b2HttpClient, objectMapper);
    }
}