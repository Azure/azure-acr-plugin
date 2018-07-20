/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.scm;

import com.microsoft.jenkins.acr.exception.UploadException;
import com.microsoft.jenkins.acr.util.Util;

public abstract class AbstractSCM {
    private final String location;

    /**
     * This class is to get the URL for source code.
     * Currently include GitHubSCM, localSCM.
     *
     * @param location Where is the source code [GitHubURL, directory].
     */
    protected AbstractSCM(String location) {
        this.location = location;
    }

    /**
     * Get source location of the SCM.
     *
     * @return Where is the source code.
     */
    protected String getLocation() {
        return this.location;
    }

    /**
     * Return the URL for source code.
     * It can be the GitHub URL of a GitHub repo.
     * It can be an Azure blob URL where the source code in local uploaded to.
     *
     * @return source URL.
     */
    public abstract String getSCMUrl() throws UploadException;

    /**
     * This static method returns analysis location and return a corresponding AbstractSCM.
     *
     * @param location Where is the source code [GitHubURL, directory].
     * @return An instance of AbstractSCM
     */
    public static AbstractSCM getInstance(String location) {
        if (Util.isGitHubRepo(location)) {
            return new GithubSCM(location);
        } else if (Util.isLocalDirectory(location)) {
            return new LocalSCM(location);
        } else {
            throw new UnsupportedOperationException(location
                    + " is not a valid SCM location. GitHub URL or local directory is acceptable.");
        }
    }
}
