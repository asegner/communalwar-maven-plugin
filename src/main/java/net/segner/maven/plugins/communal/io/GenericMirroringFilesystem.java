package net.segner.maven.plugins.communal.io;

import net.java.truevfs.access.TFile;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Pushes changes to two filesystems
 */
public class GenericMirroringFilesystem implements MirroringFilesystem {

    private TFile master, slave;

    protected GenericMirroringFilesystem() {
    }

    public GenericMirroringFilesystem(TFile master, TFile slave) {
        setTargets(master, slave);
    }

    public GenericMirroringFilesystem(File master, File slave) {
        this(new TFile(master), new TFile(slave));
    }

    protected void setTargets(TFile master, TFile slave) {
        Validate.notNull(master);
        if (master.equals(slave)) {
            slave = null;
        }
        this.master = master;
        this.slave = slave;
    }

    @Override
    public TFile[] listFiles() {
        TFile[] files = master.listFiles();
        return (files == null) ? new TFile[0] : files;
    }

    @Override
    public TFile[] listFiles(FileFilter filter) {
        TFile[] files = master.listFiles(filter);
        return (files == null) ? new TFile[0] : files;
    }

    @Override
    public Pair<TFile, TFile> getTargets() {
        return Pair.of(master, slave);
    }

    @Override
    public boolean canRead() {
        return master.canRead() && (slave == null || !slave.exists() || slave.canRead());
    }

    @Override
    public boolean canWrite() {
        return master.canWrite() && (slave == null || !slave.exists() || slave.canWrite());
    }

    @Override
    public void rm(String relativePath) throws IOException {
        for (TFile target : targetList()) {
            if (!target.exists()) continue;
            new TFile(target, relativePath).rm_r();
        }
    }

    @Override
    public void copy(TFile file, TFile destination) throws IOException {
        if (master.isParentOf(destination)) {
            copy(file, master.toPath().relativize(destination.toPath()).toString());
        } else if (slave != null && slave.isParentOf(destination)) {
            copy(file, slave.toPath().relativize(destination.toPath()).toString());
        } else {
            throw new IOException(destination.getPath() + " is not a child of " + master.getPath());
        }
    }

    @Override
    public void copy(TFile file, String relativeDestination) throws IOException {
        for (TFile target : targetList()) {
            if (!target.exists()) continue;
            TFile destination = new TFile(target, relativeDestination);
            file.toNonArchiveFile().cp_rp(new TFile(destination, file.getName()));
        }
    }

    private List<TFile> targetList() {
        return Arrays.asList(master, slave);
    }
}
