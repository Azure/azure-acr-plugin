/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import com.microsoft.jenkins.acr.commands.GetBuildLogCommand;
import com.microsoft.jenkins.acr.commands.QueueTaskCommand;
import com.microsoft.jenkins.acr.commands.scm.AbstractSCMCommand;
import com.microsoft.jenkins.acr.commands.scm.GitSCMCommand;
import com.microsoft.jenkins.acr.commands.scm.LocalSCMCommand;
import com.microsoft.jenkins.acr.commands.scm.TarballSCMCommand;
import com.microsoft.jenkins.acr.common.DockerTaskRequest;
import com.microsoft.jenkins.acr.common.scm.GitSCMRequest;
import com.microsoft.jenkins.acr.common.scm.LocalSCMRequest;
import com.microsoft.jenkins.acr.common.scm.RemoteTarballSCMRequest;
import com.microsoft.jenkins.azurecommons.command.CommandService;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import com.microsoft.jenkins.azurecommons.command.ICommand;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import lombok.Builder;
import lombok.Getter;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

/**
 * Context to queue a quick task to ACR.
 * It configure all needed steps{@link ICommand} to perform the goal.
 */
@Builder
public class QuickTaskContext extends AbstractQuickTaskContext {

    /**
     * DATA TRANSITION DECLARATION.
     */
    @Getter
    private DockerTaskRequest dockerTaskRequest;
    @Getter
    private String resourceGroupName;
    @Getter
    private String registryName;

    /**
     * Configure steps the plugin should execute.
     *
     * @param aRun          a build this is running as a part of
     * @param aWorkspace    a workspace to use for any file operations
     * @param aLauncher     a way to start processes
     * @param aTaskListener a place to send output
     * @return this
     */
    protected QuickTaskContext configure(Run<?, ?> aRun,
                                         FilePath aWorkspace,
                                         Launcher aLauncher,
                                         TaskListener aTaskListener) {
        Class scmCommand;


        switch (AbstractSCMCommand.Type.valueOf(dockerTaskRequest.getSourceType().toUpperCase())) {
            case GIT:
                scmCommand = GitSCMCommand.class;
                break;
            case LOCAL:
                scmCommand = LocalSCMCommand.class;
                break;
            case TARBALL:
                scmCommand = TarballSCMCommand.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown source type: " + dockerTaskRequest.getSourceType());
        }
        super.configure(aRun,
                aWorkspace,
                aLauncher,
                aTaskListener,
                CommandService.builder()
                        .withStartCommand(scmCommand)
                        .withTransition(scmCommand, QueueTaskCommand.class)
                        .withTransition(QueueTaskCommand.class, GetBuildLogCommand.class)
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
     * {@link AbstractSCMCommand.ISCMData}.
     */

    @Override
    public QuickTaskContext withSCMUrl(String url) {
        this.dockerTaskRequest.setSourceUrl(url);
        return this;
    }

    /**
     * {@link GetBuildLogCommand.IBuildLogData}.
     */

    public QuickTaskContext withResourceGroupName(String pResourceGroupName) {
        this.resourceGroupName = pResourceGroupName;
        return this;
    }

    public QuickTaskContext withDockerTaskRequest(DockerTaskRequest pDockerTaskRequest) {
        this.dockerTaskRequest = pDockerTaskRequest;
        return this;
    }

    public QuickTaskContext withRegistryName(String pRegistryName) {
        this.registryName = pRegistryName;
        return this;
    }

    @Override
    public QuickTaskContext cancel() {
        this.dockerTaskRequest.cancel();
        return this;
    }

    @Override
    public boolean isCanceled() {
        return this.dockerTaskRequest.isCanceled();
    }

    /**
     * {@link GitSCMCommand.IGitSCMData}.
     */

    @Override
    public GitSCMRequest getGitSCMRequest() {
        return this.dockerTaskRequest;
    }

    /**
     * {@link TarballSCMCommand.ITarballSCMData}.
     */

    @Override
    public RemoteTarballSCMRequest getTarballRequest() {
        return this.dockerTaskRequest;
    }

    /**
     * {@link LocalSCMCommand.ILocalSCMData}.
     */

    @Override
    public LocalSCMRequest getLocalSCMRequest() {
        return this.dockerTaskRequest;
    }
}
