/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.CompressableFile;
import com.microsoft.jenkins.acr.common.UploadRequest;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureStorageBlob;
import com.microsoft.jenkins.acr.util.Constants;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LocalSCM extends AbstractSCM {

    protected LocalSCM(String source) {
        super(source);
    }

    @Override
    public String getSCMUrl() throws Exception {
        this.getLogger().logStatus(Messages.scm_local(this.getSource()));
        UploadRequest request = AzureContainerRegistry.getInstance()
                .getUploadUrl(getResourceGroup(), getAcrName());
        String localFileName = getFileName(request.getRelativePath());
        this.getLogger().logStatus(Messages.compress_filename(localFileName));
        List<String> ignoreList = parseDockerIgnoreFile(Constants.DOCKER_IGNORE);
        ignoreList.addAll(Constants.COMMON_IGNORE);
        this.getLogger().logStatus(Messages.compress_ignore(StringUtils.join(ignoreList, Constants.LIST_SPERATE)));
        String filename = new CompressableFile()
                .withDirectory(this.getSource())
                .withIgnoreList(ignoreList)
                .compress(localFileName);
        this.getLogger().logStatus(Messages.scm_upload(request.getUrl()));
        AzureStorageBlob blob = new AzureStorageBlob(request.getUrl());
        blob.uploadFile(filename);
        return request.getRelativePath();
    }

    private String getFileName(String relativePath) {
        relativePath = "/" + relativePath;
        int index = relativePath.lastIndexOf('/');
        return relativePath.substring(index + 1, relativePath.length());
    }

    private List<String> parseDockerIgnoreFile(String filename) {
        return new ArrayList<>();
    }
}
