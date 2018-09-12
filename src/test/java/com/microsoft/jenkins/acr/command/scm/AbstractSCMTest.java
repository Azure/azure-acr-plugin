/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.command.scm;

import com.microsoft.jenkins.acr.commands.scm.AbstractSCMCommand;

public abstract class AbstractSCMTest<T extends AbstractSCMRequest> {

    protected String getSCMUrl(T request) throws InstantiationException, IllegalAccessException {
        getCommand().execute(request);
        return request.getSCMUrl();
    }

    abstract protected AbstractSCMCommand getCommand() throws IllegalAccessException, InstantiationException;
}
