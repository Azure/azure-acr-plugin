/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.management.containerregistry.Build;
import com.microsoft.jenkins.acr.QuickBuildRequest;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AzureContainerRegistry extends AzureService {

    private static AzureContainerRegistry instance;
    private ContainerRegistryManager acrManager;

    private AzureContainerRegistry() {
    }

    public Build queueBuildRequest(String resourceGroupName,
                                   String acrName,
                                   QuickBuildRequest request) {
          return null;
//        return this.getClient()
//                .containerRegistries()
//                .getByResourceGroup(resourceGroupName, acrName)
//                .queuedBuilds()
//                .queueQuickBuild()
//                .withOSType(request.platform().osType())
//                .withSourceLocation(request.sourceLocation())
//                .withDockerFilePath(request.dockerFilePath())
//                .withImageNames(request.imageNames().toArray(new String[request.imageNames().size()]))
//                .withBuildTimeoutInSeconds(request.timeout())
//                .create();
    }

    public Collection<String> listResourcesName(String resourceGroupName) {
        List<Registry> registryList = this.getClient().containerRegistries().listByResourceGroup(resourceGroupName);
        Collection<String> registryNameList = new ArrayList<>();
        for (Registry registry : registryList) {
            registryNameList.add(registry.name());
        }
        return registryNameList;
    }

    public void getLog(String resourceGroupName, String acrName, String buildId) {
        this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .queuedBuilds()
                .get(buildId)
                .getLogLink();
    }

    public static AzureContainerRegistry getInstance() {
        instance = instance == null ? new AzureContainerRegistry() : instance;
        return instance;
    }
}
