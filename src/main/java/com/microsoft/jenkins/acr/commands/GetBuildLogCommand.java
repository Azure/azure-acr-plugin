/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureStorageAppendBlob;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;

public class GetBuildLogCommand implements ICommand<GetBuildLogCommand.IBuildLogData> {

    /**
     * Get Azure Storage blob which saves build log from ACR.
     * Stream out the blob content.
     * @param data context
     */
    @Override
    public void execute(IBuildLogData data) {
        try {
            String blobLink = AzureContainerRegistry.getInstance()
                    .getLog(data.getResourceGroupName(),
                            data.getACRName(),
                            data.getBuildId());
            data.logStatus(Messages.log_getLogLink(blobLink));
            AzureStorageAppendBlob blob = new AzureStorageAppendBlob(blobLink);

            String line = blob.readLine();
            while (line != null) {
                data.logStatus(line);
                line = blob.readLine();
            }
            data.setCommandState(blob.isSuccess() ? CommandState.Success : CommandState.HasError);
        } catch (Exception e) {
            e.printStackTrace();
            data.logError(Messages.log_getLogError(e.getMessage()));
            data.setCommandState(CommandState.HasError);
        }
    }

    public interface IBuildLogData extends IBaseCommandData {
        String getBuildId();

        String getResourceGroupName();

        String getACRName();
    }
}
