/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import lombok.Getter;
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
        this.os = os;
        this.architecture = architecture;
        this.variant = variant;
    }

    @Override
    public String toString() {
        return getOs() + ":" + getArchitecture() + ":" + getVariant();
    }
}
