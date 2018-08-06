/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.CompressableFile;
import com.microsoft.jenkins.acr.common.UploadRequest;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class LocalSCM extends AbstractSCM {

    protected LocalSCM(String source) {
        super(source);
    }

    @Override
    public String getSCMUrl() {
        this.getLogger().logStatus(Messages.scm_local(this.getSource()));
        UploadRequest request = AzureContainerRegistry.getInstance()
                .getUploadUrl(getResourceGroup(), getAcrName());
        this.getLogger().logStatus(Messages.scm_upload(request.getUrl()));
        List<String> ignoreList = parseDockerIgnoreFile(Constants.DOCKER_IGNORE);
        ignoreList.addAll(Constants.COMMON_IGNORE);

        String filname = new CompressableFile()
                .withDirectory(this.getSource())
                .withIgnoreList(ignoreList)
                .compress(getFileName(request.getRelativePath()));
        return request.getRelativePath();
    }

    private String getFileName(String relativePath) {
        return "temp.tar.gz";
    }

    private List<String> parseDockerIgnoreFile(String filename) {
        return new ArrayList<>();
    }
}
