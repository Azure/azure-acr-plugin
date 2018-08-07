/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.compression;

import java.io.IOException;
import java.util.List;

public interface Compression {


    interface CompressedFile {
        /**
         * Get the file list the tar.gz contains.
         *
         * @return File list.
         */
        String[] fileList();
    }

    interface CompressibleWithIgnore {
        /**
         * Set the compress ignore list.
         * The file meets ignore rule will not be added to the archive file.
         * @param ignoreList List contains strings like .gitignore.
         * @return Compressible object.
         */
        CompressibleWithFile withIgnoreList(List<String> ignoreList);
    }

    interface CompressibleWithFile {
        /**
         * Set the file or directory path to the compressible object.
         * If the filename is a directory, this directory <b>WILL</b> be compressed into tar.gz zip.
         * @param filename Directory or file to be compressed.
         * @return The Object can be compressed.
         * @throws IOException
         */
        CompressibleFile withFile(String filename) throws IOException;

        /**
         * Set the directory path to the compressible object.
         * This directory <b>WILL NOT</b> be compressed into tar.gz zip.
         * ALL CHILDREN will be compressed.
         * @param directory Directory name.
         * @return The Object can be compressed.
         * @throws IOException
         */
        CompressibleFile withDirectory(String directory) throws IOException;
    }

    interface CompressibleFile {
        /**
         * Compress the file into tar.gz.
         * @return Compressed file
         * @throws IOException
         */
        CompressedFile compress() throws IOException;
    }
}
