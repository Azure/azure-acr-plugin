/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import com.microsoft.azure.management.containerregistry.QueueBuildRequest;
import com.microsoft.azure.management.containerregistry.QuickBuildRequest;
import com.microsoft.jenkins.acr.commands.GetBuildLogCommand;
import com.microsoft.jenkins.acr.commands.QueueBuildCommand;
import com.microsoft.jenkins.acr.commands.ResolveSCM;
import com.microsoft.jenkins.azurecommons.command.BaseCommandContext;
import com.microsoft.jenkins.azurecommons.command.CommandService;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

public class ACRQuickBuildContext extends BaseCommandContext
        implements QueueBuildCommand.IQueueBuildData,
        GetBuildLogCommand.IBuildLogData,
        ResolveSCM.ISCMData {

    /**
     * DATA TRANSITION DECLARATION.
     */
    private String scmUrl;
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
    protected ACRQuickBuildContext configure(Run<?, ?> aRun,
                                             FilePath aWorkspace,
                                             Launcher aLauncher,
                                             TaskListener aTaskListener) {
        super.configure(aRun,
                aWorkspace,
                aLauncher,
                aTaskListener,
                CommandService.builder()
                        .withStartCommand(ResolveSCM.class)
                        .withTransition(ResolveSCM.class, QueueBuildCommand.class)
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
    public String getResourceGroupName() {
        return null;
    }

    @Override
    public String getACRName() {
        return null;
    }

    @Override
    public QueueBuildRequest getBuildRequest() {
        return new QuickBuildRequest()
                .withSourceLocation(this.scmUrl);
    }

    @Override
    public QueueBuildCommand.IQueueBuildData withBuildId(String id) {
        this.buildId = id;
        return this;
    }

    /**
     * {@link ResolveSCM.ISCMData}.
     */

    @Override
    public String getSourceLocation() {
        return null;
    }

    @Override
    public ACRQuickBuildContext withSCMUrl(String url) {
        this.scmUrl = url;
        return this;
    }

    /**
     * {@link GetBuildLogCommand.IBuildLogData}.
     */

    @Override
    public String getBuildId() {
        return this.buildId;
    }
}
