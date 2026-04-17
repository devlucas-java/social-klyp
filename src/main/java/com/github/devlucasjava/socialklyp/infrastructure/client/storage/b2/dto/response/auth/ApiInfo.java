package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.auth;

public record ApiInfo (

    BackupApi backupApi,
    GroupsApi groupsApi,
    StorageApi storageApi
    ){
}
