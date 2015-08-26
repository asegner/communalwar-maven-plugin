package net.segner.maven.plugins.communal.enhancer;

import net.segner.maven.plugins.communal.module.ApplicationModule;

import javax.annotation.Nullable;

public abstract class AbstractEnhancer<T extends ApplicationModule> implements ModuleEnhancer<T> {

    protected T targetModule;

    @Override
    public void setTargetModule(T targetModule) {
        this.targetModule = targetModule;
    }

    @Override
    @Nullable
    public T getTargetModule() {
        return targetModule;
    }

}
