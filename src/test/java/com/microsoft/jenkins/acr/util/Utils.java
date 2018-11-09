/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.util;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

public class Utils {
    public static String randomString(int length) {
        if (length <= 0) {
            return "";
        }
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    public static File writeFile(String filename, String content, boolean append) throws IOException {
        FileWriter fw = new FileWriter(filename, append);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.append(content);
        bw.close();
        fw.close();
        return new File(filename);
    }

    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDir(file);
            }
        }
        dir.delete();
    }

    public static String loadFromEnv(String name) {
        return loadFromEnv(name, "");
    }

    public static String loadFromEnv(String name, String defaultValue) {
        final String value = System.getenv(name);
        if (StringUtils.trimToNull(value) == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
