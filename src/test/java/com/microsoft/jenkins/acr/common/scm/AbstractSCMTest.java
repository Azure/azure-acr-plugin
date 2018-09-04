/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import org.junit.BeforeClass;
import org.powermock.api.mockito.PowerMockito;

public abstract class AbstractSCMTest {
    protected static IBaseCommandData data;

    @BeforeClass
    public static void setup() {
        data = PowerMockito.mock(IBaseCommandData.class);
        PowerMockito.doNothing().when(data).logStatus("");
    }

    abstract protected String getSCMUrl(AbstractSCMRequest request) throws Exception;
}
