/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands;

import com.microsoft.jenkins.acr.exception.UploadException;
import com.microsoft.jenkins.acr.scm.AbstractSCM;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;

public class ResolveSCM implements ICommand<ResolveSCM.ISCMData> {

    @Override
    public void execute(ISCMData data) {
        try {
            data.withSCMUrl(AbstractSCM.getInstance(data.getSourceLocation()).getSCMUrl())
                    .setCommandState(CommandState.Success);
        } catch (UploadException e) {
            data.setCommandState(CommandState.HasError);
            data.logError(e.getMessage());
        }
    }

    public interface ISCMData extends IBaseCommandData {
        String getSourceLocation();

        ISCMData withSCMUrl(String url);
    }
}
