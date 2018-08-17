/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.scm.SCMRequest;
import com.microsoft.jenkins.acr.common.scm.AbstractSCMResolver;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;

public class ResolveSCMCommand implements ICommand<ResolveSCMCommand.ISCMData> {

    @Override
    public void execute(ISCMData data) {

        try {
            data.logStatus(Messages.source_getUrl());
            String url = AbstractSCMResolver.getInstance(data.getSCMRequest())
                    .withResourceGroup(data.getResourceGroupName())
                    .withAcrName(data.getRegistryName())
                    .withLogger(data)
                    .getSCMUrl();
            data.logStatus(Messages.source_url(url));
            data.withSCMUrl(url)
                    .setCommandState(CommandState.Success);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            data.logError(e.getMessage());
            data.setCommandState(CommandState.HasError);
        }
    }

    public interface ISCMData extends IBaseCommandData {
        SCMRequest getSCMRequest();

        String getResourceGroupName();

        String getRegistryName();

        ISCMData withSCMUrl(String url);
    }
}
