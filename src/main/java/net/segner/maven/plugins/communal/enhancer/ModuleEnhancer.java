package net.segner.maven.plugins.communal.enhancer;

import net.segner.maven.plugins.communal.module.ApplicationModule;

import java.io.IOException;

public interface ModuleEnhancer<T extends ApplicationModule> {

    void setTargetModule(T targetModule);

    T getTargetModule();

    void enhance() throws IOException;

}
