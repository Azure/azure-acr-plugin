/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands;

import com.microsoft.azure.management.containerregistry.Build;
import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.QuickBuildRequest;
import com.microsoft.jenkins.acr.ACRQuickBuildPlugin;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.acr.util.Util;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;
import com.microsoft.jenkins.azurecommons.telemetry.AppInsightsUtils;

public class QueueBuildCommand implements ICommand<QueueBuildCommand.IQueueBuildData> {

    @Override
    public void execute(final IQueueBuildData context) {
        try {
            context.logStatus(Messages.build_queueABuild(
                    context.getResourceGroupName(),
                    context.getACRName(),
                    Util.toJson(context.getBuildRequest())));

            Build build = AzureContainerRegistry.
                    getInstance().
                    queueBuildRequest(context.getResourceGroupName(),
                            context.getACRName(),
                            context.getBuildRequest());

            context.logStatus(Messages.build_finishQueueABuild(build.buildId()));
            context.withBuildId(build.buildId())
                    .setCommandState(CommandState.Success);

            ACRQuickBuildPlugin.sendEvent(Constants.AI, Constants.AI_QUEUE,
                    "Run", AppInsightsUtils.hash(context.getJobContext().getRun().getUrl()),
                    "ResourceGroup", AppInsightsUtils.hash(context.getResourceGroupName()),
                    "Registry", AppInsightsUtils.hash(context.getACRName()));
        } catch (Exception e) {
            e.printStackTrace();
            context.logError(Messages.build_failQueueBuild(e.getMessage()));
            context.setCommandState(CommandState.HasError);
            ACRQuickBuildPlugin.sendEvent(Constants.AI, Constants.AI_QUEUE,
                    "Message", e.getMessage(),
                    "Run", AppInsightsUtils.hash(context.getJobContext().getRun().getUrl()),
                    "ResourceGroup", AppInsightsUtils.hash(context.getResourceGroupName()),
                    "Registry", AppInsightsUtils.hash(context.getACRName()));
        }
    }

    public interface IQueueBuildData extends IBaseCommandData {
        String getResourceGroupName();

        String getACRName();

        QuickBuildRequest getBuildRequest();

        IQueueBuildData withBuildId(String id);
    }
}
