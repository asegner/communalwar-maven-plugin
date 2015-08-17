package segner.maven.plugins.pojo;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.Validate;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author aaronsegner
 */
public class WebModule {

    protected static final String TYPE_JAVAARCHIVE = "jar";
    protected static final String WEBMODULE_LIBPATH = "WEB-INF" + File.separator + "lib";

    public static final String MSGERR_NOT_UNPACKED_WAR = "Not unpacked WAR";

    private File warFolder;
    private Set<File> libContents;

    public WebModule(File singleFile) throws IOException {
        Validate.isTrue(singleFile.isDirectory(), MSGERR_NOT_UNPACKED_WAR);
        warFolder = singleFile;
        libContents = new HashSet<>(Arrays.asList(getLibFolder().listFiles((FileFilter) new WildcardFileFilter("*." + TYPE_JAVAARCHIVE))));
    }

    public String getName() {
        return warFolder.getName();
    }

    public Set<File> getLibFiles() {
        return libContents;
    }

    public File getLibFolder() {
        return new File(warFolder, WEBMODULE_LIBPATH);
    }

    public void removeLib(String jarName) {
        FileUtils.fileDelete(getLibFolder() + File.separator + jarName);
    }
}
