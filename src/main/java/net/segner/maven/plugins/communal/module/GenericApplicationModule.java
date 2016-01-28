package net.segner.maven.plugins.communal.module;

import net.java.truevfs.access.TFile;
import net.java.truevfs.access.TFileInputStream;
import net.java.truevfs.access.TFileOutputStream;
import net.segner.maven.plugins.communal.io.GenericMirroringFilesystem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

public abstract class GenericApplicationModule extends GenericMirroringFilesystem implements ApplicationModule {

    private static final String LIBRARY_EXTENSION = "jar";

    private String moduleName;
    private String libLocation = getDefaultLibraryPath();

    protected Map<String, ApplicationModule> moduleNameToModuleMap;

    GenericApplicationModule() {
    }

    void init(String archivePath) {
        init(Paths.get(archivePath).toFile());
    }

    void init(File archive) {
        setName(FilenameUtils.getName(archive.getPath()));
        setTargets(new TFile(archive), new TFile(stripExtensionFromName(archive.getPath())));
    }

    public GenericApplicationModule(String archivePath) {
        init(archivePath);
    }

    public GenericApplicationModule(Path archivePath) {
        init(archivePath.toFile());
    }

    public GenericApplicationModule(File archive) {
        init(archive);
    }

    protected String stripExtensionFromName(String name) {
        return FilenameUtils.getFullPath(name) + FilenameUtils.getBaseName(name);
    }

    @Override
    public boolean isUnpacked() {
        TFile right = getTargets().getRight();
        return right != null && right.exists();
    }

    @Override
    public void setUnpacked(TFile unpackedFile) {
        Pair<TFile, TFile> targets = getTargets();
        setTargets(targets.getLeft(), unpackedFile);
    }

    @Override
    public TFile getModuleRoot() {
        return getTargets().getLeft();
    }

    public TFile getUnpackFolder() {
        return getTargets().getRight();
    }

    @Override
    public TFile getLibrary() {
        return new TFile(getTargets().getLeft(), libLocation);
    }

    @Override
    public void setLibraryPath(String relativeLibLocation) {
        moduleNameToModuleMap = null;
        libLocation = relativeLibLocation;
    }

    @Override
    public List<TFile> getLibraryFiles() {
        TFile[] files = getLibrary().listFiles((FileFilter) new WildcardFileFilter("*." + LIBRARY_EXTENSION));
        if (files != null) {
            return Arrays.asList(files);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    @Nonnull
    public String getName() {
        return StringUtils.defaultString(moduleName);
    }

    @Override
    public void setName(String name) {
        this.moduleName = name;
    }

    @Override
    public void addLib(TFile libraryFile) throws IOException {
        copy(libraryFile, getLibrary());
    }


    @Override
    public void removeLib(String libraryName) throws IOException {
        rm(libLocation + File.separator + libraryName);
    }

    @Nonnull
    private TFile getManifestTfile(boolean unpacked) {
        return new TFile(unpacked ? getUnpackFolder() : getModuleRoot(), "META-INF/MANIFEST.MF");
    }


    @Override
    @Nonnull
    public Manifest getManifest() throws IOException {
        TFile manifestTfile = getManifestTfile(false);
        if (manifestTfile.exists() || manifestTfile.createNewFile()) {
            try (TFileInputStream in = new TFileInputStream(manifestTfile)) {
                return new Manifest(in);
            }
        }
        throw new IOException("Unable to write manifest");
    }

    @Override
    public void saveManifest(@Nonnull Manifest manifest) throws IOException {
        TFile manifestTfile = getManifestTfile(false);
        try (TFileOutputStream outputStream = new TFileOutputStream(manifestTfile)) {
            manifest.write(outputStream);
        }
        if( isUnpacked() ){
            manifestTfile = getManifestTfile(true);
            try (TFileOutputStream outputStream = new TFileOutputStream(manifestTfile)) {
                manifest.write(outputStream);
            }
        }
    }
}
