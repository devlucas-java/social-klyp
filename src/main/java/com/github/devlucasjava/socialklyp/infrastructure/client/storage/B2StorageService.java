package com.github.devlucasjava.socialklyp.infrastructure.client.storage;

public class B2StorageService implements StoragePort {

    private final B2StorageClient client;
    private final String bucketId;
    private final String downloadUrl;

    public B2StorageService(B2StorageClient client, String bucketId, String downloadUrl) {
        this.client = client;
        this.bucketId = bucketId;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String upload(byte[] file, String fileName) {

    return "";
    }
}