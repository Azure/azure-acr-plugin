/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.management.containerregistry.Build;
import com.microsoft.jenkins.acr.commands.GetBuildLogCommand;
import com.microsoft.jenkins.acr.commands.QueueBuildCommand;
import com.microsoft.jenkins.acr.commands.ResolveSCMCommand;
import com.microsoft.jenkins.acr.common.QuickBuildRequest;
import com.microsoft.jenkins.acr.Utils;
import com.microsoft.jenkins.acr.common.scm.AbstractSCMResolver;
import com.microsoft.jenkins.acr.common.scm.LocalSCMResolver;
import com.microsoft.jenkins.acr.common.scm.SCMRequest;
import com.microsoft.jenkins.acr.exception.ServiceException;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureStorageAppendBlob;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.PrintStream;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        AzureContainerRegistry.class,
        AzureStorageAppendBlob.class,
        GetBuildLogCommand.class,
        AbstractSCMResolver.class
})
public class QuickBuildContextTest {
    private String dir = "context-workspace";

    @Mock
    private AzureContainerRegistry registry;

    @Before
    public void prepare() {
        new File(dir).mkdir();
    }

    @After
    public void tearDown() {
        Utils.deleteDir(new File(dir));
    }

    @Test
    public void configureTest() throws Exception {

        QuickBuildContext context = prepareContext();


        QuickBuildRequest request = QuickBuildRequest.builder()
                .gitRepo("https://github.com/Azure/azure-acr-plugin")
                .gitRefspec("master")
                .imageNames(new String[]{"azure-acr-plugin:latest"})
                .platform("Linux")
                .dockerFilePath("Dockerfile")
                .sourceType("git")
                .build();

        mockAzureService(request);
        context.withBuildRequest(request);

        SCMRequest scmRequest = context.getSCMRequest();
        Assert.assertNull(scmRequest.getLocalDir());
        Assert.assertNull(scmRequest.getTarball());
        Assert.assertEquals("https://github.com/Azure/azure-acr-plugin", scmRequest.getGitRepo());
        Assert.assertEquals("master", scmRequest.getGitRefspec());
        Assert.assertEquals("git", scmRequest.getSourceType());

        ImmutableSet set = context.getCommandService().getRegisteredCommands();
        Assert.assertTrue(set.contains(ResolveSCMCommand.class));
        Assert.assertTrue(set.contains(QueueBuildCommand.class));
        Assert.assertTrue(set.contains(GetBuildLogCommand.class));

        Assert.assertNull(context.getBuildRequest().getSourceUrl());

        context.executeCommands();

        Assert.assertEquals("https://github.com/Azure/azure-acr-plugin.git#master",
                context.getBuildRequest().getSourceUrl());
        Assert.assertEquals(CommandState.Success, context.getLastCommandState());
    }

    @Test
    public void cancelTest() throws Exception {
        QuickBuildContext context = prepareContext();

        QuickBuildRequest request = QuickBuildRequest.builder()
                .tarball("https://remote-mock-server/mock-tarball.tar.gz")
                .imageNames(new String[]{"azure-acr-plugin:latest"})
                .platform("Linux")
                .dockerFilePath("Dockerfile")
                .sourceType("tarball")
                .build();

        context.withBuildRequest(request);
        mockAzureService(request);
        SCMRequest scmRequest = context.getSCMRequest();
        Assert.assertNull(scmRequest.getGitRepo());
        Assert.assertNull(scmRequest.getGitPath());
        Assert.assertNull(scmRequest.getGitPath());
        Assert.assertNull(scmRequest.getLocalDir());
        Assert.assertEquals("https://remote-mock-server/mock-tarball.tar.gz", scmRequest.getTarball());
        Assert.assertEquals("tarball", scmRequest.getSourceType());

        ImmutableSet set = context.getCommandService().getRegisteredCommands();
        Assert.assertTrue(set.contains(ResolveSCMCommand.class));
        Assert.assertTrue(set.contains(QueueBuildCommand.class));
        Assert.assertTrue(set.contains(GetBuildLogCommand.class));

        Assert.assertNull(context.getBuildRequest().getSourceUrl());

        Assert.assertFalse(context.getBuildRequest().isCanceled());

        context.executeCommands();
        context.cancel();
        Assert.assertEquals(CommandState.Success, context.getLastCommandState());
        Assert.assertTrue(context.getBuildRequest().isCanceled());
    }

    @Test
    public void failedTest() throws Exception {
        QuickBuildContext context = prepareContext();

        QuickBuildRequest request = QuickBuildRequest.builder()
                .localDir("/home/user/workspace/azure-acr-plugin")
                .imageNames(new String[]{"azure-acr-plugin:latest"})
                .platform("Linux")
                .dockerFilePath("Dockerfile")
                .sourceType("local")
                .build();

        context.withBuildRequest(request);
        mockAzureService(request);

        LocalSCMResolver scmResolver = PowerMockito.mock(LocalSCMResolver.class);
        PowerMockito.whenNew(LocalSCMResolver.class).withAnyArguments().thenReturn(scmResolver);
        PowerMockito.when(scmResolver.getSCMUrl()).thenThrow(new ServiceException("Azure blob", "upload", "message"));

        SCMRequest scmRequest = context.getSCMRequest();
        Assert.assertNull(scmRequest.getGitRepo());
        Assert.assertNull(scmRequest.getGitPath());
        Assert.assertNull(scmRequest.getGitPath());
        Assert.assertNull(scmRequest.getTarball());
        Assert.assertEquals("/home/user/workspace/azure-acr-plugin", scmRequest.getLocalDir());
        Assert.assertEquals("local", scmRequest.getSourceType());

        ImmutableSet set = context.getCommandService().getRegisteredCommands();
        Assert.assertTrue(set.contains(ResolveSCMCommand.class));
        Assert.assertTrue(set.contains(QueueBuildCommand.class));
        Assert.assertTrue(set.contains(GetBuildLogCommand.class));

        Assert.assertNull(context.getBuildRequest().getSourceUrl());

        context.executeCommands();
        Assert.assertTrue(context.getLastCommandState().isError());
        Assert.assertEquals(CommandState.HasError, context.getLastCommandState());
    }

    private QuickBuildContext prepareContext() {
        QuickBuildContext context = new QuickBuildContext();

        FilePath workspace = new FilePath(new File(dir));
        Run run = PowerMockito.mock(Run.class);
        Launcher launcher = PowerMockito.mock(Launcher.class);
        TaskListener listener = PowerMockito.mock(TaskListener.class);

        PrintStream logger = PowerMockito.mock(PrintStream.class);
        PowerMockito.when(listener.getLogger()).thenReturn(logger);
        PowerMockito.doNothing().when(logger).println(ArgumentMatchers.any());
        context.configure(run,
                workspace,
                launcher,
                listener);
        context.withResourceGroupName("resourcegroup")
                .withRegistryName("name");
        return context;
    }

    private void mockAzureService(QuickBuildRequest request) throws Exception {
        Build build = PowerMockito.mock(Build.class);
        PowerMockito.when(build.buildId()).thenReturn("build-id-mock");
        PowerMockito.mockStatic(AzureContainerRegistry.class);
        PowerMockito.when(AzureContainerRegistry.getInstance()).thenReturn(registry);
        PowerMockito.when(registry.queueBuildRequest("resourcegroup", "name", request))
                .thenReturn(build);
        PowerMockito.when(registry.getLog("resourcegroup", "name", "build-id-mock"))
                .thenReturn("blob-url-mock");
        PowerMockito.when(registry
                .cancelBuildAsync("resourcegroup", "name", "build-id-mock"))
                .thenReturn(null);
        AzureStorageAppendBlob blob = PowerMockito.mock(AzureStorageAppendBlob.class);
        PowerMockito.whenNew(AzureStorageAppendBlob.class).withArguments("blob-url-mock").thenReturn(blob);
        PowerMockito.when(blob.readLine()).thenReturn(null);
        PowerMockito.when(blob.isSuccess()).thenReturn(true);
    }
}
