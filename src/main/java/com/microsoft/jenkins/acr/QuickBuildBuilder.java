/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import java.io.IOException;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import com.microsoft.jenkins.acr.util.Constants;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;


public class QuickBuildBuilder extends Builder implements SimpleBuildStep {

    private final String azureCredentialsId;
    private final String resourceGroupName;
    private final String registryName;
    private final String source;
    private String scmUrl;

    /**
     * This annotation tells Jenkins to call this constructor, with values from
     * the configuration form page with matching parameter names.
     * @param azureCredentialsId Jenkins credential id.
     * @param resourceGroupName ACR resource group name.
     * @param registryName ACR name, which will run the build and the image will be default push to.
     */
    @DataBoundConstructor
    public QuickBuildBuilder(final String azureCredentialsId,
                             final String resourceGroupName,
                             final String registryName,
                             final String source) {
        this.azureCredentialsId = azureCredentialsId;
        this.resourceGroupName = resourceGroupName;
        this.registryName = registryName;
        this.source = source;
    }

    @Override
    public final void perform(final Run<?, ?> run,
                              final FilePath workspace,
                              final Launcher launcher,
                              final TaskListener listener)
            throws InterruptedException, IOException {
        ACRQuickBuildContext context = new ACRQuickBuildContext();
        context.configure(run, workspace, launcher, listener);
        context.executeCommands();
    }

    public String getAzureCredentialsId() {
        return azureCredentialsId;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public String getRegistryName() {
        return registryName;
    }

    public String getSource() {
        return source;
    }

    /**
     * Jenkins defines a method {@link Builder#getDescriptor()}, which returns
     * the corresponding {@link hudson.model.Descriptor} object.
     *
     * Since we know that it's actually {@link DescriptorImpl}, override the
     * method and give a better return type, so that we can access
     * {@link DescriptorImpl} methods more easily.
     *
     * This is not necessary, but just a coding style preference.
     *
     * @return descriptor for this builder
     */
    @Override
    public final DescriptorImpl getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return (DescriptorImpl) super.getDescriptor();
    }

    // @Extension annotation identifies this uses an extension point
    // @Symbol annotation registers a symbol with pipeline with @acrQuickBuild
    @Extension
    @Symbol("acrQuickBuild")
    public static final class DescriptorImpl
            extends BuildStepDescriptor<Builder> {

        /**
         * Constructor for this descriptor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Applicable to any kind of project.
         *
         * @param type class to be tested for applicability
         * @return true if this builder can be applied to a project of class
         * type
         */
        @Override
        public boolean isApplicable(final Class type) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Constants.DISPLAY_NAME;
        }
    }
}
