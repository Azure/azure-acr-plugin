/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.command.scm;

public interface GitSCMRequest {
    String getGitRepo();

    String getGitRefspec();

    String getGitPath();
}
