package segner.maven.plugins;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.util.FileUtils;
import segner.maven.plugins.pojo.WebModule;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;


@Mojo(name = "ear", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class CommunalWarEarMojo extends AbstractMojo {

    public static final String TYPE_WEBMODULE = "war";
    public static final String TYPE_EARMODULE = "ear";

    public static final String MSGWARN_NOT_EXIST_BUILD_DIR = "Unpacked project EAR is missing in target folder. Run maven-ear-plugin first, unpacked.";
    public static final String MSGWARN_NOT_EXIST_COMMUNALBUNDLENAME = "No value for 'communalBundleName' - ignoring execution phase";
    public static final String MSGINFO_FOUND_UNPACKED_WEB_MODULE = "Found unpacked web module: ";
    public static final String MSGINFO_SKIPPING_EAR_FOLDER_ENTRY = "Skipping ear folder entry: ";
    public static final String MSGINFO_SHARED_LIBRARY_MARKED_COMMUNAL = " * communal: ";
    public static final String MSGINFO_SINGLE_LIBRARY_DEFINITION = "individual: ";
    public static final String MSGINFO_CREATING_SKINNY_WARS = "Creating skinny wars";
    public static final String[] EARLIBRARIES = StringUtils.split("aopalliance aspectjweaver aspectjrt".toLowerCase());

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

    @Parameter
    protected String classifier;

    /**
     * The maven project (effective pom)
     */
    @Parameter(defaultValue = "${project}")
    protected MavenProject mavenProject;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The Jar archiver.
     */
    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver jarArchiver;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            preExecuteCheck();

            // determine wars and their dependency locations
            Map<String, WebModule> warArtifacts = collectWarArtifacts();

            // find communal war and move dependencies into war
            moveDuplicatesToCommunal(warArtifacts);

            //repackage ear
            repackageEar();

        } catch (IllegalArgumentException ex) {
            getLog().warn(ex.getMessage());
            if (warningBreaksBuild) {
                throw new MojoFailureException(ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void repackageEar() throws IOException {
        File earFile = mavenProject.getArtifact().getFile();
        getLog().debug("Jar archiver implementation [" + jarArchiver.getClass().getName() + "]");
        jarArchiver.setDestFile(earFile);
        jarArchiver.addDirectory(new File(getEarTargetFolder()));
        jarArchiver.createArchive();

        if (classifier != null) {
            projectHelper.attachArtifact(mavenProject, TYPE_EARMODULE, classifier, earFile);
        } else {
            mavenProject.getArtifact().setFile(earFile);
        }
    }

    /**
     * move any artifacts with more than one location over to the communalWar
     */
    private void moveDuplicatesToCommunal(Map<String, WebModule> warArtifacts) {

        getLog().info(MSGINFO_CREATING_SKINNY_WARS);

        // map jars to their containing module, afterwards the map will contain:
        //     <jarName> -> <list of containing modules>
        //
        Map<String, List<WebModule>> jarMap = new HashMap<>();
        for (WebModule module : warArtifacts.values()) {
            mergeModuleJarsIntoJarMap(jarMap, module);
        }

        // migrate jars that are contained in more than one module
        final WebModule communalWar = warArtifacts.get(communalBundleName);
        jarMap.forEach((jarName, warList) -> {
            try {
                if (forceAspectJLibToEar && StringUtils.startsWithAny(jarName.toLowerCase(), EARLIBRARIES)) {
                    getLog().info("ear library: " + jarName);
                    FileUtils.copyFileToDirectory(new File(warList.get(0).getLibFolder(), jarName), new File(getEarTargetFolder(), "APP-INF/lib"));
                    warList.forEach(webmodule -> webmodule.removeLib(jarName));

                } else if (warList.size() > 1) {
                    getLog().info(MSGINFO_SHARED_LIBRARY_MARKED_COMMUNAL + jarName);
                    boolean inCommunal = warList.removeIf(webModule -> webModule.getName().equals(communalBundleName));
                    if (!inCommunal) {
                        FileUtils.copyFileToDirectory(new File(warList.get(0).getLibFolder(), jarName), communalWar.getLibFolder());
                    }
                    warList.forEach(webmodule -> webmodule.removeLib(jarName));

                } else {
                    getLog().info(MSGINFO_SINGLE_LIBRARY_DEFINITION + jarName);

                }
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
    }

    private void mergeModuleJarsIntoJarMap(Map<String, List<WebModule>> jarMap, WebModule module) {
        for (File jarFile : module.getLibFiles()) {
            String jarName = jarFile.getName();

            List<WebModule> jarMappings = jarMap.get(jarName);
            if (jarMappings == null) {
                jarMappings = new ArrayList<>();
                jarMap.put(jarName, jarMappings);
            }
            jarMappings.add(module);
        }
    }

    /**
     * filter the ear project artifacts for only unpacked web applications in the ear build (target) folder
     * <p>
     * Currently assumes web modules will end in "*.war" instead of doing a lookup of modules
     */
    private Map<String, WebModule> collectWarArtifacts() throws IOException {
        File target = new File(getEarTargetFolder());
        File[] earFiles = target.listFiles((FileFilter) new WildcardFileFilter("*." + TYPE_WEBMODULE));
        Validate.notEmpty(earFiles);

        Map<String, WebModule> result = new HashMap<>();
        for (File singleFile : Arrays.asList(earFiles)) {
            try {
                WebModule wm = new WebModule(singleFile);
                result.put(wm.getName(), wm);
                getLog().info(MSGINFO_FOUND_UNPACKED_WEB_MODULE + wm.getName());
            } catch (IllegalArgumentException ex) {
                getLog().info(MSGINFO_SKIPPING_EAR_FOLDER_ENTRY + singleFile.getName());
            }
        }
        return result;
    }

    /**
     * Validate we are ready to execute
     *
     * @throws IllegalArgumentException produced if any configuration validations fail
     */

    protected void preExecuteCheck() throws IllegalArgumentException {
        Validate.notBlank(communalBundleName, MSGWARN_NOT_EXIST_COMMUNALBUNDLENAME);
        Validate.isTrue(FileUtils.fileExists(getEarTargetFolder()), MSGWARN_NOT_EXIST_BUILD_DIR);

        //TODO: unpack is just currently required, verify here that the  ear-plugin was executed and it uses unpack
    }

    public String getEarTargetFolder() {
        Build build = mavenProject.getBuild();
        return build.getDirectory() + File.separator + build.getFinalName();
    }
}
