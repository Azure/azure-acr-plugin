/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;

public abstract class AbstractSCMCommand<T extends AbstractSCMCommand.ISCMData> implements ICommand<T> {
    protected abstract String getSourceUrl(T data) throws Exception;

    @Override
    public void execute(T data) {
        try {
            data.logStatus(Messages.source_getUrl());
            String url = getSourceUrl(data);
            data.logStatus(Messages.source_url(url));
            data.withSCMUrl(url)
                    .setCommandState(CommandState.Success);
        } catch (Exception e) {
            if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            data.logError(e.getMessage());
            data.setCommandState(CommandState.HasError);
        }
    }

    public interface ISCMData extends IBaseCommandData {
        ISCMData withSCMUrl(String url);
    }

    /**
     * Currently we support local machine and git.
     * If user wants to use Ftp, Svn or some other location, he can use other plugin to download their code to local.
     */
    public enum Type {
        GIT,
        LOCAL,
        TARBALL
    }
}
