package com.microsoft.jenkins.acr;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.microsoft.azure.util.AzureCredentials;
import com.microsoft.jenkins.acr.service.BaseAzureHelper;
import org.jenkinsci.plugins.workflow.cps.*;
import org.jenkinsci.plugins.workflow.job.*;
import org.junit.*;
import org.jvnet.hudson.test.*;

public class QuickBuildIT extends BaseAzureHelper {
    @Rule
    public JenkinsRule j = new JenkinsRule() {
        {
            // avoid the the test timeout in 180s
            timeout = -1;
        }
    };
    @ClassRule
    public static BuildWatcher bw = new BuildWatcher();

    private final String script = "acrQuickBuild azureCredentialsId: 'azureTestId',\n" +
            "                  resourceGroupName: '" + getResourceGroup() + "',\n" +
            "                  registryName: '" + getRegistry() + "',\n" +
            "                  gitRepo: 'https://github.com/yuwzho/hello-docker',\n" +
            "                  imageNames: [[image: '"+getRegistry()+".azurecr.io/hello-docker:latest']],\n" +
            "                  sourceType: 'git'";

    @Test
    public void pipeline() throws Exception {
        // Create a new Pipeline with the given (Scripted Pipeline) definition
        AzureCredentials credentials = new AzureCredentials(CredentialsScope.GLOBAL,
                "azureTestId",
                "Azure SP credentials",
                getSubscriptionId(),
                getClientId(),
                getSecret());
        credentials.setTenant(getTenantId());
        SystemCredentialsProvider.getInstance()
                .getCredentials()
                .add(credentials);
        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("node { "+ script +" }", true));

        WorkflowRun build = j.buildAndAssertSuccess(project);

        j.assertLogContains("pushed", build);
    }
}