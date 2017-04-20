package net.segner.maven.plugins.communal.module;

import net.java.truevfs.access.TFile;

import java.io.File;


public class WebModule extends GenericApplicationModule implements ApplicationModule {

    public static final String DEFAULT_WEBMODULE_METADATAPATH = "WEB-INF";
    public static final String DEFAULT_WEBMODULE_LIBPATH = DEFAULT_WEBMODULE_METADATAPATH + File.separator + "lib";

    WebModule() {
    }

    public WebModule(TFile archivePath) {
        super(archivePath);
    }

    @Override
    public String getDefaultLibraryPath() {
        return DEFAULT_WEBMODULE_LIBPATH;
    }

    void init(TFile path) {
        super.init(path);
    }
}
