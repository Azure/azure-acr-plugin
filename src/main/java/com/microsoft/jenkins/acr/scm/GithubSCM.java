package com.microsoft.jenkins.acr.scm;

import com.microsoft.jenkins.acr.exception.UploadException;

public class GithubSCM extends AbstractSCM {

    protected GithubSCM(String location) {
        super(location);
    }

    @Override
    public String getSCMUrl() throws UploadException {
        return this.getLocation();
    }
}
