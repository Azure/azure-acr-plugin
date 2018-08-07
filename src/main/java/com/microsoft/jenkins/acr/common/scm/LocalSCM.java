/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.compression.CompressibleFileImpl;
import com.microsoft.jenkins.acr.common.UploadRequest;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureStorageBlockBlob;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.acr.util.Util;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public class LocalSCM extends AbstractSCM {

    protected LocalSCM(String source) {
        super(source);
    }

    @Override
    public String getSCMUrl() throws Exception {
        this.getLogger().logStatus(Messages.scm_local(this.getSource()));
        UploadRequest request = AzureContainerRegistry.getInstance()
                .getUploadUrl(getResourceGroup(), getAcrName());
        String localFileName = Util.getFileName(request.getRelativePath());
        this.getLogger().logStatus(Messages.scm_compress_filename(localFileName));
        String[] ignoreList = parseDockerIgnoreFile(Constants.DOCKER_IGNORE);
        this.getLogger().logStatus(
                Messages.scm_compress_ignore(StringUtils.join(ignoreList, Constants.SHORT_LIST_SPERATE)));
        try {
            String[] filenames = CompressibleFileImpl.compressToFile(localFileName)
                    .withIgnoreList(ignoreList)
                    .withDirectory(this.getSource())
                    .compress()
                    .fileList();
            this.getLogger().logStatus(
                    Messages.scm_compress_files(StringUtils.join(filenames, Constants.LONG_LIST_SPERATE)));
            this.getLogger().logStatus(Messages.scm_upload(request.getUrl()));
            AzureStorageBlockBlob blob = new AzureStorageBlockBlob(request.getUrl());
            blob.uploadFile(localFileName);
        } catch (Exception e) {
            throw e;
        } finally {
            new File(localFileName).deleteOnExit();
        }
        return request.getRelativePath();
    }

    private String[] parseDockerIgnoreFile(String filename) {
        return new String[0];
    }
}
