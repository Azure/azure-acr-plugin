/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.BufferedLineReader;
import com.microsoft.jenkins.acr.common.compression.CompressibleFileImpl;
import com.microsoft.jenkins.acr.common.UploadRequest;
import com.microsoft.jenkins.acr.common.compression.Compression;
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
        String tarFilename = Util.getFileName(request.getRelativePath());
        String localFileName = Util.concatPath(source, tarFilename);
        this.getLogger().logStatus(Messages.scm_compress_filename(localFileName));
        List<String> ignoreList = parseDockerIgnoreFile(Util.concatPath(this.source, Constants.DOCKER_IGNORE));
        ignoreList.add(tarFilename);
        this.getLogger().logStatus(
                Messages.scm_compress_ignore(StringUtils.join(ignoreList, Constants.SHORT_LIST_SPERATE)));
        try {
            Compression.CompressibleWithFile temp = CompressibleFileImpl.compressToFile(localFileName)
                    .withIgnoreList(ignoreList.toArray(new String[ignoreList.size()]));

            String[] filenames = CompressibleFileImpl.compressToFile(localFileName)
                    .withIgnoreList(ignoreList.toArray(new String[ignoreList.size()]))
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
            new File(localFileName).delete();
        }
        return request.getRelativePath();
    }

    private List<String> parseDockerIgnoreFile(String filename) {
        List<String> list = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            return list;
        }

        try {
            BufferedLineReader reader = null;
            try {
                reader = new BufferedLineReader(new InputStreamReader(new FileInputStream(file)));
                String line = reader.readLine();
                while (line != null) {
                    line = StringUtils.trimToEmpty(line);
                    if (!line.isEmpty() && !line.startsWith(Constants.COMMENT)) {
                        list.add(line);
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
            } finally {
                reader.close();
            }
        } catch (IOException e) {
        }
        return list;
    }
}
