/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.BufferedLineReader;
import com.microsoft.jenkins.acr.common.UploadRequest;
import com.microsoft.jenkins.acr.common.compression.CompressibleFileImpl;
import com.microsoft.jenkins.acr.common.scm.LocalSCMRequest;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureStorageBlockBlob;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.acr.util.Util;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LocalSCMCommand  extends AbstractSCMCommand<LocalSCMCommand.ILocalSCMData> {
    @Override
    String getSourceUrl(ILocalSCMData data) throws Exception {
        String source = data.getLocalSCMRequest().getLocalDir();
        data.logStatus(Messages.scm_local(source));
        UploadRequest request = AzureContainerRegistry.getInstance()
                .getUploadUrl(data.getResourceGroupName(), data.getRegistryName());
        String tarFilename = Util.getFileName(request.getRelativePath());
        String localFileName = Util.concatPath(source, tarFilename);
        data.logStatus(Messages.scm_compress_filename(localFileName));
        List<String> ignoreList = parseDockerIgnoreFile(Util.concatPath(source, Constants.DOCKER_IGNORE));
        ignoreList.add(tarFilename);
        data.logStatus(
                Messages.scm_compress_ignore(StringUtils.join(ignoreList, Constants.SHORT_LIST_SPERATE)));
        try {
            String[] filenames = CompressibleFileImpl.compressToFile(localFileName)
                    .withIgnoreList(ignoreList.toArray(new String[ignoreList.size()]))
                    .withDirectory(Util.normalizeFilename(source))
                    .compress()
                    .fileList();
            data.logStatus(
                    Messages.scm_compress_files(StringUtils.join(filenames, Constants.LONG_LIST_SPERATE)));
            data.logStatus(Messages.scm_upload(request.getUrl()));
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

    public interface ILocalSCMData extends AbstractSCMCommand.ISCMData {
        LocalSCMRequest getLocalSCMRequest();

        String getResourceGroupName();

        String getRegistryName();
    }
}
