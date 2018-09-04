/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;


import com.microsoft.azure.storage.StorageException;
import com.microsoft.jenkins.acr.Utils;
import com.microsoft.jenkins.acr.common.UploadRequest;
import com.microsoft.jenkins.acr.common.compression.CompressibleFileImpl;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureStorageBlockBlob;
import com.microsoft.jenkins.acr.util.Util;
import lombok.Getter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        AzureContainerRegistry.class,
        AzureStorageBlockBlob.class,
        LocalSCMResolver.class,
        CompressibleFileImpl.class
})
public class LocalSCMTest extends AbstractSCMTest {
    private String dir = "localSCMTest";

    @Mock
    private AzureContainerRegistry containerRegistry;

    @Mock
    private CompressibleFileImpl compressibleFile;

    @Before
    public void prepareDir() throws IOException, StorageException {
        new File(dir).mkdir();
    }

    @After
    public void tearDown() {
        Utils.deleteDir(new File(dir));
    }

    @Override
    protected String getSCMUrl(AbstractSCMRequest request) throws Exception {
        return AbstractSCMResolver.getInstance(request)
                .withLogger(data)
                .withAcrName("acr")
                .withResourceGroup("resourcegroup")
                .getSCMUrl();
    }

    private void mockUploadRequest(String url, String path) {
        PowerMockito.mockStatic(AzureContainerRegistry.class);
        PowerMockito.when(AzureContainerRegistry.getInstance()).thenReturn(containerRegistry);
        UploadRequest uploadRequest = new UploadRequest(url, path);
        PowerMockito.when(containerRegistry.getUploadUrl(anyString(), anyString())).thenReturn(uploadRequest);
    }

    private void mockCompression(String filenameP, String folder, List<String> ignoreList) throws IOException {
        String filename = Util.normalizeFilename(filenameP);
        folder = Util.normalizeFilename(folder);
        PowerMockito.mockStatic(CompressibleFileImpl.class);
        PowerMockito.when(CompressibleFileImpl.compressToFile(filename)).thenReturn(compressibleFile);
        String name = new File(filename).getName();
        ignoreList.add(name);
        PowerMockito.when(compressibleFile.withIgnoreList(ignoreList.toArray(new String[ignoreList.size()])))
                .thenReturn(compressibleFile);
        PowerMockito.when(compressibleFile.withDirectory(folder)).thenReturn(compressibleFile);
        PowerMockito.when(compressibleFile.compress()).thenReturn(compressibleFile);
    }

    private void mockBlob(String filenameP, String url) throws Exception {
        String filename = Util.normalizeFilename(filenameP);
        AzureStorageBlockBlob blockBlob = PowerMockito.mock(AzureStorageBlockBlob.class);
        PowerMockito.whenNew(AzureStorageBlockBlob.class).withArguments(url).thenReturn(blockBlob);
        PowerMockito.doNothing().when(blockBlob).uploadFile(filename);
    }

    @Test
    public void commonTest() throws Exception {
        String relativePath = "src/relative-path-mock.tar.gz";
        String blobUrl = "https://azure-storage-blob-mock/src/relatvice-path-mock.tar.gz";
        String workspace = new File(dir).getAbsolutePath();
        String tarballPath = new File(workspace, "relative-path-mock.tar.gz").getAbsolutePath();

        mockUploadRequest(blobUrl, relativePath);
        mockCompression(tarballPath, workspace, new ArrayList<String>());
        mockBlob(tarballPath, blobUrl);

        String url = getSCMUrl(new Request(workspace));
        Assert.assertEquals("src/relative-path-mock.tar.gz", url);
    }

    @Test
    public void dockerIgnoreTest() throws Exception {
        Utils.writeFile(dir + "/.dockerignore", "a*.txt\n!ab.txt\n# comment", false);
        String relativePath = "src/relative-path-mock.tar.gz";
        String blobUrl = "https://azure-storage-blob-mock/src/relatvice-path-mock.tar.gz";
        String workspace = new File(dir).getAbsolutePath();
        String tarballPath = new File(workspace, "relative-path-mock.tar.gz").getAbsolutePath();

        mockUploadRequest(blobUrl, relativePath);
        List<String> ignoreList = new ArrayList<>();
        ignoreList.add("a*.txt");
        ignoreList.add("!ab.txt");
        mockCompression(tarballPath, workspace, ignoreList);
        mockBlob(tarballPath, blobUrl);

        String url = getSCMUrl(new Request(workspace));
        Assert.assertEquals("src/relative-path-mock.tar.gz", url);
    }


    class Request extends AbstractSCMRequest {
        @Getter
        private final String localDir;

        Request(String local) {
            localDir = local;
        }

        @Override
        public String getSourceType() {
            return "local";
        }
    }
}
