/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import com.microsoft.jenkins.acr.Messages;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class Platform {
    public enum OS {
        /**
         * The first one is default.
         */
        Linux,
        Windows
    }

    public enum ARCHITECTURE {
        AMD64,
        X86,
        ARM
    }

    public enum VARIANT {
        V6,
        V7,
        V8
    }

    @Getter
    private final String os;
    @Getter
    private final String architecture;
    @Getter
    private final String variant;

    @DataBoundConstructor
    public Platform(String os, String architecture, String variant) {
        // valid_condition       isArm        isVariantEmpty
        //                          1               0
        //                          0               1
        // ==> !(isArm ^ isVariantEmpty)
        // ==> (isArm == isVariantEmpty)
        boolean isArm = architecture.toLowerCase().equals(ARCHITECTURE.ARM.toString().toLowerCase());
        boolean isVariantEmpty = StringUtils.trimToNull(variant) == null;
        if (isArm == isVariantEmpty) {
            throw new IllegalArgumentException(Messages.platform_arm());
        }
        this.os = os;
        this.architecture = architecture;
        this.variant = variant;
    }

    @Override
    public String toString() {
        return getOs() + ":" + getArchitecture() + ":" + getVariant();
    }
}
