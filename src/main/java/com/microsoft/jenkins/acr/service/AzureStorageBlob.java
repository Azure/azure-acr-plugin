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
import com.microsoft.jenkins.acr.common.BufferedLineReader;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.acr.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

/**
 * Azure Storage append blob.
 */
public class AzureStorageBlob extends AzureService {
    private final CloudAppendBlob blob;
    private BufferedLineReader reader;
    private long offset;

    public AzureStorageBlob(String link) throws URISyntaxException, ServiceException {
        try {
            this.blob = new CloudAppendBlob(new StorageUri(new URI(link)));
            this.offset = 0L;
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

    /**
     * Read next line from this Azure Storage blob.
     *
     * @return Next line
     * @throws Exception {@link IOException}, {@link ServiceException}, {@link InterruptedException}
     */
    public String readLine() throws Exception {
        if (this.reader == null) {
            if (this.offset == 0) {  // blob may not ready at the very beginning
                this.reader = createReaderWithRetry();
            } else {
                this.reader = createReader();
                this.reader.skipLines(this.offset);
            }
        }

        try {
            String line = this.reader.readLine();

            if (line != null) {
                this.offset++;
            }

            // If the blob is finished, null line means the blob is really finish read.
            if (line != null || this.isFinished()) {
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //skip and retry
        }


        try {
            // read to current end, but service continue writes the stream.
            this.reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            // skip
        }
        this.reader = null;
        return readLine();
    }

    /**
     * If the append blob is finished, service will set the metadata[Completed] with success or failed value.
     *
     * @return whether the metadata contains Completed key.
     */
    private boolean isFinished() {
        return this.blob.getMetadata().containsKey(Constants.BLOB_COMPLETE);
    }

    /**
     * Create a stream reader to the latest stream.
     *
     * @return BufferedLineReader with blob stream.
     * @throws ServiceException Failed to get the blob stream.
     */
    private BufferedLineReader createReader() throws ServiceException {
        return new BufferedLineReader(new InputStreamReader(this.getStream()));
    }

    /**
     * Retry logic of {@link #createReader()} to get {@link BufferedLineReader}.
     *
     * @return BufferedLineReader with blob stream.
     * @throws Exception {@link ServiceException} means failed to get blob stream,
     *                   {@link InterruptedException} exception when sleeping.
     */
    private BufferedLineReader createReaderWithRetry() throws Exception {
        return Util.retry(new Callable<BufferedLineReader>() {
            @Override
            public BufferedLineReader call() throws Exception {
                return createReader();
            }
        }, Constants.DEFAULT_RETRY);
    }

    /**
     * Download the latest input stream of the blob. This method will invoke a service call.
     *
     * @return input stream
     * @throws ServiceException When there is some Azure service exception
     */
    private InputStream getStream() throws ServiceException {
        try {
            return blob.openInputStream();
        } catch (StorageException e) {
            e.printStackTrace();
            throw new ServiceException("Storage", Messages.log_actionName(), e.getMessage());
        }
    }
}
