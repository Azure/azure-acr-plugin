/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import com.microsoft.jenkins.acr.commands.GetBuildLogCommand;
import com.microsoft.jenkins.acr.commands.QueueBuildCommand;
import com.microsoft.jenkins.acr.commands.scm.GitSCMCommand;
import com.microsoft.jenkins.acr.commands.scm.LocalSCMCommand;
import com.microsoft.jenkins.acr.commands.scm.TarballSCMCommand;
import com.microsoft.jenkins.azurecommons.command.BaseCommandContext;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractQuickBuildContext extends BaseCommandContext
        implements QueueBuildCommand.IQueueBuildData,
        GetBuildLogCommand.IBuildLogData,
        GitSCMCommand.IGitSCMData,
        TarballSCMCommand.ITarballSCMData,
        LocalSCMCommand.ILocalSCMData  {
    @Getter
    @Setter
    private String buildId;
}
