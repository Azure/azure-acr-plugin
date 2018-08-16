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
import com.microsoft.jenkins.acr.common.Platform;
import com.microsoft.jenkins.acr.common.scm.GitSCMResolver;
import com.microsoft.jenkins.acr.descriptor.Image;
import com.microsoft.jenkins.acr.common.QuickBuildRequest;
import com.microsoft.jenkins.acr.service.AzureContainerRegistry;
import com.microsoft.jenkins.acr.service.AzureHelper;
import com.microsoft.jenkins.acr.service.AzureResourceGroup;
import com.microsoft.jenkins.acr.util.Util;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import com.microsoft.jenkins.acr.util.Constants;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * Build action entry of this plugin.
 * This builder together with config.jelly in resources,
 * defines the view of this build action.
 */
public class QuickBuildBuilder extends Builder implements SimpleBuildStep {

    @Getter
    private final String azureCredentialsId;
    @Getter
    private final String resourceGroupName;
    @Getter
    private final String registryName;

    @Getter
    @Setter
    @DataBoundSetter
    private List<Image> imageNames;
    @Getter
    @Setter
    @DataBoundSetter
    private String platform;
    @Getter
    @Setter
    @DataBoundSetter
    private String buildArgs;
    @Getter
    @Setter
    @DataBoundSetter
    private int timeout;
    @Getter
    @Setter
    @DataBoundSetter
    private boolean withCache;
    @Getter
    @Setter
    @DataBoundSetter
    private String dockerfile;

    @Getter
    @Setter
    @DataBoundSetter
    private String sourceType = Constants.GIT;
    @Getter
    @Setter
    @DataBoundSetter
    private String gitRepo;
    @Getter
    @Setter
    @DataBoundSetter
    private String gitBranch;
    @Getter
    @Setter
    @DataBoundSetter
    private String gitPath;
    @Getter
    @Setter
    @DataBoundSetter
    private String local;

    /**
     * This annotation tells Jenkins to call this constructor, with values from
     * the configuration form page with matching parameter names.
     *
     * @param azureCredentialsId Jenkins credential id.
     * @param resourceGroupName  ACR resource group name.
     * @param registryName       ACR name, which will run the build and the image will be default push to.
     */
    @DataBoundConstructor
    public QuickBuildBuilder(final String azureCredentialsId,
                             final String resourceGroupName,
                             final String registryName) {
        this.azureCredentialsId = azureCredentialsId;
        this.resourceGroupName = resourceGroupName;
        this.registryName = registryName;
    }

    @Override
    public final void perform(final Run<?, ?> run,
                              final FilePath workspace,
                              final Launcher launcher,
                              final TaskListener listener)
            throws InterruptedException, IOException {
        getDescriptor().checkPrerequisites(null,
                getAzureCredentialsId(),
                getResourceGroupName(),
                getRegistryName(),
                // at least one of the local and git repo should contain value.
                getLocal().concat(getGitRepo()));
        QuickBuildRequest buildRequest = QuickBuildRequest.builder()
                .sourceType(getSourceType())
                .gitRepo(getGitRepo())
                .gitBranch(getGitBranch())
                .gitPath(getGitPath())
                .localDir(getLocal())
                .imageNames(Util.toStringArray(getImageNames()))
                .platform(getPlatform())
                .buildArguments(getBuildArgs())
                .dockerFilePath(getDockerfile())
                .noCache(!isWithCache())
                .timeout(getTimeout())
                .build();

        QuickBuildContext context = new QuickBuildContext();
        context.configure(run, workspace, launcher, listener)
                .withResourceGroupName(getResourceGroupName())
                .withRegistryName(getRegistryName())
                .withBuildRequest(buildRequest)
                .executeCommands();

        if (context.getLastCommandState().isError()) {
            run.setResult(Result.FAILURE);
            // NB: The perform(AbstractBuild<?,?>, Launcher, BuildListener) method inherited from
            //     BuildStepCompatibilityLayer will delegate the call to SimpleBuildStep#perform when possible,
            //     and always return true (continue the followed build steps) regardless of the Run#getResult.
            //     We need to terminate the execution explicitly with an exception.
            //
            // see BuildStep#perform
            //     Using the return value to indicate success/failure should
            //     be considered deprecated, and implementations are encouraged
            //     to throw {@link AbortException} to indicate a failure.
            throw new AbortException(Messages.context_endWithErrorState(context.getCommandState()));
        } else {
            listener.getLogger().println(Messages.context_finished());
        }
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
         * Fill platform with {@link Platform}.
         *
         * @param owner Item
         * @return Platform list
         */
        public ListBoxModel doFillPlatformItems(@AncestorInPath final Item owner) {
            ListBoxModel list = new ListBoxModel();
            for (Platform p : Platform.values()) {
                list.add(p.toString());
            }
            return list;
        }

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
                    checkPrerequisites(owner, azureCredentialsId),
                    new Callable<Collection<String>>() {
                        @Override
                        public Collection<String> call() throws Exception {
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
         * @return ListBoxModel contains registry names
         */
        public ListBoxModel doFillRegistryNameItems(@AncestorInPath final Item owner,
                                                    @QueryParameter final String azureCredentialsId,
                                                    @QueryParameter final String resourceGroupName) {
            return constructListBox(Messages.plugin_selectAzureContainerRegistry(),
                    checkPrerequisites(owner, azureCredentialsId, resourceGroupName),
                    new Callable<Collection<String>>() {
                        @Override
                        public Collection<String> call() throws Exception {
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

        public FormValidation doCheckGitRepo(@QueryParameter String sourceType, @QueryParameter String gitRepo) {
            if (sourceType == null || !sourceType.equals(Constants.GIT)) {
                return FormValidation.ok();
            }
            return GitSCMResolver.verifyLocation(gitRepo)
                    ? FormValidation.ok()
                    : FormValidation.error(Messages.source_help());
        }

        private ListBoxModel constructListBox(String defaultValue,
                                              boolean validate,
                                              Callable<Collection<String>> action) {
            ListBoxModel list = new ListBoxModel();
            list.add(defaultValue);

            try {
                Collection<String> resources = validate ? action.call() : new ArrayList<String>();
                for (String name : resources) {
                    list.add(name);
                }
            } catch (Exception e) {
                list.add(e.getMessage(), Constants.INVALID_OPTION);
            }

            return list;
        }

        private boolean checkPrerequisites(final Item owner, String azureCredentialsId, String... params) {
            if (StringUtils.trimToNull(azureCredentialsId) == null) {
                return false;
            }

            AzureHelper.getInstance().auth(owner, azureCredentialsId);

            if (params != null || params.length == 0) {
                return true;
            }

            for (String param : params) {
                if (StringUtils.trimToNull(param) == null) {
                    return false;
                }
            }
            return true;
        }
    }
}
