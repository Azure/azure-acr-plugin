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
import hudson.FilePath;
import jenkins.security.MasterToSlaveCallable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LocalSCMCommand  extends AbstractSCMCommand<LocalSCMCommand.ILocalSCMData> {
    @Override
    protected String getSourceUrl(ILocalSCMData data) throws Exception {
        UploadRequest request = AzureContainerRegistry.getInstance()
                .getUploadUrl(data.getResourceGroupName(), data.getRegistryName());
        LocalSCMCommandOnAgent agent = new LocalSCMCommandOnAgent();
        agent.setWorkspace(data.getJobContext().getWorkspace());
        agent.setSource(data.getLocalSCMRequest().getLocalDir());
        agent.setUrl(request.getUrl());
        String[] filenames = data.getJobContext().getWorkspace().act(agent);
        data.logStatus(Messages.scm_compress_files(StringUtils.join(filenames, Constants.LONG_LIST_SPERATE)));
        data.logStatus(Messages.scm_upload(request.getUrl()));
        return request.getRelativePath();
    }

    public interface ILocalSCMData extends AbstractSCMCommand.ISCMData {
        LocalSCMRequest getLocalSCMRequest();

        String getResourceGroupName();

        String getRegistryName();
    }

    protected static final class LocalSCMCommandOnAgent extends MasterToSlaveCallable<String[], Exception> {

        @Setter
        @Getter
        private FilePath workspace;
        @Setter
        @Getter
        private String url;
        @Setter
        @Getter
        private String source;

        private LocalSCMCommandOnAgent() {
        }

        @Override
        public String[] call() throws Exception {
            FilePath dir = workspace.child(source);
            FilePath tar = dir.createTempFile(Constants.TEMPFILE, ".tar.gz");
            List<String> ignoreList = parseDockerIgnoreFile(dir.child(Constants.DOCKER_IGNORE).getRemote());
            ignoreList.add(tar.getName());
            try {
               String[] filenames = CompressibleFileImpl.compressToFile(tar.getRemote())
                       .withIgnoreList(ignoreList.toArray(new String[ignoreList.size()]))
                       .withDirectory(Util.normalizeFilename(dir.getRemote()))
                       .compress()
                       .fileList();
                AzureStorageBlockBlob blob = new AzureStorageBlockBlob(url);
                blob.uploadFile(tar.getRemote());
                return filenames;
            } finally {
                tar.delete();
            }
        }

        private List<String> parseDockerIgnoreFile(String filename) {
            List<String> list = new ArrayList<>();
            File file = new File(filename);
            if (!file.exists()) {
                return list;
            }

            try (BufferedLineReader reader = new BufferedLineReader(new InputStreamReader(new FileInputStream(file)))) {
                String line = reader.readLine();
                while (line != null) {
                    line = StringUtils.trimToEmpty(line);
                    if (!line.isEmpty() && !line.startsWith(Constants.COMMENT)) {
                        list.add(line);
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // ignore the dockerignore exception here
            }
            return list;
        }
    }
}
