/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.util;

public final class Constants {
    private Constants() {
    }

    public static final String PLUGIN_NAME = "AzureJenkinsContainerRegistryBuild";

    public static final String AI = "ACRBuildRequest";
    public static final String AI_QUEUE = "Queue";

    public static final String INVALID_OPTION = "*";

    public static final String LINUX = "Linux";
    public static final String WINDOWS = "Windows";

    public static final String BLOB_COMPLETE = "Complete";
    public static final String BUILD_FAILED = "failed";

    public static final int SLEEP_IN_MS = 1000;
    public static final int DEFAULT_RETRY = 10;

}
