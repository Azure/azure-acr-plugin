/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.util.Constants;
import org.apache.commons.lang.StringUtils;

public class GitSCMResolver extends AbstractSCMResolver {
    private final GitSCMRequest request;

    protected GitSCMResolver(GitSCMRequest request) {
        this.request = request;
    }

    @Override
    public String getSCMUrl() {
        StringBuilder builder = new StringBuilder(request.getGitRepo());

        if (request.getGitRepo().endsWith(Constants.FILE_SPERATE)) {
            builder.deleteCharAt(builder.length() - 1);
        }

        if (!request.getGitRepo().endsWith(Constants.GIT_SUFFIX)) {
            builder.append(Constants.GIT_SUFFIX);
        }

        if (StringUtils.trimToNull(request.getGitRef()) != null) {
            builder.append(Constants.HASHTAG).append(request.getGitRef());
        }

        if (StringUtils.trimToNull(request.getGitPath()) != null) {
            builder.append(Constants.COLON).append(request.getGitPath());
        }

        String gitUrl = builder.toString();

        this.getLogger().logStatus(Messages.scm_git(gitUrl));
        return gitUrl;
    }

    /**
     * Verify whether the location is a legal git url or a local directory.
     *
     * @param location github url or local directory.
     * @return boolean
     */
    public static boolean verifyLocation(String location) {
        String source = StringUtils.trimToEmpty(location).toLowerCase();
        if (source.isEmpty()) {
            return true;
        }

        // SSH model for git is not supported
        return !source.startsWith(Constants.GIT_SSH_PREFIX);
    }
}
