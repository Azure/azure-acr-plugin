/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.management.containerregistry.Build;
import com.microsoft.azure.management.containerregistry.Build.QueuedQuickBuildDefinitionStages.WithCreate;
import com.microsoft.azure.management.containerregistry.Build.
        QueuedQuickBuildDefinitionStages.QueuedQuickBuildArgumentDefinitionStages.WithBuildArgumentAttach;
import com.microsoft.azure.management.containerregistry.OsType;
import com.microsoft.azure.management.containerregistry.SourceUploadDefinition;
import com.microsoft.jenkins.acr.common.QuickBuildRequest;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.jenkins.acr.common.UploadRequest;
import com.microsoft.jenkins.acr.descriptor.BuildArgument;
import rx.Completable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AzureContainerRegistry extends AzureService {

    private static AzureContainerRegistry instance;

    private AzureContainerRegistry() {
    }

    /**
     * Send a quickBuildRequest to ACR build.
     *
     * @param resourceGroupName resource group of Azure Container Registry.
     * @param acrName           name of Azure Container Registry.
     * @param request           request object.
     * @return Build object contain build id.
     */
    public Build queueBuildRequest(String resourceGroupName,
                                   String acrName,
                                   QuickBuildRequest request) {
        WithCreate withCreate = this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .queuedBuilds()
                .queueQuickBuild()
                .withOSType(OsType.fromString(request.getPlatform()))
                .withSourceLocation(request.getSourceUrl())
                .withDockerFilePath(request.getDockerFilePath());
        if (request.getImageNames() == null || request.getImageNames().length == 0) {
            withCreate.withImagePushDisabled();
        } else {
            withCreate.withImagePushEnabled()
                    .withImageNames(request.getImageNames());
        }

        for (BuildArgument arg : request.getBuildArguments()) {
            WithBuildArgumentAttach<WithCreate> argumentAttach = withCreate.defineBuildArgument(arg.getKey())
                    .withValue(arg.getValue());
            if (arg.isSecrecy()) {
                argumentAttach.withSecrecyEnabled();
            } else {
                argumentAttach.withSecrecyDisabled();
            }
            withCreate = argumentAttach.attach();
        }

        return withCreate.create();
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
     * @param buildId           build ID.
     * @return Link of the Azure Storage Blob.
     */
    public String getLog(String resourceGroupName, String acrName, String buildId) {
        return this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .queuedBuilds()
                .get(buildId)
                .getLogLink();
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
     * @param buildId           build ID.
     * @return Completable job.
     */
    public Completable cancelBuildAsync(String resourceGroupName, String acrName, String buildId) {
        return this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .queuedBuilds()
                .cancelAsync(buildId);
    }

    public static AzureContainerRegistry getInstance() {
        instance = instance == null ? new AzureContainerRegistry() : instance;
        return instance;
    }
}
