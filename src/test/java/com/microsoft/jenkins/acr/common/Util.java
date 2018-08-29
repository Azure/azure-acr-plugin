/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

public class Util {
    public static String randomString(int length) {
        if (length <= 0) {
            return new String();
        }
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    public static File writeFile(String filename, String content, boolean append) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename, append));
        bw.append(content);
        bw.close();
        return new File(filename);
    }
}
