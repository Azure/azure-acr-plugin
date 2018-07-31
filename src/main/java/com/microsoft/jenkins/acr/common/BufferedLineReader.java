/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.acr.util.Util;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.concurrent.Callable;

public class BufferedLineReader extends BufferedReader {
    public BufferedLineReader(Reader in, int sz) {
        super(in, sz);
    }

    public BufferedLineReader(Reader in) {
        super(in);
    }

    /**
     * Skips lines.
     *
     * @param n The number of lines to skip
     * @return The number of lines actually skipped
     * @throws IllegalArgumentException If <code>n</code> is negative.
     * @throws Exception              If an I/O error occurs
     */
    public long skipLines(long n) throws Exception {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }

        if (n == 0) {
            return 0;
        }

        long remain = n;
        String line = new String();
        final BufferedLineReader reader = this;

        while (remain-- > 0 && line != null) {
            Util.retry(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return reader.readLine();
                }
            }, Constants.DEFAULT_RETRY);
        }

        return n - remain;
    }
}
