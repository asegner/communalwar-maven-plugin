package net.segner.maven.plugins.communal.module;

import net.java.truevfs.access.TFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Map;


public class WebModule extends GenericApplicationModule implements ApplicationModule {
    private static final Logger logger = LoggerFactory.getLogger(WebModule.class);

    public static final String DEFAULT_WEBMODULE_METADATAPATH = "WEB-INF";
    public static final String DEFAULT_WEBMODULE_LIBPATH = DEFAULT_WEBMODULE_METADATAPATH + File.separator + "lib";

    public WebModule(TFile archivePath) {
        super(archivePath);
    }

    @Override
    public String getDefaultLibraryPath() {
        return DEFAULT_WEBMODULE_LIBPATH;
    }

    /**
     * can web applications contain other modules?
     */
    @Override
    public Map<String, ApplicationModule> getModules() {
        logger.debug("Retreiving modules from a web application module");
        return Collections.emptyMap();
    }
}
