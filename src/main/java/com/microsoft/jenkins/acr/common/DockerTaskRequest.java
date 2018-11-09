/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import com.microsoft.jenkins.acr.descriptor.BuildArgument;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Azure Container Request object.
 * It should contains all arguments to queue a quick task to ACR.
 * Each argument should have an entry in the {@link com.microsoft.jenkins.acr.QuickTaskContext}
 */
@Builder
public class DockerTaskRequest extends AbstractRequest {

    /**
     * =========== SCM ================
     */
    /**
     * SCM type: git or local.
     */
    @Getter
    private String sourceType;

    /**
     * SCM git url when the source type is git.
     */
    @Getter
    private String gitRepo;

    /**
     * SCM git branch or pull request.
     */
    @Getter
    private String gitRefspec;

    /**
     * Docker build path in the git repo.
     */
    @Getter
    private String gitPath;

    /**
     * Local directory path when source type is local.
     */
    @Getter
    private String localDir;

    /**
     * Files in a compressed archive on a remote web server.
     */
    @Getter
    private String tarball;

    /**
     * ============ Required ===============
     */

    /**
     * The fully qualified image names including the repository and tag.
     */
    @Getter
    private List<String> imageNames;

    /**
     * ============ Optional ===============
     */

    /**
     * The collection of build arguments to be used.
     */
    @Getter
    private BuildArgument[] buildArguments;

    /**
     * The value of this property indicates whether the image cache is enabled
     * or not.
     */
    @Getter
    private boolean noCache;

    /**
     * Build timeout in seconds.
     */
    @Getter
    private int timeout;

    /**
     * The platform properties against which the build will happen.
     */
    @Getter
    private Platform platform;

    /**
     * The Docker file path relative to the source location.
     */
    @Getter
    private String dockerFilePath;
}
