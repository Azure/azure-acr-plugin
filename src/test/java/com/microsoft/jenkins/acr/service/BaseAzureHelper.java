/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.jenkins.acr.util.Utils;
import lombok.Getter;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.naming.AuthenticationException;

public abstract class BaseAzureHelper {
    @Getter
    private static Azure azure;
    @Getter
    private static String resourceGroup;
    @Getter
    private static String registry;
    @Getter
    private static String clientId;
    @Getter
    private static String tenantId;
    @Getter
    private static String subscriptionId;
    @Getter
    private static String secret;

    @BeforeClass
    public static void setup() throws AuthenticationException {
        clientId = Utils.loadFromEnv("ACR_TEST_CLIENT_ID");
        tenantId = Utils.loadFromEnv("ACR_TEST_TENANT_ID");
        subscriptionId = Utils.loadFromEnv("ACR_TEST_SUBSCRIPTION_ID");
        secret = Utils.loadFromEnv("ACR_TEST_SECRET");
        resourceGroup = Utils.loadFromEnv("ACR_TEST_RESOURCE_GROUP");
        registry = Utils.loadFromEnv("ACR_TEST_REGISTRY");
        String location = Utils.loadFromEnv("ACR_TEST_LOCATION", "eastus");
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                getClientId(),
                getTenantId(),
                getSecret(),
                AzureEnvironment.AZURE);
        azure = Azure.authenticate(credentials).withSubscription(getSubscriptionId());
        azure.resourceGroups().define(getResourceGroup())
                .withRegion(location)
                .create();

        azure.containerRegistries()
                .define(getRegistry())
                .withRegion(location)
                .withExistingResourceGroup(getResourceGroup())
                .withBasicSku()
                .create();
    }

    @AfterClass
    public static void tearDown() {
        azure.containerRegistries().deleteByResourceGroup(resourceGroup, registry);
        azure.resourceGroups().deleteByName(resourceGroup);
    }
}
