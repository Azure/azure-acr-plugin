/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.scm;

import com.microsoft.jenkins.acr.exception.UploadException;

public class LocalSCM extends AbstractSCM {

    protected LocalSCM(String location) {
        super(location);
    }

    @Override
    public String getSCMUrl() throws UploadException {
        return null;
    }
}
