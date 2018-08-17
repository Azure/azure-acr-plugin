/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import com.microsoft.jenkins.acr.commands.GetBuildLogCommand;
import com.microsoft.jenkins.acr.commands.QueueBuildCommand;
import com.microsoft.jenkins.acr.commands.ResolveSCMCommand;
import com.microsoft.jenkins.acr.common.QuickBuildRequest;
import com.microsoft.jenkins.acr.common.scm.SCMRequest;
import com.microsoft.jenkins.azurecommons.command.BaseCommandContext;
import com.microsoft.jenkins.azurecommons.command.CommandService;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

/**
 * Context to queue a quick build to ACR.
 * It configure all needed steps{@link ICommand} to perform the goal.
 */
public class QuickBuildContext extends BaseCommandContext
        implements QueueBuildCommand.IQueueBuildData,
        GetBuildLogCommand.IBuildLogData,
        ResolveSCMCommand.ISCMData {

    /**
     * DATA TRANSITION DECLARATION.
     */
    @Getter
    private QuickBuildRequest buildRequest;
    @Getter
    private String resourceGroupName;
    @Getter
    private String registryName;
    @Getter
    @Setter
    private String buildId;

    /**
     * Configure steps the plugin should execute.
     *
     * @param aRun a build this is running as a part of
     * @param aWorkspace a workspace to use for any file operations
     * @param aLauncher a way to start processes
     * @param aTaskListener a place to send output
     * @return this
     */
    protected QuickBuildContext configure(Run<?, ?> aRun,
                                          FilePath aWorkspace,
                                          Launcher aLauncher,
                                          TaskListener aTaskListener) {
        super.configure(aRun,
                aWorkspace,
                aLauncher,
                aTaskListener,
                CommandService.builder()
                        .withStartCommand(ResolveSCMCommand.class)
                        .withTransition(ResolveSCMCommand.class, QueueBuildCommand.class)
                        .withTransition(QueueBuildCommand.class, GetBuildLogCommand.class)
                        .build());
        return this;
    }

    @Override
    public StepExecution startImpl(StepContext stepContext) throws Exception {
        return null;
    }

    @Override
    public IBaseCommandData getDataForCommand(ICommand iCommand) {
        return this;
    }


    /**
     * =========== COMMANDDATA IMPLEMENTATION =============
     */

    /**
     * {@link QueueBuildCommand.IQueueBuildData}.
     */
    @Override
    public SCMRequest getSCMRequest() {
        return this.buildRequest;
    }

    /**
     * {@link ResolveSCMCommand.ISCMData}.
     */

    @Override
    public QuickBuildContext withSCMUrl(String url) {
        this.buildRequest.setSourceUrl(url);
        return this;
    }

    /**
     * {@link GetBuildLogCommand.IBuildLogData}.
     */

    public QuickBuildContext withResourceGroupName(String pResourceGroupName) {
        this.resourceGroupName = pResourceGroupName;
        return this;
    }

    public QuickBuildContext withBuildRequest(QuickBuildRequest pBuildRequest) {
        this.buildRequest = pBuildRequest;
        return this;
    }

    public QuickBuildContext withRegistryName(String pRegistryName) {
        this.registryName = pRegistryName;
        return this;
    }

    @Override
    public QuickBuildContext cancel() {
        this.buildRequest.cancel();
        return this;
    }

    @Override
    public boolean isCanceled() {
        return this.buildRequest.isCanceled();
    }
}
