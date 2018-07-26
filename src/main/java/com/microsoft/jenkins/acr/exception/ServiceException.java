/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.exception;

import com.microsoft.jenkins.acr.Messages;

public class ServiceException extends Exception {
    public ServiceException(String service, String action, String message) {
        super(Messages.service_error(service, action, message));
    }
}
