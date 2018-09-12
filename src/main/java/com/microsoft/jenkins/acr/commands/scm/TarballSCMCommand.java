/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.commands.scm;

import com.microsoft.jenkins.acr.common.scm.RemoteTarballSCMRequest;

public class TarballSCMCommand extends AbstractSCMCommand<TarballSCMCommand.ITarballSCMData> {
    @Override
    String getSourceUrl(ITarballSCMData data) {
        return data.getTarballRequest().getTarball();
    }

    public interface ITarballSCMData extends AbstractSCMCommand.ISCMData {
        RemoteTarballSCMRequest getTarballRequest();
    }
}
