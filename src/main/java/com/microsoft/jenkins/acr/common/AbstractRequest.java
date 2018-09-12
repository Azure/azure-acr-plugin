/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import com.microsoft.jenkins.acr.command.scm.SCMRequest;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractRequest implements Cancelable, SCMRequest {

    /**
     * Whether the request is canceled.
     */
    @Getter
    private boolean canceled = false;

    /**
     * The URL of the source that needs to be built.
     * For docker build, it can be an GitHub URL or Azure Blob URL.
     */
    @Getter
    @Setter
    private String sourceUrl;

    /**
     * Cancel the docker build in ACR.
     */
    public void cancel() {
        this.canceled = true;
    }
}
