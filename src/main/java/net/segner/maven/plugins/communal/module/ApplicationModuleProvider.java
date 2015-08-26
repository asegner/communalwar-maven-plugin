package net.segner.maven.plugins.communal.module;

import net.java.truevfs.access.TFile;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;


public class ApplicationModuleProvider {
    private ApplicationModuleProvider() {
    }

    /**
     * Builds a ApplicationModule for the provided path
     *
     * @param path File path of the module root folder / file
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends GenericApplicationModule> T get(TFile path) throws IOException, IllegalModuleException {

        // Basic sanity check
        Validate.notNull(path);
        String name = path.getName();
        Validate.isTrue(path.exists(), "File path does not exist: " + name);

        GenericApplicationModule module = findByDefaultPathInspection(path);
        if (module != null) {
            Validate.isTrue(module.canRead(), "Unable to read module " + name);
            Validate.isTrue(module.canWrite(), "Unable to write module " + name);
            return (T) module;
        }

        // Unable to determine type
        throw new IllegalModuleException("Path does not represent a known application module");
    }

    public static <T extends GenericApplicationModule> T get(TFile path, GenericApplicationModule container) throws IOException, IllegalModuleException {
        T module = get(path);
        module.setUnpacked(new TFile(container.getUnpackFolder(), container.getModuleRoot().toPath().relativize(module.getModuleRoot().toPath()).toString()));
        return module;
    }

    /**
     * returns a module based on inspecting expected default paths that would exist in known modules
     */
    @Nullable
    private static GenericApplicationModule findByDefaultPathInspection(TFile path) {
        // check if its a WAR by looking for WEB-INF
        TFile webmd = new TFile(path, WebModule.DEFAULT_WEBMODULE_METADATAPATH);
        if (webmd.exists()) {
            return new WebModule(path);
        }

        // TODO check if its a JAR by looking for
        //
        return null;
    }
}
