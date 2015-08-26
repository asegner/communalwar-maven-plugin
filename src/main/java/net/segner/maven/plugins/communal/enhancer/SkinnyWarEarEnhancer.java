package net.segner.maven.plugins.communal.enhancer;

import net.java.truevfs.access.TFile;
import net.segner.maven.plugins.communal.LibraryFilter;
import net.segner.maven.plugins.communal.module.ApplicationModule;
import net.segner.maven.plugins.communal.module.EarModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SkinnyWarEarEnhancer extends AbstractEnhancer<EarModule> implements ModuleEnhancer<EarModule> {
    private static final Logger logger = LoggerFactory.getLogger(SkinnyWarEarEnhancer.class);

    public static final String MSGINFO_CREATING_SKINNY_WARS = "Purging modules to skinny";

    public static final String MSGDEBUG_COMMUNAL_LIBRARY = " * skinny: ";
    public static final String MSGDEBUG_SINGLE_LIBRARY = "individual: ";
    public static final String MSGDEBUG_PINNED_LIBRARY = "pinned: ";
    public static final String MSGDEBUG_EAR_LIBRARY = "ear library: ";

    private Map<String, List<ApplicationModule>> libraryMap;
    private List<LibraryFilter> pinnedLibraries;
    private List<LibraryFilter> earLibraries;
    protected String sharedModuleName;

    @Override
    public void enhance() throws IOException {
        logger.info(MSGINFO_CREATING_SKINNY_WARS);
        makeSkinnyModules();
    }

    public void setEarLibraries(List<LibraryFilter> earLibraries) {
        this.earLibraries = earLibraries;
    }

    public void setPinnedLibraries(List<LibraryFilter> pinnedLibraries) {
        this.pinnedLibraries = pinnedLibraries;
    }

    /**
     * move any artifacts with more than one location over to the communalWar
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
        final ApplicationModule communalWar = earModules.get(sharedModuleName);
        libraryMap.forEach((jarName, warList) -> applyPackagingLayoutToJar(communalWar, jarName, warList));
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
     * Applies the communal war packaging layout, providing an EAR layout that is LTW friendly
     */
    private void applyPackagingLayoutToJar(ApplicationModule communalWar, String jarName, List<ApplicationModule> moduleList) {
        Validate.notNull(getTargetModule(), "No target module");

        try {
            if (isPinnedLibrary(jarName)) { // pinned library, do not move
                moduleList.forEach(war -> logger.debug(MSGDEBUG_PINNED_LIBRARY + jarName + " [" + war.getName() + "]"));

            } else if (isEarLibrary(jarName)) { // ear library
                logger.debug(MSGDEBUG_EAR_LIBRARY + jarName);
                getTargetModule().addLib(new TFile(moduleList.get(0).getLibrary(), jarName));
                for (ApplicationModule webmodule : moduleList) {
                    webmodule.removeLib(jarName);
                }

            } else if (moduleList.size() > 1) { // purgable library (shared)
                logger.debug(MSGDEBUG_COMMUNAL_LIBRARY + jarName);
                List<ApplicationModule> copyManifest = new ArrayList<>(moduleList);
                boolean inCommunal = copyManifest.remove(communalWar);
                if (!inCommunal) {
                    communalWar.addLib(new TFile(moduleList.get(0).getLibrary(), jarName));
                }
                for (ApplicationModule module : copyManifest) {
                    module.removeLib(jarName);
                }

            } else if (moduleList.size() == 1) { // war library
                logger.debug(MSGDEBUG_SINGLE_LIBRARY + jarName + " [" + moduleList.get(0).getName() + "]");
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
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
