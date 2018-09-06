/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.NotImplementedException;

public abstract class AbstractSCMRequest implements SCMRequest {
    @Getter
    @Setter
    private String sourceUrl;

    @Override
    public String getGitRepo() {
        throw new NotImplementedException();
    }

    @Override
    public String getGitRefspec() {
        throw new NotImplementedException();
    }

    @Override
    public String getGitPath() {
        throw new NotImplementedException();
    }

    @Override
    public String getLocalDir() {
        throw new NotImplementedException();
    }

    @Override
    public String getTarball() {
        throw new NotImplementedException();
    }
}
