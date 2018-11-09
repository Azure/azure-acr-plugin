/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common;

import com.microsoft.jenkins.acr.util.Utils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BufferedLineReaderTest {

    private final String filename = "buffered-line-test-file";
    private BufferedLineReader reader;

    @After
    public void tearDown() throws IOException {
        if (reader != null) {
            reader.close();
        }
        new File(filename).delete();
    }

    @Test
    public void commonTest() throws IOException {
        Utils.writeFile(filename, "1. a\n\n\n3. b\n#4. c", false);
        reader = new BufferedLineReader(new FileReader(filename));
        try {
            reader.skipLines(-1);
            Assert.fail();
        }catch (Exception e) {
            Assert.assertEquals("skip value is negative", e.getMessage());
        }

        Assert.assertEquals("1. a", reader.readLine());
        reader.skipLines(0);
        Assert.assertEquals("", reader.readLine());
        reader.skipLines(1);
        Assert.assertEquals("3. b", reader.readLine());
        reader.skipLines(2);
        Assert.assertNull(reader.readLine());
        reader.skipLines(3);  // should be successfully execute
    }
}
