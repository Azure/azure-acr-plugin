/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.management.containerregistry.Architecture;
import com.microsoft.azure.management.containerregistry.OS;
import com.microsoft.azure.management.containerregistry.PlatformProperties;
import com.microsoft.azure.management.containerregistry.RegistryDockerTaskRunRequest;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.SourceUploadDefinition;
import com.microsoft.azure.management.containerregistry.Variant;
import com.microsoft.jenkins.acr.common.DockerTaskRequest;
import com.microsoft.jenkins.acr.common.UploadRequest;
import rx.Completable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AzureContainerRegistry extends AzureService {

    private static AzureContainerRegistry instance;

    private AzureContainerRegistry() {
    }

    /**
     * Send a DockerTaskRequest to ACR build.
     *
     * @param resourceGroupName resource group of Azure Container Registry.
     * @param acrName           name of Azure Container Registry.
     * @param request           request object.
     * @return String           object contain run id.
     */
    public String queueTaskRequest(String resourceGroupName,
                                    String acrName,
                                    DockerTaskRequest request) {
        PlatformProperties platformProperties = new PlatformProperties()
                .withOs(OS.fromString(request.getPlatform().getOs()))
                .withArchitecture(Architecture.fromString(request.getPlatform().getArchitecture()))
                .withVariant(Variant.fromString(request.getPlatform().getVariant()));
        boolean pushable = request.getImageNames() != null && request.getImageNames().size() > 0;
        return this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .scheduleRun()
                .withPlatform(platformProperties)
                .withDockerTaskRunRequest()
                .defineDockerTaskStep()
                .withDockerFilePath(request.getDockerFilePath())
                .withPushEnabled(pushable)
                .withImageNames(request.getImageNames())
                .withCacheEnabled(!request.isNoCache())
                .attach()
                .withSourceLocation(request.getSourceUrl())
                .withTimeout(request.getTimeout())
                .execute()
                .runId();
    }

    /**
     * List all ACR names in a resource group.
     *
     * @param resourceGroupName resource group
     * @return List of ACR name.
     */
    public Collection<String> listResourcesName(String resourceGroupName) {
        List<Registry> registryList = this.getClient()
                .containerRegistries()
                .listByResourceGroup(resourceGroupName);
        Collection<String> registryNameList = new ArrayList<>();
        for (Registry registry : registryList) {
            registryNameList.add(registry.name());
        }
        return registryNameList;
    }

    /**
     * Azure Container Registry Build will write build result to an Azure Storage Blob.
     * This function will get the blob URL with build ID.
     *
     * @param resourceGroupName resource group of ACR.
     * @param acrName           name of ACR.
     * @param runId             run ID.
     * @return Link of the Azure Storage Blob.
     */
    public String getLog(String resourceGroupName, String acrName, String runId) {
        return this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .manager()
                .registryTaskRuns()
                .getLogSasUrl(resourceGroupName, acrName, runId);
    }

    /**
     * If queue an ACR build from local SCM, need to write the code into an Azure Storage Blob.
     * This function will get the blob URL.
     *
     * @param resourceGroupName resource group of ACR.
     * @param acrName           name of ACR.
     * @return blob url and relative path.
     */
    public UploadRequest getUploadUrl(String resourceGroupName, String acrName) {
        SourceUploadDefinition definition = this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .getBuildSourceUploadUrl();
        return new UploadRequest(definition.uploadUrl(), definition.relativePath());
    }

    /**
     * Cancel a processing ACR build.
     *
     * @param resourceGroupName resource group of ACR.
     * @param acrName           name of ACR.
     * @param runId             run ID.
     * @return Completable job.
     */
    public Completable cancelBuildAsync(String resourceGroupName, String acrName, String runId) {
        return this.getClient()
                .containerRegistries()
                .manager()
                .registryTaskRuns()
                .cancelAsync(resourceGroupName, acrName, runId);
    }

    public static AzureContainerRegistry getInstance() {
        instance = instance == null ? new AzureContainerRegistry() : instance;
        return instance;
    }
}
