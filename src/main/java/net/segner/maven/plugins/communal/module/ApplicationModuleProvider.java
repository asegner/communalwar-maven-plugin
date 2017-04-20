package net.segner.maven.plugins.communal.module;

import lombok.extern.slf4j.Slf4j;
import net.java.truevfs.access.TFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Build;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Named
@Singleton
@Slf4j
public class ApplicationModuleProvider {
    public static final String MODULE_EXTENSION_EAR = ".ear";
    public static final String MAVEN_EAR_PLUGIN = "org.apache.maven.plugins:maven-ear-plugin";

    @Inject
    @Named("project.build")
    private Build build;
    @Inject
    private Provider<WebModule> webModuleProvider;
    @Inject
    private Provider<RarModule> rarModuleProvider;
    @Inject
    private Provider<EjbModule> ejbModuleProvider;
    private Xpp3Dom earPluginConfig;

    @PostConstruct
    public void postConstruct() {
        earPluginConfig = (Xpp3Dom) build.getPluginsAsMap().get(MAVEN_EAR_PLUGIN).getConfiguration();
    }


    /**
     * Builds a ApplicationModule for the provided path
     *
     * @param path File path of the module root folder / file
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <T extends GenericApplicationModule> T get(TFile path) throws IOException, IllegalModuleException {

        // Basic sanity check
        Validate.notNull(path);
        String name = path.getName();
        Validate.isTrue(path.exists(), "File path does not exist: " + name);

        // determine module type
        GenericApplicationModule module = findByEarConfiguration(path);
        module = (module == null) ? findByDefaultPathInspection(path) : module;
        module = (module == null) ? findByModuleExtension(path) : module;

        // Unable to determine type
        try {
            return checkModulePermissions(module);
        } catch (IllegalArgumentException e) {
            throw new IllegalModuleException("Module specified is not a known and/or valid application module: " + name);
        }
    }

    private <T extends GenericApplicationModule> T checkModulePermissions(GenericApplicationModule module) {
        Validate.notNull(module, "ApplicationModuleProvider passed a null module");
        Validate.isTrue(module.canRead(), "Unable to read module");
        Validate.isTrue(module.canWrite(), "Unable to write module");
        return (T) module;
    }

    @Nonnull
    public <T extends GenericApplicationModule> T get(TFile path, GenericApplicationModule container) throws IOException, IllegalModuleException {
        T module = get(path);
        module.setUnpacked(new TFile(container.getUnpackFolder(), container.getModuleRoot().toPath().relativize(module.getModuleRoot().toPath()).toString()));
        return module;
    }

    @Nullable
    private GenericApplicationModule findByModuleExtension(@Nonnull TFile path) {
        if (StringUtils.equalsIgnoreCase(RarModule.EXTENSION, FilenameUtils.getExtension(path.getPath()))) {
            RarModule rar = rarModuleProvider.get();
            rar.init(path);
            log.debug("Rar module detected: {}", path.getName());
            return rar;
        } else if (StringUtils.equalsIgnoreCase(EjbModule.EXTENSION, FilenameUtils.getExtension(path.getPath()))) {
            EjbModule ejb = ejbModuleProvider.get();
            ejb.init(path);
            log.debug("Ejb module detected: {}", path.getName());
            return ejb;
        }
        return null;
    }

    @Nullable
    private GenericApplicationModule findByEarConfiguration(@Nonnull TFile path) {
        // TODO determine module type by inspecting ear configuration
        return null;
    }

    /**
     * returns a module based on inspecting expected default paths that would exist in known modules
     */
    @Nullable
    private GenericApplicationModule findByDefaultPathInspection(@Nonnull TFile path) {
        // check if its a WAR by looking for WEB-INF
        TFile webmd = new TFile(path, WebModule.DEFAULT_WEBMODULE_METADATAPATH);
        if (webmd.exists()) {
            WebModule wm = webModuleProvider.get();
            wm.init(path);
            log.debug("Web module detected: {}", path.getName());
            return wm;
        }
        return null;
    }

    public EarModule getEar() {
        String expectedEarPath = build.getDirectory() + File.separator + build.getFinalName() + MODULE_EXTENSION_EAR;
        log.debug("EAR file expected at: {}", expectedEarPath);
        EarModule module = new EarModule(this, expectedEarPath);

        // find shared library path, otherwise default is used
        Xpp3Dom child = earPluginConfig.getChild("defaultLibBundleDir");
        if (child != null && StringUtils.isNotBlank(child.getValue())) {
            String libraryFolder = child.getValue();
            log.debug("Using ear library folder from maven ear plugin definition: {}", libraryFolder);
            module.setLibraryPath(libraryFolder);
        }
        return module;
    }
}
