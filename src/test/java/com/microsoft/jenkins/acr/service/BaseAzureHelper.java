/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import lombok.Getter;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.naming.AuthenticationException;

public abstract class BaseAzureHelper {
    @Getter
    private static Azure azure;
    @Getter
    private static final String resourceGroup = "yuwzhorg";
    @Getter
    private static final String registry = "yuwzhoacrintegrationtest";

    @BeforeClass
    public static void setup() throws AuthenticationException {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                "XXXXX",
                "XXXXXX",
                "XXXXX",
                AzureEnvironment.AZURE);
        azure = Azure.authenticate(credentials).withSubscription("685ba005-af8d-4b04-8f16-a7bf38b2eb5a");
        azure.resourceGroups().define(resourceGroup)
                .withRegion("eastus")
                .create();

        azure.containerRegistries()
                .define(registry)
                .withRegion("eastus")
                .withExistingResourceGroup(resourceGroup)
                .withBasicSku()
                .create();
        AzureHelper.getInstance().auth(azure);
    }

    @AfterClass
    public static void teardown() {
        azure.containerRegistries().deleteByResourceGroup(resourceGroup, registry);
        azure.resourceGroups().deleteByName(resourceGroup);
    }
}
