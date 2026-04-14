package com.github.devlucasjava.socialklyp.infrastructure.client.storage;

import lombok.RequiredArgsConstructor;

import java.net.URL;
import java.net.http.HttpClient;

@RequiredArgsConstructor
public class B2StorageClient {

    private final String keyId;
    private final String applicationKey;
    private HttpClient httpClient;

    private String apiUrl;
    private String authToken;

    public void authorize() throws Exception {
        URL url = new URL("https://api.backblazeb2.com/b2api/v2/b2_authorize_account");

    }

    public String getUploadUrl(String bucketId) throws Exception {
        URL url = new URL(apiUrl + "/b2api/v2/b2_get_upload_url");

        return "";
    }

    public String uploadFile(String uploadUrl, String token, byte[] file, String fileName) throws Exception {
        URL url = new URL(uploadUrl);

        return "";
    }

    public void deleteFile(String fileId) throws Exception {
        URL url = new URL(apiUrl + "/b2api/v2/b2_delete_file_version");
    }

    public String getUrlFile(String fileId) {

        return "";
    }

    public String getApiUrl() {
        return "";
    }

}
