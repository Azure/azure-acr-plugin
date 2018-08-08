/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.compression;

import com.microsoft.jenkins.acr.util.Constants;
import org.apache.commons.lang.StringUtils;

public class IgnoreRule {
    private final boolean ignore;
    private final String pattern;

    public IgnoreRule(String rule) {
        rule = StringUtils.trimToEmpty(rule);
        boolean isIgnore = true;
        if (rule.startsWith(Constants.EXCLAMATION)) {
            isIgnore = false;
            rule = rule.substring(Constants.EXCLAMATION.length());
        }

        this.ignore = isIgnore;

        String[] tokens = rule.split(Constants.FILE_SPERATE);
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.equals("**")) {
                tokens[i] = ".*";
            } else {
                tokens[i] = token.replaceAll("\\*", "[^/]*")
                        .replaceAll("\\?", "[^/]");
            }
        }
        this.pattern = "^" + StringUtils.join(tokens, '/') + "$";
    }

    public boolean isIgnore() {
        return ignore;
    }

    public String getPattern() {
        return pattern;
    }
}
