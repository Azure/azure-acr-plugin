/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import com.microsoft.jenkins.acr.Utils;
import com.microsoft.jenkins.acr.common.compression.CompressibleFileImpl;
import com.microsoft.jenkins.acr.common.compression.Compression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;

public class CompressionTest {
    private static String workspace = "compression_test";
    private static int contentLength = 10;

    /**
     * Create a workspace for each test.
     */
    @Before
    public void setUpWorkSpaces() {
        new File(workspace).mkdir();
    }

    /**
     * Delete the workspace directory after each test.
     */
    @After
    public void tearDown() {
        File dir = new File(workspace);
        Utils.deleteDir(dir);
    }

    @Test
    public void compressionWithoutIgnoreTest() throws IOException {
        File source = prepareSource(getFilename("a.txt"));
        String tarball = getFilename("a.tar.gz");
        Compression.CompressedFile file = CompressibleFileImpl.compressToFile(tarball)
                .withIgnoreList(null)
                .withFile(source.getAbsolutePath())
                .compress();
        Assert.assertTrue(new File(tarball).exists());
        Assert.assertEquals(file.fileList().length, 1);
        Assert.assertEquals(file.fileList()[0], source.getAbsolutePath());
    }

    @Test
    public void compressionWithIgnore() throws IOException {
        File source = prepareSource(getFilename("a.txt"));
        File ignore = prepareSource(getFilename("b.txt"));
        String tarball = getFilename("a.tar.gz");
        Compression.CompressedFile file = CompressibleFileImpl.compressToFile(tarball)
                .withIgnoreList(new String[]{ignore.getAbsolutePath()})
                .withFile(source.getAbsolutePath())
                .compress();
        Assert.assertTrue(new File(tarball).exists());
        Assert.assertEquals(file.fileList().length, 1);
        Assert.assertEquals(file.fileList()[0], source.getAbsolutePath());
    }

    @Test
    public void compressionWithNonExistFile() throws IOException {
        File source = new File(getFilename("a.txt"));
        String tarball = getFilename("a.tar.gz");
        Compression.CompressedFile file = CompressibleFileImpl.compressToFile(tarball)
                .withIgnoreList(null)
                .withFile(source.getAbsolutePath())
                .compress();
        Assert.assertTrue(new File(tarball).exists());
        Assert.assertEquals(file.fileList().length, 0);
    }

    @Test
    public void compressionWithDirectory() throws IOException {
        File source = prepareFiles("source", new String[]{
                "dir/",
                "dir\\a.txt",
                "dir/b.txt",
                ".git/",
                "directory/",
                "directory/a.txt",
                "directory/b.txt",
                "directory/.git"
        });

        String tarball = getFilename("a.tar.gz");
        Compression.CompressedFile file = CompressibleFileImpl.compressToFile(tarball)
                .withIgnoreList(null)
                .withDirectory(source.getAbsolutePath())
                .compress();
        Assert.assertTrue(new File(tarball).exists());
        Assert.assertEquals(file.fileList().length, 6);
    }

    @Test
    public void compressionWithDirectoryIgnoreDir() throws IOException {
        File source = prepareFiles("source", new String[]{
                "dir/",
                "dir/a.txt",
                "dir/b.txt",
                ".git/",
                "directory/",
                "directory/a.txt",
                "directory/b.txt",
                "directory/.git"
        });

        String tarball = getFilename("a.tar.gz");
        Compression.CompressedFile file = CompressibleFileImpl.compressToFile(tarball)
                .withIgnoreList(new String[]{"dir"})
                .withDirectory(source.getAbsolutePath())
                .compress();
        Assert.assertTrue(new File(tarball).exists());
        Assert.assertEquals(file.fileList().length, 3);
    }

    @Test
    public void compressionWithDirectoryIgnoreFile() throws IOException {
        File source = prepareFiles("source", new String[]{
                "dir/",
                "dir/a.txt",
                "dir/b.txt",
                ".git/",
                "directory/",
                "directory/a.txt",
                "directory/b.txt",
                "directory/.git"
        });

        String tarball = getFilename("a.tar.gz");
        Compression.CompressedFile file = CompressibleFileImpl.compressToFile(tarball)
                .withIgnoreList(new String[]{"!dir/a.txt", "dir/**"})
                .withDirectory(source.getAbsolutePath())
                .compress();
        Assert.assertTrue(new File(tarball).exists());
        Assert.assertEquals(file.fileList().length, 5);
    }

    private File prepareSource(String filename, String content) throws IOException {
        return Utils.writeFile(filename, content, false);
    }

    private String getFilename(String name) {
        return workspace + "/" + name;
    }

    private File prepareSource(String filename, int length) throws IOException {
        return prepareSource(filename, Utils.randomString(length));
    }

    private File prepareSource(String filename) throws IOException {
        return prepareSource(filename, contentLength);
    }



    private File prepareFiles(String name, String[] files) throws IOException {
        File file = new File(getFilename(name));
        file.mkdir();
        for (String s : files) {
            String filename = name + "/" + s;
            if (s.endsWith("/")) {
                new File(getFilename(filename)).mkdir();
            } else {
                prepareSource(getFilename(filename));
            }
        }
        return file;
    }
}
