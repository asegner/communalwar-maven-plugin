package net.segner.maven.plugins.communal;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import net.segner.maven.plugins.communal.enhancer.CommunalSkinnyWarEarEnhancer;
import net.segner.maven.plugins.communal.enhancer.CommunalSkinnyWarEarEnhancerFactory;
import net.segner.maven.plugins.communal.enhancer.ModuleEnhancer;
import net.segner.maven.plugins.communal.enhancer.SkinnyWarEarEnhancer;
import net.segner.maven.plugins.communal.enhancer.StandardlSkinnyWarEarEnhancer;
import net.segner.maven.plugins.communal.enhancer.WeblogicLtwMetadataEnhancer;
import net.segner.maven.plugins.communal.module.ApplicationModuleProvider;
import net.segner.maven.plugins.communal.module.EarModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EarLayoutEnhancerModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(EarLayoutEnhancerModule.class);
    public static final List<String> ASPECTJLIBRARIES = Arrays.asList(StringUtils.split("aopalliance aspectjweaver aspectjrt"));

    private String communalModuleName;
    private List<LibraryFilter> earLibraryList;
    private List<LibraryFilter> pinnedLibraryList;
    private Boolean forceAspectJLibToEar;
    private Boolean generateWeblogicLtwMetadata;
    private Boolean warningBreaksBuild;
    private Build build;


    public EarLayoutEnhancerModule(String communalModuleName,
                                   List<LibraryFilter> earLibraryList,
                                   List<LibraryFilter> pinnedLibraryList,
                                   Boolean forceAspectJLibToEar,
                                   Boolean generateWeblogicLtwMetadata,
                                   Boolean warningBreaksBuild,
                                   Build build) {
        this.communalModuleName = communalModuleName;
        this.earLibraryList = earLibraryList;
        this.pinnedLibraryList = pinnedLibraryList;
        this.forceAspectJLibToEar = forceAspectJLibToEar;
        this.generateWeblogicLtwMetadata = generateWeblogicLtwMetadata;
        this.warningBreaksBuild = warningBreaksBuild;
        this.build = build;
    }

    @Override
    protected void configure() {
        bind(Boolean.class)
                .annotatedWith(Names.named("warningBreaksBuild"))
                .toInstance(this.warningBreaksBuild);
        bind(Build.class)
                .annotatedWith(Names.named("project.build"))
                .toInstance(this.build);

        install(new FactoryModuleBuilder()
                .implement(ModuleEnhancer.class, CommunalSkinnyWarEarEnhancer.class)
                .build(CommunalSkinnyWarEarEnhancerFactory.class));
    }

    @Nonnull
    @Provides
    public EarModule earModule(ApplicationModuleProvider applicationModuleProvider) {
        return applicationModuleProvider.getEar();
    }


    @Nonnull
    @Provides
    public ModuleEnhancer<EarModule> earModuleEnhancer(CommunalSkinnyWarEarEnhancerFactory communalSkinnyWarEarEnhancerProvider, Provider<StandardlSkinnyWarEarEnhancer> standardlSkinnyWarEarEnhancerProvider) {
        // merge ear library list with aspectj list for the complete list
        List<LibraryFilter> fullEarLibraryList = new ArrayList<>(earLibraryList);
        if (forceAspectJLibToEar) {
            ASPECTJLIBRARIES.forEach(earLib -> fullEarLibraryList.add(new LibraryPrefixFilter(earLib)));
        }

        // add skinny enhancer to enhancer chain
        SkinnyWarEarEnhancer skinnyEnhancer = StringUtils.isNotBlank(communalModuleName) ?
                communalSkinnyWarEarEnhancerProvider.forCommunalName(communalModuleName) :
                standardlSkinnyWarEarEnhancerProvider.get();
        skinnyEnhancer.setPinnedLibraries(pinnedLibraryList);
        skinnyEnhancer.setEarLibraries(fullEarLibraryList);

        // add weblogic ltw metadata generation to enhancer chain
        if (generateWeblogicLtwMetadata) {
            WeblogicLtwMetadataEnhancer ltwEnhancer = new WeblogicLtwMetadataEnhancer();
            ltwEnhancer.setSkinnyEnhancer(skinnyEnhancer);
            return ltwEnhancer;
        } else {
            return skinnyEnhancer;
        }
    }
}
