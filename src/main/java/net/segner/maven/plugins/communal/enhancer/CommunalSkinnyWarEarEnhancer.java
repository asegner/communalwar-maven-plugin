package net.segner.maven.plugins.communal.enhancer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.extern.slf4j.Slf4j;
import net.segner.maven.plugins.communal.module.EarModule;

import java.io.IOException;

@Slf4j
public class CommunalSkinnyWarEarEnhancer extends SkinnyWarEarEnhancer implements ModuleEnhancer<EarModule> {

    public static final String MSGINFO_CREATING_SKINNY_WARS = "Enhancing EAR with Communal Skinny WAR layout";

    @Inject
    public CommunalSkinnyWarEarEnhancer(@Assisted String communalBundleName) {
        setCommunalModule(communalBundleName);
    }

    @Override
    public void enhance() throws IOException {
        log.info(MSGINFO_CREATING_SKINNY_WARS);
        makeSkinnyModules();
        log.info(MSGINFO_SUCCESS);
    }

    public void setCommunalModule(String communalModule) {
        sharedModuleName = communalModule;
    }
}
