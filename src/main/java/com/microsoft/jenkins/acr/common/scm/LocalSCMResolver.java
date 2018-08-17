/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.BufferedLineReader;
import com.microsoft.jenkins.acr.common.compression.CompressibleFileImpl;
import com.microsoft.jenkins.acr.common.UploadRequest;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureStorageBlockBlob;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.acr.util.Util;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LocalSCMResolver extends AbstractSCMResolver {

    private final String source;

    protected LocalSCMResolver(LocalSCMRequest request) {
        this.source = request.getLocalDir();
    }

    @Override
    public String getSCMUrl() throws Exception {
        this.getLogger().logStatus(Messages.scm_local(this.source));
        UploadRequest request = AzureContainerRegistry.getInstance()
                .getUploadUrl(getResourceGroup(), getAcrName());
        String localFileName = Util.getFileName(request.getRelativePath());
        this.getLogger().logStatus(Messages.scm_compress_filename(localFileName));
        String[] ignoreList = parseDockerIgnoreFile(this.source
                + Constants.FILE_SPERATE
                + Constants.DOCKER_IGNORE);
        this.getLogger().logStatus(
                Messages.scm_compress_ignore(StringUtils.join(ignoreList, Constants.SHORT_LIST_SPERATE)));
        try {
            String[] filenames = CompressibleFileImpl.compressToFile(localFileName)
                    .withIgnoreList(ignoreList)
                    .withDirectory(this.source)
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
        File file = new File(filename);
        if (!file.exists()) {
            return new String[0];
        }

        List<String> list = new ArrayList<>();
        try {
            BufferedLineReader reader = new BufferedLineReader(new InputStreamReader(new FileInputStream(file)));
            String line = reader.readLine();
            while (line != null) {
                line = StringUtils.trimToEmpty(line);
                if (!line.isEmpty() && !line.startsWith(Constants.COMMENT)) {
                    list.add(line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            return new String[0];
        }
        return list.toArray(new String[list.size()]);
    }
}