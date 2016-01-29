package net.segner.maven.plugins.communal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.sisu.Description;

import java.util.ArrayList;
import java.util.List;

@Mojo(name = "ear", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Description("Enhances the EAR with an alternate layout")
public class EarLayoutEnhancerMojo extends AbstractMojo {

    /**
     * The bundle name of the war to be used as the communal war. This expects an ear to have already been packaged.
     * A skinny WAR is a WAR that does not have all of its dependencies in WEB-INF/lib. Instead those dependencies are shared between the WARs.
     * Usually the dependencies are shared via the EAR. When this is not empty, the shared dependencies will be moved to the communal WAR.
     */
    @Parameter(alias = "communalWar")
    protected String communalModuleName;
    @Parameter(defaultValue = "false")
    protected Boolean addToManifestClasspath;
    @Parameter(defaultValue = "true")
    protected Boolean forceAspectJLibToEar;
    @Parameter(defaultValue = "true")
    private Boolean generateWeblogicLtwMetadata;
    @Parameter(alias = "earLibraries")
    protected List<LibraryFilter> earLibraryList = new ArrayList<>();
    @Parameter(alias = "pinnedLibraries")
    protected List<LibraryFilter> pinnedLibraryList = new ArrayList<>();
    @Parameter(defaultValue = "true")
    protected Boolean warningBreaksBuild;
    @Parameter(defaultValue = "${project.build}")
    protected Build build;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Injector injector = Guice.createInjector(new EarLayoutEnhancerModule(communalModuleName, earLibraryList, pinnedLibraryList, forceAspectJLibToEar, generateWeblogicLtwMetadata, warningBreaksBuild, build, addToManifestClasspath));
        EarLayoutEnhancer plugin = injector.getInstance(EarLayoutEnhancer.class);
        plugin.start();
    }
}
