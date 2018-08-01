/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

public class LocalSCM extends AbstractSCM {

    protected LocalSCM(String source) {
        super(source);
    }

    @Override
    public String getSCMUrl() {
        return null;
    }
}
