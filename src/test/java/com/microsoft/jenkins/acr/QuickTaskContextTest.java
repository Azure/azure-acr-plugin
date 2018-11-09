/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import com.google.common.collect.ImmutableSet;
import com.microsoft.jenkins.acr.commands.GetBuildLogCommand;
import com.microsoft.jenkins.acr.commands.QueueTaskCommand;
import com.microsoft.jenkins.acr.commands.scm.GitSCMCommand;
import com.microsoft.jenkins.acr.commands.scm.LocalSCMCommand;
import com.microsoft.jenkins.acr.commands.scm.TarballSCMCommand;
import com.microsoft.jenkins.acr.common.Platform;
import com.microsoft.jenkins.acr.common.DockerTaskRequest;
import com.microsoft.jenkins.acr.util.Utils;
import com.microsoft.jenkins.acr.common.scm.SCMRequest;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureStorageAppendBlob;
import com.microsoft.jenkins.azurecommons.command.CommandState;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        AzureContainerRegistry.class,
        AzureStorageAppendBlob.class,
        GetBuildLogCommand.class,
        LocalSCMCommand.class
})
public class QuickTaskContextTest {
    private String dir = "context-workspace";
    private static List<String> imageList;
    private static Platform platform;

    @Mock
    private AzureContainerRegistry registry;

    @BeforeClass
    public static void prepareClass() {
        imageList = new ArrayList();
        imageList.add("azure-acr-plugin:latest");
        platform = new Platform("Linux", "ARM64", "V6");
    }

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
        DockerTaskRequest request = DockerTaskRequest.builder()
                .gitRepo("https://github.com/Azure/azure-acr-plugin")
                .gitRefspec("master")
                .imageNames(imageList)
                .platform(platform)
                .dockerFilePath("Dockerfile")
                .sourceType("git")
                .build();

        mockAzureService(request);
        QuickTaskContext context = prepareContext(request);

        SCMRequest scmRequest = context.getBuildRequest();
        Assert.assertNull(scmRequest.getLocalDir());
        Assert.assertNull(scmRequest.getTarball());
        Assert.assertEquals("https://github.com/Azure/azure-acr-plugin", scmRequest.getGitRepo());
        Assert.assertEquals("master", scmRequest.getGitRefspec());
        Assert.assertEquals("git", scmRequest.getSourceType());

        ImmutableSet set = context.getCommandService().getRegisteredCommands();
        Assert.assertTrue(set.contains(GitSCMCommand.class));
        Assert.assertTrue(set.contains(QueueTaskCommand.class));
        Assert.assertTrue(set.contains(GetBuildLogCommand.class));

        Assert.assertNull(context.getBuildRequest().getSourceUrl());

        context.executeCommands();

        Assert.assertEquals("https://github.com/Azure/azure-acr-plugin.git#master",
                context.getBuildRequest().getSourceUrl());
        Assert.assertEquals(CommandState.Success, context.getLastCommandState());
    }

    @Test
    public void cancelTest() throws Exception {

        DockerTaskRequest request = DockerTaskRequest.builder()
                .tarball("https://remote-mock-server/mock-tarball.tar.gz")
                .imageNames(imageList)
                .platform(platform)
                .dockerFilePath("Dockerfile")
                .sourceType("tarball")
                .build();

        QuickTaskContext context = prepareContext(request);

        mockAzureService(request);
        SCMRequest scmRequest = context.getBuildRequest();
        Assert.assertNull(scmRequest.getGitRepo());
        Assert.assertNull(scmRequest.getGitPath());
        Assert.assertNull(scmRequest.getGitPath());
        Assert.assertNull(scmRequest.getLocalDir());
        Assert.assertEquals("https://remote-mock-server/mock-tarball.tar.gz", scmRequest.getTarball());
        Assert.assertEquals("tarball", scmRequest.getSourceType());

        ImmutableSet set = context.getCommandService().getRegisteredCommands();
        Assert.assertTrue(set.contains(TarballSCMCommand.class));
        Assert.assertTrue(set.contains(QueueTaskCommand.class));
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
        DockerTaskRequest request = DockerTaskRequest.builder()
                .localDir("/home/user/workspace/azure-acr-plugin")
                .imageNames(imageList)
                .platform(platform)
                .dockerFilePath("Dockerfile")
                .sourceType("local")
                .build();

        QuickTaskContext context = prepareContext(request);

        mockAzureService(request);

        SCMRequest scmRequest = context.getBuildRequest();
        Assert.assertNull(scmRequest.getGitRepo());
        Assert.assertNull(scmRequest.getGitPath());
        Assert.assertNull(scmRequest.getGitPath());
        Assert.assertNull(scmRequest.getTarball());
        Assert.assertEquals("/home/user/workspace/azure-acr-plugin", scmRequest.getLocalDir());
        Assert.assertEquals("local", scmRequest.getSourceType());

        ImmutableSet set = context.getCommandService().getRegisteredCommands();
        Assert.assertTrue(set.contains(LocalSCMCommand.class));
        Assert.assertTrue(set.contains(QueueTaskCommand.class));
        Assert.assertTrue(set.contains(GetBuildLogCommand.class));

        Assert.assertNull(context.getBuildRequest().getSourceUrl());

        context.executeCommands();
        Assert.assertTrue(context.getLastCommandState().isError());
        Assert.assertEquals(CommandState.HasError, context.getLastCommandState());
    }

    private QuickTaskContext prepareContext(DockerTaskRequest request) {
        QuickTaskContext context = QuickTaskContext.builder()
                .buildRequest(request)
                .registryName("name")
                .resourceGroupName("resourcegroup")
                .build();

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
        return context;
    }

    private void mockAzureService(DockerTaskRequest request) throws Exception {
        PowerMockito.mockStatic(AzureContainerRegistry.class);
        PowerMockito.when(AzureContainerRegistry.getInstance()).thenReturn(registry);
        PowerMockito.when(registry.queueTaskRequest("resourcegroup", "name", request))
                .thenReturn("build-id-mock");
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
