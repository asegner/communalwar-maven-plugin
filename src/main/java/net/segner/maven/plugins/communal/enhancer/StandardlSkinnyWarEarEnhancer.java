package net.segner.maven.plugins.communal.enhancer;

import lombok.extern.slf4j.Slf4j;
import net.segner.maven.plugins.communal.module.EarModule;

import java.io.IOException;

@Slf4j
public class StandardlSkinnyWarEarEnhancer extends SkinnyWarEarEnhancer implements ModuleEnhancer<EarModule> {

    public static final String MSGINFO_CREATING_SKINNY_WARS = "Enhancing EAR with Standard Skinny WAR layout";

    @Override
    public void enhance() throws IOException {
        log.info(MSGINFO_CREATING_SKINNY_WARS);
        makeSkinnyModules();
        log.info(MSGINFO_SUCCESS);
    }
}
