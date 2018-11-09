/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.scm.GitSCMRequest;
import com.microsoft.jenkins.acr.util.Constants;
import org.apache.commons.lang.StringUtils;

public class GitSCMCommand extends AbstractSCMCommand<GitSCMCommand.IGitSCMData> {

    protected String getSourceUrl(IGitSCMData data) {
        GitSCMRequest request = data.getGitSCMRequest();
        StringBuilder builder = new StringBuilder(request.getGitRepo());

        if (request.getGitRepo().endsWith(Constants.FILE_SPERATE)) {
            builder.deleteCharAt(builder.length() - 1);
        }

        if (!request.getGitRepo().endsWith(Constants.GIT_SUFFIX)) {
            builder.append(Constants.GIT_SUFFIX);
        }

        if (StringUtils.trimToNull(request.getGitRefspec()) != null) {
            builder.append(Constants.HASHTAG).append(request.getGitRefspec());
        }

        if (StringUtils.trimToNull(request.getGitPath()) != null) {
            builder.append(Constants.COLON).append(request.getGitPath());
        }

        String gitUrl = builder.toString();

        data.logStatus(Messages.scm_git(gitUrl));
        return gitUrl;
    }

    public interface IGitSCMData extends AbstractSCMCommand.ISCMData {
        GitSCMRequest getGitSCMRequest();
    }
}
