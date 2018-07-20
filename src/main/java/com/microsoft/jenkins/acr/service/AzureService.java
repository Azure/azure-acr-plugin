/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.management.Azure;

import javax.naming.AuthenticationException;

public abstract class AzureService {

    protected Azure getClient() {
        try {
            return AzureHelper.getInstance().getClient();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
