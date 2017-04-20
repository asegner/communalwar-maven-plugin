package net.segner.maven.plugins.communal.enhancer;

import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import net.java.truevfs.access.TFile;
import net.segner.maven.plugins.communal.LibraryFilter;
import net.segner.maven.plugins.communal.module.ApplicationModule;
import net.segner.maven.plugins.communal.module.EarModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@Slf4j
public abstract class SkinnyWarEarEnhancer extends AbstractEnhancer<EarModule> implements ModuleEnhancer<EarModule> {

    @Inject
    @Named("warningBreaksBuild")
    private Boolean warningBreaksBuild;

    @Inject
    @Named("addToManifestClasspath")
    private Boolean addToManifestClasspath;

    public static final String MSGDEBUG_COMMUNAL_LIBRARY = " * skinny: ";
    public static final String MSGDEBUG_SINGLE_LIBRARY = "individual: ";
    public static final String MSGDEBUG_PINNED_LIBRARY = "pinned: ";
    public static final String MSGDEBUG_EAR_LIBRARY = "ear library: ";
    public static final String MSGINFO_SUCCESS = "Finished Layout";
    public static final Attributes.Name ATTR_CLASSPATH = new Attributes.Name("Class-Path");

    private Map<String, List<ApplicationModule>> libraryMap;
    private List<LibraryFilter> pinnedLibraries;
    private List<LibraryFilter> earLibraries;


    protected String sharedModuleName;

    public void setEarLibraries(List<LibraryFilter> earLibraries) {
        this.earLibraries = earLibraries;
    }

    public void setPinnedLibraries(List<LibraryFilter> pinnedLibraries) {
        this.pinnedLibraries = pinnedLibraries;
    }

    /**
     * move any artifacts with more than one location over to the shared module
     */
    protected void makeSkinnyModules() throws IOException {
        Validate.notNull(getTargetModule(), "No target module");

        // map jars (libraries) to their containing module, afterwards the map will contain:
        //     <libraryName> -> <list of containing modules>
        //
        libraryMap = new HashMap<>();
        Map<String, ApplicationModule> earModules = getTargetModule().getModules();
        earModules.values().forEach(this::mergeModuleLibrariesIntoMap);

        // migrate jars that are contained in more than one module
        final ApplicationModule sharedModule = StringUtils.isNotBlank(sharedModuleName) ? earModules.get(sharedModuleName) : getTargetModule();
        Validate.notNull(sharedModule, "Shared module not found: " + sharedModuleName);
        libraryMap.forEach((jarName, warList) -> applyPackagingLayoutToJar(sharedModule, jarName, warList));

        // build list of jars in shared module
        // go through each module and add list to manifest class-path
        if (addToManifestClasspath) {
            Path moduleRootPath = getTargetModule().getModuleRoot().toPath();
            List<String> sharedLibList = sharedModule.getLibraryFiles()
                    .stream()
                    .map(file -> {
                        return moduleRootPath.relativize(file.toPath()).toString();
                    })
                    .collect(Collectors.toList());
            earModules.forEach((name, module) -> {
                Manifest manifest = null;
                try {
                    log.info("Updating module manifest: {}", module.getName());
                    manifest = module.getManifest();
                    List<String> classpath = parseClassPathAttribute(manifest);
                    classpath.addAll(0, sharedLibList);
                    manifest.getMainAttributes().put(ATTR_CLASSPATH, StringUtils.join(classpath, " "));
                    module.saveManifest(manifest);
                } catch (IOException e) {
                    log.warn("Failed to write manifest for {}", module.getName()); //TODO respect warningBreaksBuild flag
                }
            });
        }
    }

    private List<String> parseClassPathAttribute(Manifest manifest) {
        Attributes attrs = manifest.getMainAttributes();
        String classpath = attrs.containsKey(ATTR_CLASSPATH) ? attrs.getValue(ATTR_CLASSPATH) : StringUtils.EMPTY;
        return new ArrayList<>(Arrays.asList(StringUtils.split(classpath, " ")));
    }

    private void mergeModuleLibrariesIntoMap(ApplicationModule containedModule) {
        List<TFile> moduleLibraries = containedModule.getLibraryFiles();
        for (TFile library : moduleLibraries) {
            String jarName = library.getName();

            List<ApplicationModule> libraryMappings = libraryMap.get(jarName);
            if (libraryMappings == null) {
                libraryMappings = new ArrayList<>();
                libraryMap.put(jarName, libraryMappings);
            }
            libraryMappings.add(containedModule);
        }
    }

    /**
     * Applies the modified packaging layout, providing an EAR layout that is LTW friendly
     */
    private void applyPackagingLayoutToJar(ApplicationModule sharedModule, String jarName, List<ApplicationModule> moduleList) {
        Validate.notNull(getTargetModule(), "No target module");

        try {
            if (isPinnedLibrary(jarName)) { // pinned library, do not move
                moduleList.forEach(war -> log.debug(MSGDEBUG_PINNED_LIBRARY + jarName + " [" + war.getName() + "]"));

            } else if (isEarLibrary(jarName)) { // ear library
                log.debug(MSGDEBUG_EAR_LIBRARY + jarName);
                getTargetModule().addLib(new TFile(moduleList.get(0).getLibrary(), jarName));
                for (ApplicationModule webmodule : moduleList) {
                    webmodule.removeLib(jarName);
                }

            } else if (moduleList.size() > 1) { // purgable library (shared)
                log.debug(MSGDEBUG_COMMUNAL_LIBRARY + jarName);
                List<ApplicationModule> copyManifest = new ArrayList<>(moduleList);
                boolean inCommunal = copyManifest.remove(sharedModule);
                if (!inCommunal) {
                    sharedModule.addLib(new TFile(moduleList.get(0).getLibrary(), jarName));
                }
                for (ApplicationModule module : copyManifest) {
                    module.removeLib(jarName);
                }

            } else if (moduleList.size() == 1) { // war library
                log.debug("{}{} [{}]", MSGDEBUG_SINGLE_LIBRARY, jarName, moduleList.get(0).getName());
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public String getSharedModuleName() {
        return sharedModuleName;
    }

    private boolean isPinnedLibrary(String jarName) {
        if (pinnedLibraries != null) {
            for (LibraryFilter lib : pinnedLibraries) {
                if (lib.isMatch(jarName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEarLibrary(String jarName) {
        if (StringUtils.isNotBlank(jarName)) {
            if (earLibraries != null) {
                for (LibraryFilter lib : earLibraries) {
                    if (lib.isMatch(jarName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
