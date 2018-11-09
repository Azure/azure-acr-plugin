/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands.scm;

import com.microsoft.jenkins.azurecommons.JobContext;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import hudson.EnvVars;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractSCMRequest implements AbstractSCMCommand.ISCMData {
    @Getter
    protected String SCMUrl;

    @Getter
    @Setter
    protected CommandState commandState;

    @Override
    public AbstractSCMCommand.ISCMData withSCMUrl(String url) {
        this.SCMUrl = url;
        return this;
    }

    @Override
    public void logError(String s) {
        // do nothing
    }

    @Override
    public void logStatus(String s) {
        // do nothing
    }

    @Override
    public void logError(Exception e) {
        // do nothing
    }

    @Override
    public void logError(String s, Exception e) {
        // do nothing
    }

    @Override
    public JobContext getJobContext() {
        return null;
    }

    @Override
    public EnvVars getEnvVars() {
        return null;
    }
}