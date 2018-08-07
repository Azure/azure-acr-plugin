/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.acr.util.Constants;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public abstract class AbstractSCM {

    private final String source;
    private String resourceGroup;
    private String acrName;
    private IBaseCommandData logger;

    protected AbstractSCM(String source) {
        this.source = source;
    }

    /**
     * Currently we support local machine and git.
     * If user wants to use Ftp, Svn or some other location, he can use other plugin to download their code to local.
     */
    public enum Type {
        GIT,
        LOCAL
    }

    private static AbstractSCM.Type getType(String sourceLocation) {
        if (sourceLocation.endsWith(Constants.GIT_SUFFIX)) {
            return Type.GIT;
        }
        File file = new File(sourceLocation);
        if (file.isDirectory()) {
            return Type.LOCAL;
        }

        throw new IllegalArgumentException(Messages.source_help());
    }

    /**
     * Verify whether the location is a legal git url or a local directory.
     *
     * @param location github url or local directory.
     * @return boolean
     */
    public static boolean verifyLocation(String location) {
        String source = StringUtils.trimToEmpty(location).toLowerCase();
        if (source.isEmpty()) {
            return false;
        }

        // HTTP mode for git mush end with ".git"
        if (source.startsWith(Constants.HTTP_SCHEMA) || source.startsWith(Constants.HTTPS_SCHEMA)) {
            return source.endsWith(Constants.GIT_SUFFIX);
        }

        // SSH model for git is not supported
        if (source.startsWith(Constants.GIT_SSH_PREFIX)) {
            return false;
        }

        // Here we cannot verify the file schema since the directory may not exist when configuration
        return true;
    }

    /**
     * Get the SCM instance to get the upload URL.
     *
     * @param sourceLocation github url or local directory
     * @return Instance of AbstractSCM
     */
    public static AbstractSCM getInstance(String sourceLocation) {
        Type type = getType(sourceLocation);
        switch (type) {
            case GIT:
                return new GitSCM(sourceLocation);
            case LOCAL:
                return new LocalSCM(sourceLocation);
            default:
                throw new IllegalArgumentException(Messages.source_help());
        }
    }

    protected String getSource() {
        return source;
    }

    protected String getResourceGroup() {
        return resourceGroup;
    }

    public AbstractSCM withResourceGroup(String pResourceGroup) {
        this.resourceGroup = pResourceGroup;
        return this;
    }

    protected String getAcrName() {
        return acrName;
    }

    public AbstractSCM withAcrName(String pAcrName) {
        this.acrName = pAcrName;
        return this;
    }

    public AbstractSCM withLogger(IBaseCommandData pLogger) {
        this.logger = pLogger;
        return this;
    }

    protected IBaseCommandData getLogger() {
        return logger;
    }

    public abstract String getSCMUrl() throws Exception;
}
