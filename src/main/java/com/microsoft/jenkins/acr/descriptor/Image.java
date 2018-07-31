/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.descriptor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class Image extends AbstractDescribableImpl<Image> {
    private String image;

    @DataBoundConstructor
    public Image(String image) {
        this.image = StringUtils.trimToEmpty(image);
    }


    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return getImage();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Image> {
    }
}
