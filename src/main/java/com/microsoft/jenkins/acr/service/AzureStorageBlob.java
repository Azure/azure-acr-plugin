/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;


import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.exception.ServiceException;
import com.microsoft.jenkins.acr.util.Constants;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class AzureStorageBlob extends AzureService {
    private final CloudAppendBlob blob;

    public AzureStorageBlob(String link) throws URISyntaxException, ServiceException {
        try {
            this.blob = new CloudAppendBlob(new StorageUri(new URI(link)));
        } catch (StorageException e) {
            e.printStackTrace();
            throw new ServiceException("Storage", Messages.log_actionName(), e.getMessage());
        }
    }

    /**
     * Whether the blob is completely uploaded with result.
     *
     * @return {@code true} or {@code false} or {@code null} indicates not finished
     */
    public Boolean isSuccess() {

        String result = this.blob.getMetadata().get(Constants.BLOB_COMPLETE);
        if (result == null) {
            return null;
        }

        return !result.equals(Constants.BUILD_FAILED);
    }

    public boolean isFinished() {
        return this.blob.getMetadata().containsKey(Constants.BLOB_COMPLETE);
    }

    private String getEncoding() {
        return this.blob.getProperties().getContentEncoding();
    }

    /**
     * Get the input stream of the blob.
     *
     * @return input stream
     * @throws ServiceException When there is some Azure service exception
     */
    public InputStream getStream() throws ServiceException {
        try {
            return blob.openInputStream();
        } catch (StorageException e) {
            e.printStackTrace();
            throw new ServiceException("Storage", Messages.log_actionName(), e.getMessage());
        }
    }
}
