package net.segner.maven.plugins.communal.io;


import net.java.truevfs.access.TFile;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileFilter;
import java.io.IOException;

/**
 * Pushes identical changes to two filesystems that are mirrored copies of each other.
 * Intended to ease manipulation of an unpacked and packed filesystem at one time.
 */
public interface MirroringFilesystem {

    TFile[] listFiles();

    TFile[] listFiles(FileFilter filter);

    /**
     * @return TFile Pair with left being the master target, and right being the slave target
     */
    Pair<TFile, TFile> getTargets();

    /**
     * @return true if both filesystems are readable
     */
    boolean canRead();

    /**
     * @return true if both filesystems are writable
     */
    boolean canWrite();

    /**
     * Recursively deletes the given file or directory tree.
     * <p>
     * This file system operation is <em>not</em> atomic.
     *
     * @param relativePath path to be removed from both filesystems
     * @throws IOException if any I/O error occurs.
     */
    void rm(String relativePath) throws IOException;

    /**
     * Copies the data from the source {@code source} to the destination
     * {@code destination} and closes both streams - even if an exception occurs.
     *
     * @param source the input file
     * @param destination the output file
     * @throws IOException if any I/O error occurs.
     */
    void copy(TFile source, TFile destination) throws IOException;

    void copy(TFile source, String relativeDestination) throws IOException;
}
