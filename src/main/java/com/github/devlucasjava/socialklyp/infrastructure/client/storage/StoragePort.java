package com.github.devlucasjava.socialklyp.infrastructure.client.storage;

public interface StoragePort {

    String upload(byte[] file, String fileName);
}