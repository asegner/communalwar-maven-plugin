package net.segner.maven.plugins.communal;

import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import net.java.truevfs.access.TVFS;
import net.java.truevfs.kernel.spec.FsSyncException;
import net.segner.maven.plugins.communal.enhancer.ModuleEnhancer;
import net.segner.maven.plugins.communal.module.EarModule;
import org.apache.commons.lang3.Validate;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import javax.inject.Inject;

@Slf4j
public class EarLayoutEnhancer {

    @Inject
    private EarModule earModule;
    @Inject
    private ModuleEnhancer<EarModule> earModuleEnhancer;
    @Inject
    @Named("warningBreaksBuild")
    private Boolean warningBreaksBuild;

    public void start() throws MojoExecutionException, MojoFailureException {
        try {
            // get the ear module
            Validate.isTrue(earModule.canRead() && earModule.canWrite(), "Missing read / write permissions to ear target folder");

            // enhance the ear with the configured enhancer chain including the skinny war pattern
            earModuleEnhancer.setTargetModule(earModule);
            earModuleEnhancer.enhance();

        } catch (IllegalArgumentException ex) {
            log.warn(ex.getMessage());
            if (warningBreaksBuild) {
                throw new MojoFailureException(ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            try {
                TVFS.umount();
            } catch (FsSyncException e) {
                //noinspection ThrowFromFinallyBlock
                throw new MojoExecutionException("TVFS failed to unmount cleanly", e);
            }
        }
    }
}
