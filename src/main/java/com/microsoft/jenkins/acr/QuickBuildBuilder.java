/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.microsoft.azure.util.AzureBaseCredentials;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureHelper;
import com.microsoft.jenkins.acr.service.AzureResourceGroup;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import com.microsoft.jenkins.acr.util.Constants;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Build action entry of this plugin.
 * This builder together with config.jelly in resources,
 * defines the view of this build action.
 */
public class QuickBuildBuilder extends Builder implements SimpleBuildStep {

    private final String azureCredentialsId;
    private final String resourceGroupName;
    private final String registryName;
    private final String source;
    private final List<String> imageNames;


    /**
     * This annotation tells Jenkins to call this constructor, with values from
     * the configuration form page with matching parameter names.
     *
     * @param azureCredentialsId Jenkins credential id.
     * @param resourceGroupName  ACR resource group name.
     * @param registryName       ACR name, which will run the build and the image will be default push to.
     * @param source             SCM source location.
     * @param imageNames         Image name with tag.
     */
    @DataBoundConstructor
    public QuickBuildBuilder(final String azureCredentialsId,
                             final String resourceGroupName,
                             final String registryName,
                             final String source,
                             final List<String> imageNames) {
        this.azureCredentialsId = azureCredentialsId;
        this.resourceGroupName = resourceGroupName;
        this.registryName = registryName;
        this.source = source;
        this.imageNames = imageNames;
    }

    @Override
    public final void perform(final Run<?, ?> run,
                              final FilePath workspace,
                              final Launcher launcher,
                              final TaskListener listener)
            throws InterruptedException, IOException {
//        QuickBuildRequest buildRequest = new QuickBuildRequest()
//                .withSourceLocation(this.getSource())
//                .withImageNames(this.getImageNames());
//        QuickBuildContext context = new QuickBuildContext();
//        context.configure(run, workspace, launcher, listener);
//        context.executeCommands();
        listener.getLogger().println("azure credentials: " + azureCredentialsId);
        listener.getLogger().println("resource group: " + resourceGroupName);
        listener.getLogger().println("acr: " + registryName);
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

    public List<String> getImageNames() {
        return imageNames;
    }

    /**
     * Jenkins defines a method {@link Builder#getDescriptor()}, which returns
     * the corresponding {@link hudson.model.Descriptor} object.
     * <p>
     * Since we know that it's actually {@link DescriptorImpl}, override the
     * method and give a better return type, so that we can access
     * {@link DescriptorImpl} methods more easily.
     * <p>
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
            return Messages.plugin_displayName();
        }

        /**
         * ============= Input fill and check ================.
         */

        /**
         * Dynamic fill the resource group name.
         *
         * @param owner              Item
         * @param azureCredentialsId azureCredentialId, if this credential changed, trigger this method
         * @return resource group list
         */
        public ListBoxModel doFillResourceGroupNameItems(@AncestorInPath final Item owner,
                                                         @QueryParameter final String azureCredentialsId) {
            return constructListBox(Messages.plugin_selectAzureResourceGroup(),
                    new Callable<Collection<String>>() {
                        @Override
                        public Collection<String> call() throws Exception {
                            if (StringUtils.trimToNull(azureCredentialsId) == null) {
                                return new ArrayList<>();
                            }
                            AzureHelper.getInstance().auth(owner, azureCredentialsId);
                            return AzureResourceGroup.getInstance().listResourceGroupNames();
                        }
                    });
        }

        /**
         * Dynamic fill the registry name.
         *
         * @param owner              Item
         * @param azureCredentialsId Trigger this method if this field changed.
         * @param resourceGroupName  List resources under this resource group. Trigger this method if changed.
         * @return
         */
        public ListBoxModel doFillRegistryNameItems(@AncestorInPath final Item owner,
                                                    @QueryParameter final String azureCredentialsId,
                                                    @QueryParameter final String resourceGroupName) {
            return constructListBox(Messages.plugin_selectAzureContainerRegistry(),
                    new Callable<Collection<String>>() {
                        @Override
                        public Collection<String> call() throws Exception {
                            if (StringUtils.trimToNull(azureCredentialsId) == null
                                    || StringUtils.trimToNull(resourceGroupName) == null) {
                                return new ArrayList<>();
                            }
                            AzureHelper.getInstance().auth(owner, azureCredentialsId);
                            return AzureContainerRegistry.getInstance().listResourcesName(resourceGroupName);
                        }
                    });
        }

        public ListBoxModel doFillAzureCredentialsIdItems(@AncestorInPath Item owner) {
            StandardListBoxModel model = new StandardListBoxModel();
            model.add(Messages.plugin_selectAzureCredential(), Constants.INVALID_OPTION);
            model.includeAs(ACL.SYSTEM, owner, AzureBaseCredentials.class);
            return model;
        }

        private ListBoxModel constructListBox(String defaultValue, Callable<Collection<String>> action) {
            ListBoxModel list = new ListBoxModel();
            if (defaultValue != null) {
                list.add(defaultValue);
            }

            try {
                for (String name : action.call()) {
                    list.add(name);
                }
            } catch (Exception e) {
                list.add(e.getMessage(), Constants.INVALID_OPTION);
            }

            if (list.size() == 0) {
                list.add(Messages.plugin_noResources(), Constants.INVALID_OPTION);
            }
            return list;
        }
    }
}
