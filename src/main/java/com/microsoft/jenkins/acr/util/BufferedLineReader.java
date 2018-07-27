/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

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
     * @param  n  The number of lines to skip
     *
     * @return    The number of lines actually skipped
     *
     * @exception  IllegalArgumentException  If <code>n</code> is negative.
     * @exception IOException  If an I/O error occurs
     */
    public long skipLines(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }

        if (n == 0) {
            return 0;
        }

        long remain = n;
        String line = this.readLine();

        while (--remain > 0 && line != null) {
            line = this.readLine();
        }

        return n - remain;
    }
}
