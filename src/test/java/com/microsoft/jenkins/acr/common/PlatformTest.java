/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import org.junit.Assert;
import org.junit.Test;

public class PlatformTest {
    @Test
    public void armWithVariant() {
        Platform platform = new Platform("linux", "arm", "v6");
        Assert.assertEquals("linux", platform.getOs());
        Assert.assertEquals("arm", platform.getArchitecture());
        Assert.assertEquals("v6", platform.getVariant());
    }

    @Test (expected = IllegalArgumentException.class)
    public void armWithNoVariant() {
        new Platform("linux", "arm", null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void notArmWithVariant() {
        new Platform("Linux", "AMD64", "v6");
    }

    @Test
    public void x86withNoVariant() {
        Platform platform = new Platform("linux", "x86", "");
        Assert.assertEquals("linux", platform.getOs());
        Assert.assertEquals("x86", platform.getArchitecture());
        Assert.assertEquals("", platform.getVariant());
    }
}
