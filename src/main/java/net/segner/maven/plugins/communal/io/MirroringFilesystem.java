package net.segner.maven.plugins.communal.io;


import net.java.truevfs.access.TFile;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileFilter;
import java.io.IOException;

public interface MirroringFilesystem {

    TFile[] listFiles();

    TFile[] listFiles(FileFilter filter);

    /**
     * @return TFile Pair with left being the master target, and right being the slave target
     */
    Pair<TFile, TFile> getTargets();

    boolean canRead();

    boolean canWrite();

    void rm(String relativePath) throws IOException;

    void copy(TFile file, TFile destination) throws IOException;

    void copy(TFile file, String relativeDestination) throws IOException;
}
