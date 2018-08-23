/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.descriptor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import lombok.Getter;
import org.kohsuke.stapler.DataBoundConstructor;

public class BuildArgument extends AbstractDescribableImpl<BuildArgument> {
    @Getter
    private final String key;
    @Getter
    private final transient String value;
    @Getter
    private final boolean secrecy;

    @DataBoundConstructor
    public BuildArgument(String key, String value, boolean secrecy) {
        this.key = key;
        this.value = value;
        this.secrecy = secrecy;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<BuildArgument> {
    }
}
