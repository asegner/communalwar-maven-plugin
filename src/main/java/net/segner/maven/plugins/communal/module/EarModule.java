package net.segner.maven.plugins.communal.module;

import net.java.truevfs.access.TFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EarModule extends GenericApplicationModule {
    private static final Logger logger = LoggerFactory.getLogger(EarModule.class);

    public static final String MSGINFO_FOUND_MODULE = "Found module: ";
    public static final String MSGINFO_SKIPPING_EAR_FOLDER_ENTRY = "Skipping ear entry: ";

    private static final String DEFAULT_LIB_RELATIVE_LOCATION = File.separator + "lib";
    private static final String[] EAR_METADATA_FILELIST = StringUtils.split("APP-INF META-INF".toLowerCase());
    private static final String MSGINFO_UNPACKED = " <unpacked>";

    public EarModule(String path) {
        super(path);
    }

    @Override
    public String getDefaultLibraryPath() {
        return DEFAULT_LIB_RELATIVE_LOCATION;
    }

    @Override
    @Nonnull
    public Map<String, ApplicationModule> getModules() throws IOException {
        if (moduleNameToModuleMap == null) {
            createModuleMap();
        }
        return moduleNameToModuleMap;
    }

    protected boolean isEarMetadata(TFile file) {
        return StringUtils.endsWithAny(FilenameUtils.getName(file.getPath().toLowerCase()), EAR_METADATA_FILELIST);
    }

    private void createModuleMap() throws IOException {
        moduleNameToModuleMap = new HashMap<>();

        // build module list
        List<TFile> earFiles = Arrays.asList(listFiles(file -> {
            TFile tf = (TFile) file;
            return !tf.toNonArchiveFile().isDirectory() && !isEarMetadata(tf);
        }));
        List<TFile> earFolders = Arrays.asList(listFiles(file -> {
            TFile tf = (TFile) file;
            return tf.toNonArchiveFile().isDirectory() && !isEarMetadata(tf);
        }));
        Validate.isTrue((earFiles.size() + earFolders.size()) > 0, "Ear module should contain at least one application module");

        // create modules referenced in list
        for (TFile moduleReference : earFiles) {
            try {
                // add internal ear module
                GenericApplicationModule gam = ApplicationModuleProvider.get(moduleReference, this);
                moduleNameToModuleMap.put(gam.getName(), gam);

                if (logger.isInfoEnabled()) {
                    logger.info(MSGINFO_FOUND_MODULE + gam.getName());
                }
            } catch (IllegalArgumentException | IllegalModuleException ex) {
                logger.warn(MSGINFO_SKIPPING_EAR_FOLDER_ENTRY + moduleReference.getName());
            }
        }

        //create modules for any remaining unpacked modules
        for (TFile moduleReference : earFolders) {
            try {
                // add ear module
                GenericApplicationModule gam = ApplicationModuleProvider.get(moduleReference, this);
                moduleNameToModuleMap.put(gam.getName(), gam);

                if (logger.isInfoEnabled()) {
                    logger.info(MSGINFO_FOUND_MODULE + gam.getName() + MSGINFO_UNPACKED);
                }
            } catch (IllegalArgumentException | IllegalModuleException ex) {
                logger.warn(MSGINFO_SKIPPING_EAR_FOLDER_ENTRY + moduleReference.getName());
            }
        }
    }

}
