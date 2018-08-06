/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import java.util.List;

/**
 * Create a compressed file.
 */
public class CompressableFile {
    public CompressableFile() {
    }

    public String compress(String filename) {
        return filename;
    }

    public CompressableFile withDirectory(String directory) {
        return this;
    }

    public CompressableFile withFile(String file) {
        return this;
    }

    public CompressableFile withIgnoreList(List<String> ignoreList) {
        return this;
    }
}
