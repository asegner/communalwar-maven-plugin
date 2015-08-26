package net.segner.maven.plugins.communal;

import net.java.truevfs.access.TVFS;
import net.java.truevfs.kernel.spec.FsSyncException;
import net.segner.maven.plugins.communal.enhancer.CommunalSkinnyWarEarEnhancer;
import net.segner.maven.plugins.communal.enhancer.ModuleEnhancer;
import net.segner.maven.plugins.communal.module.EarModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mojo(name = "ear", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class CommunalWarEarMojo extends AbstractMojo {
    public static final String MAVEN_EAR_PLUGIN = "org.apache.maven.plugins:maven-ear-plugin";

    public static final String MODULE_EXTENSION_EAR = ".ear";
    public static final List<String> ASPECTJLIBRARIES = Arrays.asList(StringUtils.split("aopalliance aspectjweaver aspectjrt"));

    /**
     * The bundle name of the war to be used as the communal war. This expects an ear to have already been packaged.
     * A skinny WAR is a WAR that does not have all of its dependencies in WEB-INF/lib. Instead those dependencies are shared between the WARs.
     * Usually the dependencies are shared via the EAR. When this is not empty, the shared dependencies will be moved to the communal WAR.
     */
    @Parameter(alias = "communalWar")
    protected String communalBundleName;

    @Parameter(defaultValue = "true")
    protected Boolean warningBreaksBuild;

    @Parameter(defaultValue = "true")
    protected Boolean forceAspectJLibToEar;

    @Parameter(alias = "earLibraries")
    protected List<LibraryFilter> earLibraryList = new ArrayList<>();

    @Parameter(alias = "pinnedLibraries")
    protected List<LibraryFilter> pinnedLibraryList = new ArrayList<>();

    @Parameter(defaultValue = "${project.build}")
    protected Build mavenBuild;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // get the ear module
            EarModule earModule = fetchEarModule();
            Validate.isTrue(earModule.canRead() && earModule.canWrite(), "Missing read / write permissions to ear target folder");

            // enhance the ear with the communal skinny war pattern
            ModuleEnhancer<EarModule> earModuleEnhancer = fetchCommunalSkinnyWarEarEnhancer(communalBundleName);
            earModuleEnhancer.setTargetModule(earModule);
            earModuleEnhancer.enhance();

        } catch (IllegalArgumentException ex) {
            getLog().warn(ex.getMessage());
            if (warningBreaksBuild) {
                throw new MojoFailureException(ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            try {
                TVFS.umount();
            } catch (FsSyncException e) {
                throw new MojoExecutionException("TVFS failed to unmount cleanly", e);
            }
        }
    }

    private ModuleEnhancer<EarModule> fetchCommunalSkinnyWarEarEnhancer(String communalBundleName) {
        // merge ear library list with aspectj list for the complete list
        List<LibraryFilter> fullEarLibraryList = new ArrayList<>(earLibraryList);
        if (forceAspectJLibToEar) {
            ASPECTJLIBRARIES.forEach(earLib -> fullEarLibraryList.add(new LibraryPrefixFilter(earLib)));
        }

        // create the enhancer and return it
        CommunalSkinnyWarEarEnhancer earModuleEnhancer = new CommunalSkinnyWarEarEnhancer(communalBundleName);
        earModuleEnhancer.setPinnedLibraries(pinnedLibraryList);
        earModuleEnhancer.setEarLibraries(fullEarLibraryList);
        return earModuleEnhancer;
    }

    private EarModule fetchEarModule() {
        //TODO move instantiation to ApplicationModuleProvider.get()
        EarModule module = new EarModule(mavenBuild.getDirectory() + File.separator + mavenBuild.getFinalName() + MODULE_EXTENSION_EAR);
        Xpp3Dom earPluginConfig = (Xpp3Dom) mavenBuild.getPluginsAsMap().get(MAVEN_EAR_PLUGIN).getConfiguration();
        Xpp3Dom child = earPluginConfig.getChild("defaultLibBundleDir");
        if (child != null && StringUtils.isNotBlank(child.getValue())) {
            module.setLibraryPath(child.getValue());
        }
        return module;
    }
}
