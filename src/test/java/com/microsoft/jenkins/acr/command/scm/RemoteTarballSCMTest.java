/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.command.scm;

import com.microsoft.jenkins.acr.commands.scm.AbstractSCMCommand;
import com.microsoft.jenkins.acr.commands.scm.TarballSCMCommand;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

public class RemoteTarballSCMTest extends AbstractSCMTest<RemoteTarballSCMTest.Request> {


    @Test
    public void commonTest() throws Exception {
        String source = "https://unittest-mock";
        String url = getSCMUrl(new Request(source));
        Assert.assertEquals(source, url);
    }

    @Override
    protected AbstractSCMCommand getCommand() throws IllegalAccessException, InstantiationException {
        return TarballSCMCommand.class.newInstance();
    }

    class Request extends AbstractSCMRequest implements TarballSCMCommand.ITarballSCMData, RemoteTarballSCMRequest {
        @Getter
        private final String tarball;

        public Request(String url) {
            this.tarball = url;
        }

        @Override
        public RemoteTarballSCMRequest getTarballRequest() {
            return this;
        }
    }
}
