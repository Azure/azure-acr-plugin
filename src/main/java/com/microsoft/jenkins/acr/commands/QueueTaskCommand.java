/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands;

import com.microsoft.jenkins.acr.ACRTasksPlugin;
import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.common.DockerTaskRequest;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureHelper;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.acr.util.Util;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;
import com.microsoft.jenkins.azurecommons.telemetry.AppInsightsUtils;

public class QueueTaskCommand implements ICommand<QueueTaskCommand.IQueueBuildData> {

    @Override
    public void execute(final IQueueBuildData context) {
        try {
            context.logStatus(Messages.build_queueABuild(
                    context.getResourceGroupName(),
                    context.getRegistryName(),
                    Util.toJson(context.getDockerTaskRequest())));

             String runId = AzureContainerRegistry.
                    getInstance().
                    queueTaskRequest(context.getResourceGroupName(),
                            context.getRegistryName(),
                            context.getDockerTaskRequest());

            context.logStatus(Messages.build_finishQueueABuild(runId));
            context.setBuildId(runId);
            context.setCommandState(CommandState.Success);

            ACRTasksPlugin.sendEvent(Constants.AI, Constants.AI_QUEUE,
                    "Run", AppInsightsUtils.hash(context.getJobContext().getRun().getUrl()),
                    "Subscription", AppInsightsUtils.hash(AzureHelper.getInstance().getSubscription()),
                    "ResourceGroup", AppInsightsUtils.hash(context.getResourceGroupName()),
                    "Registry", AppInsightsUtils.hash(context.getRegistryName()));
        } catch (Exception e) {
            e.printStackTrace();
            context.logError(Messages.build_failQueueBuild(e.getMessage()));
            context.setCommandState(CommandState.HasError);
            ACRTasksPlugin.sendEvent(Constants.AI, Constants.AI_QUEUE,
                    "Message", e.getMessage(),
                    "Run", AppInsightsUtils.hash(context.getJobContext().getRun().getUrl()),
                    "Subscription", AppInsightsUtils.hash(AzureHelper.getInstance().getSubscription()),
                    "ResourceGroup", AppInsightsUtils.hash(context.getResourceGroupName()),
                    "Registry", AppInsightsUtils.hash(context.getRegistryName()));
        }
    }

    public interface IQueueBuildData extends IBaseCommandData {
        String getResourceGroupName();

        String getRegistryName();

        DockerTaskRequest getDockerTaskRequest();

        void setBuildId(String id);
    }
}
