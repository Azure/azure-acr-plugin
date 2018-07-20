/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.util.AzureBaseCredentials;
import com.microsoft.azure.util.AzureCredentialUtil;
import com.microsoft.jenkins.acr.ACRQuickBuildPlugin;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.azurecommons.core.AzureClientFactory;
import com.microsoft.jenkins.azurecommons.core.credentials.TokenCredentialData;
import hudson.model.Item;

import javax.naming.AuthenticationException;

public final class AzureHelper {
    private static AzureHelper instance;
    private static Azure client;

    private AzureHelper() {
    }

    public static AzureHelper getInstance() {
        instance = instance == null ? new AzureHelper() : instance;
        return instance;
    }

    public AzureHelper auth(TokenCredentialData token) {
        this.client = AzureClientFactory.getClient(token, new AzureClientFactory.Configurer() {
            @Override
            public Azure.Configurable configure(Azure.Configurable configurable) {
                return configurable
                        .withInterceptor(new ACRQuickBuildPlugin.AzureTelemetryInterceptor())
                        .withUserAgent(AzureClientFactory.getUserAgent(Constants.PLUGIN_NAME,
                                AzureHelper.class.getPackage().getImplementationVersion()));
            }
        });
        return this;
    }

    public AzureHelper auth(Item owner, String credentialId) {
        TokenCredentialData token = getToken(owner, credentialId);
        return auth(token);

    }

    protected Azure getClient() throws AuthenticationException {
        if (this.client == null) {
            throw new AuthenticationException("The client is not Authorized");
        }
        return this.client;
    }

    private TokenCredentialData getToken(Item owner, String credentialId) {
        AzureBaseCredentials credential = AzureCredentialUtil.getCredential(owner, credentialId);
        if (credential == null) {
            throw new IllegalStateException(
                    String.format("Can't find credential in scope %s with id: %s", owner, credentialId));
        }
        return TokenCredentialData.deserialize(credential.serializeToTokenData());
    }
}

