/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

public class RemoteTarballSCMResolver extends AbstractSCMResolver {
    private final RemoteTarballSCMRequest request;
    RemoteTarballSCMResolver(RemoteTarballSCMRequest request) {
        this.request = request;
    }

    @Override
    public String getSCMUrl() throws Exception {
        return request.getTarball();
    }
}
