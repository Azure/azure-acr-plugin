/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.compression;

import com.microsoft.jenkins.acr.util.Util;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Create a compressed file.
 */
public class CompressibleFileImpl extends TarArchiveOutputStream
        implements Compression.CompressedFile,
        Compression.CompressibleFile,
        Compression.CompressibleWithFile,
        Compression.CompressibleWithIgnore {
    private List<String> ignore;
    private final List<String> fileList;
    private int workspaceLength = 0;

    protected CompressibleFileImpl(String filename) throws IOException {
        super(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filename))));
        this.fileList = new ArrayList<>();
    }

    @Override
    public Compression.CompressedFile compress() throws IOException {
        this.close();
        return this;
    }

    @Override
    public String[] fileList() {
        return fileList.toArray(new String[fileList.size()]);
    }

    @Override
    public Compression.CompressibleFile withFile(String filename) throws IOException {
        addFile(new File(filename));
        return this;
    }

    @Override
    public Compression.CompressibleFile withDirectory(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            return this;
        }

        workspaceLength = dir.getAbsolutePath().length() + 1;

        for (File child : dir.listFiles()) {
            addFile(child);
        }
        return this;
    }

    /**
     * Add a file or directory into the compress list and record it.
     * If the file is in the ignore list, skip it.
     * @param file File need to be added
     * @throws IOException
     */
    private void addFile(File file) throws IOException {
        if (!file.exists() || isIgnoreFile(file.getAbsolutePath())) {
            // return directly if the file isn't exist or ignored.
            return;
        }
        this.fileList.add(file.getAbsolutePath());
        this.putArchiveEntry(new TarArchiveEntry(file, file.getAbsolutePath().substring(workspaceLength)));
        if (file.isFile()) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(bis, this);
            this.closeArchiveEntry();
            bis.close();
        } else if (file.isDirectory()) {
            this.closeArchiveEntry();
            for (File child : file.listFiles()) {
                addFile(child);
            }
        }
    }

    /**
     * Check whether the file is ignored.
     * 1. Start with a specific pattern?
     * 2. End with a specific extension?
     * @param absolutePath
     * @return
     */
    private boolean isIgnoreFile(String absolutePath) {
        absolutePath = Util.normalizeFilename(absolutePath);

        return false;
    }

    @Override
    public Compression.CompressibleWithFile withIgnoreList(List<String> ignoreList) {
        this.ignore = ignoreList;
        return this;
    }

    public static Compression.CompressibleWithIgnore compressToFile(String filename) throws IOException {
        File dest = new File(filename);
        dest.deleteOnExit();
        dest.createNewFile();
        return new CompressibleFileImpl(filename);
    }
}
