/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.common.scm;

import com.microsoft.jenkins.acr.Messages;
import com.microsoft.jenkins.azurecommons.command.IBaseCommandData;

public abstract class AbstractSCMResolver {

    private String resourceGroup;
    private String acrName;
    private IBaseCommandData logger;

    protected AbstractSCMResolver() {
    }

    /**
     * Currently we support local machine and git.
     * If user wants to use Ftp, Svn or some other location, he can use other plugin to download their code to local.
     */
    public enum Type {
        GIT,
        LOCAL,
        TARBALL
    }

    /**
     * Get the SCM instance to get the upload URL.
     *
     * @param source github url or local directory
     * @return Instance of AbstractSCMResolver
     */
    public static AbstractSCMResolver getInstance(SCMRequest source) {
        Type type = Type.valueOf(source.getSourceType().toUpperCase());
        switch (type) {
            case GIT:
                return new GitSCMResolver(source);
            case LOCAL:
                return new LocalSCMResolver(source);
            case TARBALL:
                return new RemoteTarballSCMResolver(source);
            default:
                throw new IllegalArgumentException(Messages.source_help());
        }
    }

    protected String getResourceGroup() {
        return resourceGroup;
    }

    public AbstractSCMResolver withResourceGroup(String pResourceGroup) {
        this.resourceGroup = pResourceGroup;
        return this;
    }

    protected String getAcrName() {
        return acrName;
    }

    public AbstractSCMResolver withAcrName(String pAcrName) {
        this.acrName = pAcrName;
        return this;
    }

    public AbstractSCMResolver withLogger(IBaseCommandData pLogger) {
        this.logger = pLogger;
        return this;
    }

    protected IBaseCommandData getLogger() {
        return logger;
    }

    public abstract String getSCMUrl() throws Exception;
}
