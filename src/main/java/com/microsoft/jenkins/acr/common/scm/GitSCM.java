/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;

public class GitSCM extends AbstractSCM {
    protected GitSCM(String source) {
        super(source);
    }

    @Override
    public String getSCMUrl() {
        this.getLogger().logStatus(Messages.scm_git(this.getSource()));
        return this.getSource();
    }
}
