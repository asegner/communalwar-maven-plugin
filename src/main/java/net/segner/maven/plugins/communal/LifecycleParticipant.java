package net.segner.maven.plugins.communal;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.StringUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import javax.annotation.Nullable;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "net.segner.maven.plugins.communal.LifecycleParticipant")
public class LifecycleParticipant extends AbstractMavenLifecycleParticipant implements LogEnabled {
    public static final String _THIS_GROUP_ID = "net.segner.maven.plugins";
    public static final String _THIS_ARTIFACT_ID = "communalwar-maven-plugin";
    public static final String MAVEN_EAR_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
    public static final String MAVEN_EAR_PLUGIN_ARTIFACT_ID = "maven-deploy-plugin";

    private Logger logger;

    public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
        try {
            final int totalModules = session.getProjects().size();
            logger.info("Inspecting build with total of " + totalModules + " modules...");

            for (MavenProject project : session.getProjects()) {
                final Plugin mavenPlugin = getBuildPlugin(project.getModel());
                if (mavenPlugin != null) {
                    if (mavenPlugin.getExecutions().isEmpty()) {
                        // add executions to nexus-staging-maven-plugin
                        final PluginExecution execution = new PluginExecution();
                        execution.getGoals().add("ear");
                        execution.setPhase("package");
                        execution.setConfiguration(mavenPlugin.getConfiguration());
                        mavenPlugin.getExecutions().add(execution);
                    }
                }
            }

        } catch (IllegalStateException e) {
            throw new MavenExecutionException(e.getMessage(), e);
        }

    }

    /**
     * Returns the plugin from build/plugins section of model or {@code null} if not present.
     */
    protected Plugin getBuildPlugin(final Model model) {
        if (model.getBuild() != null) {
            return getBuildPluginFromContainer(model.getBuild());
        }
        return null;
    }

    /**
     * Returns the nexus-staging-maven-plugin from pluginContainer or {@code null} if not present.
     */
    protected Plugin getBuildPluginFromContainer(final PluginContainer pluginContainer) {
        return getPluginByGAFromContainer(_THIS_GROUP_ID, _THIS_ARTIFACT_ID, pluginContainer);
    }

    @Nullable
    protected Plugin getPluginByGAFromContainer(final String groupId, final String artifactId, final PluginContainer pluginContainer) {
        Plugin result = null;
        for (Plugin plugin : pluginContainer.getPlugins()) {
            if (StringUtils.equalsIgnoreCase(groupId, plugin.getGroupId())
                    && StringUtils.equalsIgnoreCase(artifactId, plugin.getArtifactId())) {
                if (result != null) {
                    throw new IllegalStateException("The build contains multiple versions of plugin " + groupId + ":" + artifactId);
                }
                result = plugin;
            }
        }
        return result;
    }

    @Override
    public void enableLogging(final Logger logger) {
        this.logger = logger;
    }

}
