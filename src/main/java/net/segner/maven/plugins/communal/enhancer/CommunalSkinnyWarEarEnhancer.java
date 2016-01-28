package net.segner.maven.plugins.communal.enhancer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.segner.maven.plugins.communal.module.EarModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommunalSkinnyWarEarEnhancer extends SkinnyWarEarEnhancer implements ModuleEnhancer<EarModule> {
    private static final Logger logger = LoggerFactory.getLogger(CommunalSkinnyWarEarEnhancer.class);

    public static final String MSGINFO_CREATING_SKINNY_WARS = "Enhancing EAR with Communal Skinny WAR layout";

    @Inject
    public CommunalSkinnyWarEarEnhancer(@Assisted String communalBundleName) {
        setCommunalModule(communalBundleName);
    }

    @Override
    public void enhance() throws IOException {
        logger.info(MSGINFO_CREATING_SKINNY_WARS);
        makeSkinnyModules();
        logger.info(MSGINFO_SUCCESS);
    }

    public void setCommunalModule(String communalModule) {
        sharedModuleName = communalModule;
    }
}
