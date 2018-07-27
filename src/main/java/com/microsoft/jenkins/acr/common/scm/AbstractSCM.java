/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.util.Constants;

import java.io.File;

public abstract class AbstractSCM {

    private final String source;

    protected AbstractSCM(String source) {
        this.source = source;
    }

    /**
     * Currently we support local machine and git.
     * If user wants to use Ftp, Svn or some other location, he can use other plugin to download their code to local.
     */
    public enum Type {
        GIT,
        LOCAL
    }

    public static AbstractSCM.Type getType(String sourceLocation) {
        if (sourceLocation.endsWith(Constants.GIT_SUFFIX)) {
            return Type.GIT;
        }
        File file = new File(sourceLocation);
        if (!file.isDirectory()) {
            return Type.LOCAL;
        }

        throw new IllegalArgumentException(Messages.source_help());
    }

    public static AbstractSCM getInstance(String sourceLocation) {
        Type type = getType(sourceLocation);
        switch (type) {
            case GIT:
                return new GitSCM(sourceLocation);
            case LOCAL:
                return new LocalSCM(sourceLocation);
            default:
                throw new IllegalArgumentException(Messages.source_help());
        }
    }

    protected String getSource() {
        return this.source;
    }

    public abstract String getSCMUrl();
}
