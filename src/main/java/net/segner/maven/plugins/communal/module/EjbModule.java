package net.segner.maven.plugins.communal.module;

import lombok.extern.slf4j.Slf4j;
import net.java.truevfs.access.TFile;

@Slf4j
public class EjbModule extends GenericApplicationModule implements ApplicationModule {

    public static final String DEFAULT_WEBMODULE_LIBPATH = "";
    public static final String EXTENSION = "jar";

    EjbModule() {
    }

    public EjbModule(TFile archivePath) {
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
