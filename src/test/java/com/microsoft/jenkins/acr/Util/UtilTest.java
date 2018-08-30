/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.Util;

import com.microsoft.jenkins.acr.common.QuickBuildRequest;
import com.microsoft.jenkins.acr.descriptor.BuildArgument;
import com.microsoft.jenkins.acr.descriptor.Image;
import com.microsoft.jenkins.acr.util.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UtilTest {
    @Test
    public void toJsonTest() {
        QuickBuildRequest request = QuickBuildRequest.builder()
                .platform("linux")
                .localDir("gitrepo")
                .buildArguments(new BuildArgument[]{
                        new BuildArgument("key", "secret", false)
                })
                .imageNames(new String[]{
                        "1",
                        "2"
                })
                .noCache(false)
                .build();
        Assert.assertEquals("{\"localDir\":\"gitrepo\"," +
                "\"imageNames\":[\"1\",\"2\"]," +
                "\"buildArguments\":[{\"key\":\"key\",\"secrecy\":false}]," +
                "\"noCache\":false," +
                "\"timeout\":0," +
                "\"platform\":\"linux\"," +
                "\"canceled\":false}", Util.toJson(request));
    }

    @Test
    public void toStringArrayTest() {

        String[] src = new String[]{"a", "b", "c"};
        List<Image> list = new ArrayList<>();
        for (String i : src) {
            list.add(new Image(i));
        }

        String[] result = Util.toStringArray(list);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals("a", result[0]);
        Assert.assertEquals("b", result[1]);
        Assert.assertEquals("c", result[2]);

        Assert.assertEquals(0, Util.toStringArray(null).length);
    }

    @Test
    public void normalizeFilenameTest() {
        Assert.assertEquals("C://a/b/d", Util.normalizeFilename("C:\\\\a\\b\\d"));
        Assert.assertEquals("C://a/b/d", Util.normalizeFilename("C://a/b/d"));
    }

    @Test
    public void concatPathTest() {
        Assert.assertEquals("/home/user/file", Util.concatPath("/home/user", "file"));
        Assert.assertEquals("C://a/b", Util.concatPath("/home/user", "C://a/b"));
    }

    @Test
    public void getFilenameTest() {
        Assert.assertEquals("file", Util.getFileName("/c/d/e/file"));
        Assert.assertEquals("file", Util.getFileName("file"));
        Assert.assertEquals("file", Util.getFileName("C:\\\\d\\file"));
    }
}
