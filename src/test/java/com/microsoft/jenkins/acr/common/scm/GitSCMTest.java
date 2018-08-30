/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;


public class GitSCMTest extends AbstractSCMTest {

    @Test
    public void commonTest() throws Exception {
        String repo = "https://github.com/Azure/azure-acr-plugin.git";
        String branch = "master";
        String path = null;
        String url = getSCMUrl(new Request(repo, branch, path));
        Assert.assertEquals(repo + "#" + branch, url);
    }

    @Test
    public void pathTest() throws Exception {
        String repo = "https://github.com/Azure/azure-acr-plugin.git";
        String branch = null;
        String path = "src";
        String url = getSCMUrl(new Request(repo, branch, path));
        Assert.assertEquals(repo + ":" + path, url);
    }

    @Test
    public void fullTest() throws Exception {
        String repo = "https://github.com/Azure/azure-acr-plugin.git";
        String branch = "master";
        String path = "src";
        String url = getSCMUrl(new Request(repo, branch, path));
        Assert.assertEquals(repo + "#" + branch + ":" + path, url);
    }

    @Test
    public void repoCompleteTest() throws Exception {
        String repo = "https://github.com/Azure/azure-acr-plugin";
        String branch = "master";
        String path = null;
        String url = getSCMUrl(new Request(repo, branch, path));
        Assert.assertEquals(repo + ".git#" + branch, url);
    }

    @Test
    public void repoResolveTest() throws Exception {
        String repo = "https://github.com/Azure/azure-acr-plugin/";
        String branch = "master";
        String path = null;
        String url = getSCMUrl(new Request(repo, branch, path));
        Assert.assertEquals("https://github.com/Azure/azure-acr-plugin.git#" + branch, url);
    }

    @Test
    public void verifyLocation() {
        Assert.assertTrue(GitSCMResolver.verifyLocation(null));
        Assert.assertTrue(GitSCMResolver.verifyLocation(""));
        Assert.assertTrue(GitSCMResolver.verifyLocation("https://github.com/Azure/azure-acr-plugin.git"));
        Assert.assertTrue(GitSCMResolver.verifyLocation("https://github.com/Azure/azure-acr-plugin"));
        Assert.assertTrue(GitSCMResolver.verifyLocation("https://github.com/Azure/azure-acr-plugin/"));
        Assert.assertTrue(GitSCMResolver.verifyLocation("http://github.com/Azure/azure-acr-plugin/"));
        Assert.assertFalse(GitSCMResolver.verifyLocation("git@github.com:Azure/azure-acr-plugin.git"));
    }

    protected String getSCMUrl(AbstractSCMRequest request) throws Exception {
        return AbstractSCMResolver.getInstance(request).withLogger(data).getSCMUrl();
    }

    class Request extends AbstractSCMRequest {
        @Getter
        private final String gitRepo;
        @Getter
        private final String gitRef;
        @Getter
        private final String gitPath;

        public Request(String repo, String ref, String path) {
            this.gitRepo = repo;
            this.gitRef = ref;
            this.gitPath = path;
        }

        @Override
        public String getSourceType() {
            return "git";
        }
    }
}
