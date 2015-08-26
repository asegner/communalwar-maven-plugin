package net.segner.maven.plugins.communal.enhancer;

import net.segner.maven.plugins.communal.module.EarModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommunalSkinnyWarEarEnhancer extends SkinnyWarEarEnhancer implements ModuleEnhancer<EarModule> {
    private static final Logger logger = LoggerFactory.getLogger(CommunalSkinnyWarEarEnhancer.class);

    public static final String MSGINFO_CREATING_SKINNY_WARS = "Enhancing EAR with Communal Skinny WAR pattern";
    public static final String MSGINFO_SUCCESS = "Enhancement complete";

    public CommunalSkinnyWarEarEnhancer(String communalBundleName) {
        setCommunalModule(communalBundleName);
    }

    @Override
    public void enhance() throws IOException {
        logger.info(MSGINFO_CREATING_SKINNY_WARS);
        makeSkinnyModules();
        logger.info(MSGINFO_SUCCESS);
    }

    public void setCommunalModule(String communalModule) {
        //TODO verify the module exists in the ear
        sharedModuleName = communalModule;
    }
}
