/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands;

import com.microsoft.azure.management.containerregistry.Build;
import com.microsoft.azure.management.containerregistry.QueueBuildRequest;
import com.microsoft.jenkins.acr.ACRQuickBuildPlugin;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.azurecommons.JobContext;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;
import com.microsoft.jenkins.azurecommons.telemetry.AppInsightsUtils;

public class QueueBuildCommand implements ICommand<QueueBuildCommand.IQueueBuildData> {

    @Override
    public void execute(final IQueueBuildData context) {
        final JobContext jobContext = context.getJobContext();
        try {
            final String resourceGroupName = context.getResourceGroupName();
            final String acrName = context.getACRName();
            final QueueBuildRequest request = context.getBuildRequest();

            context.logStatus("Queue a quick build request to ACR "
                    + context.getResourceGroupName()
                    + "/"
                    + context.getACRName()
                    + ": "
                    + request.toString());
            Build build = AzureContainerRegistry.
                    getInstance().
                    queueBuildRequest(resourceGroupName, acrName, request);
            context.logStatus("Queued a build: " + build.toString());
            context.withBuildId(build.buildId())
                    .setCommandState(CommandState.Success);

            ACRQuickBuildPlugin.sendEvent(Constants.AI, Constants.AI_QUEUE,
                    "Run", AppInsightsUtils.hash(context.getJobContext().getRun().getUrl()),
                    "ResourceGroup", AppInsightsUtils.hash(context.getResourceGroupName()),
                    "Registry", AppInsightsUtils.hash(context.getACRName()));
        } catch (Exception e) {
            context.logError("Fails in queueing ACR quick build request");
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

        QueueBuildRequest getBuildRequest();

        IQueueBuildData withBuildId(String id);
    }
}
