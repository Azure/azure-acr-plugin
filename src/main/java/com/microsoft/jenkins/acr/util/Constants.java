/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class Constants {
    public static final String PLUGIN_NAME = "AzureJenkinsContainerRegistryBuild";

    public static final String AI = "ACRBuildRequest";
    public static final String AI_QUEUE = "Queue";

    public static final String INVALID_OPTION = "*";

    public static final String BLOB_COMPLETE = "Complete";
    public static final String BUILD_SUCCEEDED = "successful";

    public static final int SLEEP_IN_MS = 3000;
    public static final int DEFAULT_RETRY = 20;

    public static final String TEMPFILE = "tarfile";

    public static final String CWD = ".";

    public static final String LOCAL = "local";
    public static final String GIT = "git";

    public static final String GIT_SUFFIX = ".git";
    public static final String GIT_SSH_PREFIX = "git@";

    public static final String COMMENT = "#";
    public static final String EXCLAMATION = "!";
    public static final String FILE_SPERATE = "/";
    public static final String HASHTAG = "#";
    public static final String COLON = ":";
    public static final String SHORT_LIST_SPERATE = ", ";
    public static final String LONG_LIST_SPERATE = "\n    ";
    public static final String DOCKER_IGNORE = ".dockerignore";
    public static final String DOCKERFILE = "Dockerfile";
    public static final List<String> COMMON_IGNORE = ImmutableList.of(".git",
            ".gitignore",
            ".bzr",
            "bzrignore",
            ".hg",
            ".hgignore",
            ".svn");
    public static final int TIMEOUT = 300;

    private Constants() {
    }
}
