/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.management.containerregistry.Build;
import com.microsoft.azure.management.containerregistry.BuildGetLogResult;
import com.microsoft.azure.management.containerregistry.QueueBuildRequest;
import com.microsoft.azure.management.containerregistry.Registries;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;
import rx.Observable;

import java.util.List;

public final class AzureContainerRegistry extends AzureService {

    private static AzureContainerRegistry instance;
    private ContainerRegistryManager acrManager;

    private AzureContainerRegistry() {
    }

    public Build queueBuildRequest(String resourceGroupName,
                                   String acrName,
                                   QueueBuildRequest request) {

        return this.getClient().containerRegistries()
                .queueBuildAsync(resourceGroupName, acrName, request)
                .toBlocking()
                .first();
    }

    public List<Registries> getResources(String resourceGroupName) {
        return null;
    }

    public Observable<BuildGetLogResult> getLog(String resourceGroupName, String acrName, String buildId) {
        return null;
    }

    public static AzureContainerRegistry getInstance() {
        instance = instance == null ? new AzureContainerRegistry() : instance;
        return instance;
    }
}
