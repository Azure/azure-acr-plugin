/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

public class RemoteTarballSCMTest extends AbstractSCMTest {
    @Override
    String getSCMUrl(AbstractSCMRequest request) throws Exception {
        return AbstractSCMResolver.getInstance(request).withLogger(data).getSCMUrl();
    }

    @Test
    public void commonTest() throws Exception {
        String source = "https://unittest-mock";
        String url = getSCMUrl(new Request(source));
        Assert.assertEquals(source, url);
    }

    class Request extends AbstractSCMRequest {
        @Getter
        private final String tarball;

        public Request(String url) {
            this.tarball = url;
        }

        @Override
        public String getSourceType() {
            return "tarball";
        }
    }
}
