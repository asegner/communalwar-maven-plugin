package net.segner.maven.plugins.communal;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

@Named("net.segner.maven.plugins.communal.LifecycleParticipant")
@Singleton
@Slf4j
public class LifecycleParticipant extends AbstractMavenLifecycleParticipant {

    public static final String _THIS_GROUP_ID = "net.segner.maven.plugins";
    public static final String _THIS_ARTIFACT_ID = "skinnywar-maven-plugin";


    public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
        try {
            log.info("Ensuring skinnywar execution in build ");
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
    @Nullable
    protected Plugin getBuildPlugin(final Model model) {
        if (model.getBuild() != null) {
            return getBuildPluginFromContainer(model.getBuild());
        }
        return null;
    }

    /**
     * Returns the nexus-staging-maven-plugin from pluginContainer or {@code null} if not present.
     */
    @Nullable
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

}
