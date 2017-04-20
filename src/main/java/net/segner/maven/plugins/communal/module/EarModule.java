package net.segner.maven.plugins.communal.module;

import lombok.extern.slf4j.Slf4j;
import net.java.truevfs.access.TFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class EarModule extends GenericApplicationModule {

    public static final String MSGINFO_FOUND_MODULE = "Found module: ";
    public static final String MSGINFO_SKIPPING_EAR_FOLDER_ENTRY = "Skipping ear entry: ";

    private static final String DEFAULT_LIB_RELATIVE_LOCATION = File.separator + "lib";
    private static final String[] EAR_METADATA_FILELIST = StringUtils.split("APP-INF META-INF".toLowerCase());
    private static final String MSGINFO_UNPACKED = " <unpacked>";

    EarModule(ApplicationModuleProvider applicationModuleProvider) {
        this.applicationModuleProvider = applicationModuleProvider;
    }

    public EarModule(ApplicationModuleProvider applicationModuleProvider, String path) {
        super(path);
        this.applicationModuleProvider = applicationModuleProvider;
    }

    protected ApplicationModuleProvider applicationModuleProvider;

    @Override
    public String getDefaultLibraryPath() {
        return DEFAULT_LIB_RELATIVE_LOCATION;
    }

    @Nonnull
    public Map<String, ApplicationModule> getModules() throws IOException {
        if (moduleNameToModuleMap == null) {
            createModuleMap();
        }
        return Collections.unmodifiableMap(moduleNameToModuleMap);
    }

    /**
     * tests folders for inclusion into the module list.
     *
     * @param file file/folder under test for inclusion
     * @return false if filename ends with any value in EAR_METADATA_FILELIST or the ear's library location, true otherwise
     */
    protected boolean isEarMetadata(TFile file) {
        String filename = FilenameUtils.getName(file.getPath().toLowerCase());
        String libFilename = FilenameUtils.getName(getLibrary().getPath().toLowerCase());
        return StringUtils.endsWithAny(filename, EAR_METADATA_FILELIST) || StringUtils.endsWithAny(filename, libFilename);
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
        createModule(earFiles, false);

        //create modules for any remaining unpacked modules
        createModule(earFolders, true);
    }

    private void createModule(Collection<TFile> tfiles, boolean unpacked) throws IOException {
        for (TFile moduleReference : tfiles) {
            try {
                // add ear module
                GenericApplicationModule gam = applicationModuleProvider.get(moduleReference, this);
                moduleNameToModuleMap.put(gam.getName(), gam);

                if (log.isInfoEnabled()) {
                    log.info(MSGINFO_FOUND_MODULE + gam.getName() + (unpacked ? MSGINFO_UNPACKED : StringUtils.EMPTY));
                }
            } catch (IllegalArgumentException | IllegalModuleException ex) {
                log.warn(MSGINFO_SKIPPING_EAR_FOLDER_ENTRY + moduleReference.getName());
            }
        }
    }
}

