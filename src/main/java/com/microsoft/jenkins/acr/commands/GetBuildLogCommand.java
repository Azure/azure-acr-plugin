/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands;

import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;

public class GetBuildLogCommand implements ICommand<GetBuildLogCommand.IBuildLogData> {

    @Override
    public void execute(IBuildLogData iBuildLogData) {
    }

    public interface IBuildLogData extends IBaseCommandData {
        String getBuildId();
    }
}
