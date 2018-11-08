/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import lombok.Getter;
import lombok.Setter;

public class AzureContainerRegistryTest extends BaseAzureHelper {
    public Tag listRepositoryTag(String repository) {
//        RegistryCredentials credential = this.getAzure()
//                .containerRegistries()
//                .getByResourceGroup(getResourceGroup(), getRegistry())
//                .getCredentials();
//
//
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .header("Authorization", "")
        return new Tag();
    }

    class Tag {
        @Setter
        @Getter
        private String name;
        @Setter
        @Getter
        private String[] tags;
        @Setter
        @Getter
        private TagException[] errors;
    }

    class TagException {
        @Getter
        @Setter
        private String code;
        @Setter
        @Getter
        private String message;
        @Getter
        @Setter
        private Detail detail;
    }

    class Detail {
        @Getter
        @Setter
        private String name;
    }
}
