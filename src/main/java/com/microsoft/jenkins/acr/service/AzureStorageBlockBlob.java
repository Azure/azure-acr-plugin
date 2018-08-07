/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.exception.ServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AzureStorageBlockBlob {
    private final CloudBlockBlob blob;

    public AzureStorageBlockBlob(String link) throws URISyntaxException, ServiceException {
        try {
            this.blob = new CloudBlockBlob(new StorageUri(new URI(link)));
        } catch (StorageException e) {
            e.printStackTrace();
            throw new ServiceException("Storage", Messages.scm_upload_actionName(), e.getMessage());
        }
    }
    /**
     * Write a file to the blob and upload it.
     * @param filename filename
     */
    public void uploadFile(String filename) throws IOException, StorageException {
        this.blob.uploadFromFile(filename);
    }
}
