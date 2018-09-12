/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import com.microsoft.jenkins.acr.common.QuickBuildRequest;
import com.microsoft.jenkins.acr.descriptor.BuildArgument;
import com.microsoft.jenkins.acr.service.AzureHelper;
import com.microsoft.jenkins.acr.service.AzureResourceGroup;
import com.microsoft.jenkins.acr.service.AzureService;
import com.microsoft.jenkins.acr.service.BaseAzureHelper;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.PrintStream;

public class QuickBuildContextIT extends BaseAzureHelper {
    @Test
    public void gitSCM() {
        QuickBuildRequest request = QuickBuildRequest.builder()
                .sourceType("git")
                .gitRepo("https://github.com/yuwzho/hello-docker")
                .platform("Linux")
                .dockerFilePath("Dockerfile")
                .buildArguments(new BuildArgument[0])
                .imageNames(new String[]{"hello-docker:git"})
                .build();
        QuickBuildContext context = QuickBuildContext.builder()
                .resourceGroupName(getResourceGroup())
                .registryName(getRegistry())
                .buildRequest(request)
                .build();
        FilePath workspace = new FilePath(new File("workspace-test"));
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
        context.executeCommands();
    }
}
