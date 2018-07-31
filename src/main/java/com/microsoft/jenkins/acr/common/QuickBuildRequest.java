/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import com.microsoft.jenkins.acr.descriptor.Image;
import com.microsoft.jenkins.acr.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Container Request object.
 * It should contains all arguments to queue a quick build to ACR.
 * Each argument should have an entry in the {@link com.microsoft.jenkins.acr.QuickBuildBuilder}
 */
public class QuickBuildRequest {
    /**
     * ============ Required ===============
     */

    /**
     * The fully qualified image names including the repository and tag.
     */
    private List<String> imageNames  = new ArrayList<>();

    /**
     * The location of the source that needs to be built.
     * For docker build, it can be an GitHub URL or local directory path.
     */
    private String sourceLocation;

    /**
     * The URL of the source that needs to be built.
     * For docker build, it can be an GitHub URL or Azure Blob URL.
     */
    private String sourceUrl;

    /**
     * ============ Optional ===============
     */

    /**
     * The collection of build arguments to be used.
     */
    private List<String> buildArguments;

    /**
     * The value of this property indicates whether the image cache is enabled
     * or not.
     */
    private boolean noCache;

    /**
     * Build timeout in seconds.
     */
    private int timeout;

    /**
     * The platform properties against which the build will happen.
     */
    private String platform = Constants.LINUX;

    /**
     * The Docker file path relative to the source location.
     */
    private String dockerFilePath;

    /**
     * =============== Getter and Setter ================
     */

    /**
     * Get the imageNames value.
     *
     * @return the imageNames value
     */
    public List<String> imageNames() {
        return this.imageNames;
    }

    /**
     * Set the imageNames value.
     *
     * @param pImageNames the imageNames value to set
     * @return the QuickBuildRequest object itself.
     */
    public QuickBuildRequest withImageNames(List<Image> pImageNames) {
        this.imageNames = new ArrayList<>();
        for (Image image : pImageNames) {
            imageNames.add(image.toString());
        }
        return this;
    }

    /**
     * Get the sourceLocation value.
     *
     * @return the sourceLocation value
     */
    public String sourceLocation() {
        return this.sourceLocation;
    }

    /**
     * Set the sourceLocation value.
     *
     * @param pSourceLocation the sourceLocation value to set
     * @return the QuickBuildRequest object itself.
     */
    public QuickBuildRequest withSourceLocation(String pSourceLocation) {
        this.sourceLocation = pSourceLocation;
        return this;
    }

    /**
     * Get the sourceUrl value.
     *
     * @return the sourceUrl value
     */
    public String sourceUrl() {
        return this.sourceUrl;
    }

    /**
     * Set the sourceLocation value.
     *
     * @param pSourceUrl the sourceUrl value to set
     * @return the QuickBuildRequest object itself.
     */
    public QuickBuildRequest withSourceUrl(String pSourceUrl) {
        this.sourceUrl = pSourceUrl;
        return this;
    }

    /**
     * Get the buildArguments value.
     *
     * @return the buildArguments value
     */
    public List<String> buildArguments() {
        return this.buildArguments;
    }

    /**
     * Set the buildArguments value.
     *
     * @param pBuildArguments the buildArguments value to set
     * @return the QuickBuildRequest object itself.
     */
    public QuickBuildRequest withBuildArguments(List<String> pBuildArguments) {
        this.buildArguments = pBuildArguments;
        return this;
    }

    /**
     * Get the noCache value.
     *
     * @return the noCache value
     */
    public boolean noCache() {
        return this.noCache;
    }

    /**
     * Set the noCache value.
     *
     * @param pNoCache the noCache value to set
     * @return the QuickBuildRequest object itself.
     */
    public QuickBuildRequest withNoCache(boolean pNoCache) {
        this.noCache = pNoCache;
        return this;
    }

    /**
     * Get the timeout value.
     *
     * @return the timeout value
     */
    public Integer timeout() {
        return this.timeout;
    }

    /**
     * Set the timeout value.
     *
     * @param pTimeout the timeout value to set
     * @return the QuickBuildRequest object itself.
     */
    public QuickBuildRequest withTimeout(Integer pTimeout) {
        this.timeout = pTimeout;
        return this;
    }

    /**
     * Get the platform value.
     *
     * @return the platform value
     */
    public String platform() {
        return this.platform;
    }

    /**
     * Set the platform value.
     *
     * @param pPlatform the platform value to set
     * @return the QuickBuildRequest object itself.
     */
    public QuickBuildRequest withPlatform(String pPlatform) {
        this.platform = pPlatform;
        return this;
    }

    /**
     * Get the dockerFilePath value.
     *
     * @return the dockerFilePath value
     */
    public String dockerFilePath() {
        return this.dockerFilePath;
    }

    /**
     * Set the dockerFilePath value.
     *
     * @param pDockerFilePath the dockerFilePath value to set
     * @return the QuickBuildRequest object itself.
     */
    public QuickBuildRequest withDockerFilePath(String pDockerFilePath) {
        this.dockerFilePath = pDockerFilePath;
        return this;
    }
}
