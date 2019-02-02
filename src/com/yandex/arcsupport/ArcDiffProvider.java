package com.yandex.arcsupport;

import java.io.File;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.SimpleContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.diff.ItemLatestState;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.yandex.arcsupport.repo.ArcRepository;
import org.jetbrains.annotations.Nullable;

public class ArcDiffProvider implements DiffProvider {
    private final Project myProject;

    public ArcDiffProvider(Project myProject) {
        this.myProject = myProject;
    }

    @Nullable
    @Override
    public VcsRevisionNumber getCurrentRevision(VirtualFile virtualFile) {
        // TODO :

        VirtualFile vcsRoot = LocalFileSystem.getInstance()
                .findFileByIoFile(new File("/home/elwood/proj/arcadia-arc/arcadia"));
        String currentRev = new ArcRepository(vcsRoot).getCurrentRevision();
        ArcRevisionNumber beforeRevisionNumber = new ArcRevisionNumber(currentRev);

        return beforeRevisionNumber;
    }

    @Nullable
    @Override
    public ItemLatestState getLastRevision(VirtualFile virtualFile) {
        // TODO :
        VirtualFile vcsRoot = LocalFileSystem.getInstance()
                .findFileByIoFile(new File("/home/elwood/proj/arcadia-arc/arcadia"));
        String currentRev = new ArcRepository(vcsRoot).getCurrentRevision();
        ArcRevisionNumber beforeRevisionNumber = new ArcRevisionNumber(currentRev);

        // TODO:
        return new ItemLatestState(beforeRevisionNumber, true, true);
    }

    @Nullable
    @Override
    public ItemLatestState getLastRevision(FilePath filePath) {
        // TODO :
        VirtualFile vcsRoot = LocalFileSystem.getInstance()
                .findFileByIoFile(new File("/home/elwood/proj/arcadia-arc/arcadia"));
        String currentRev = new ArcRepository(vcsRoot).getCurrentRevision();
        ArcRevisionNumber beforeRevisionNumber = new ArcRevisionNumber(currentRev);

        // TODO:
        return new ItemLatestState(beforeRevisionNumber, true, true);
    }

    @Nullable
    @Override
    public ContentRevision createFileContent(VcsRevisionNumber vcsRevisionNumber, VirtualFile virtualFile) {
        // TODO : Charset?
        VirtualFile vcsRoot = LocalFileSystem.getInstance()
                .findFileByIoFile(new File("/home/elwood/proj/arcadia-arc/arcadia"));

        try {
            // TODO : mb use VcsFileUtil.relativePath(vcsRoot, virtualFile) instead of virtualFile.getPath()
            FilePath filePath = ArcContentRevision.createPath(vcsRoot, virtualFile.getPath(), false);
            return new ArcContentRevision(filePath, (ArcRevisionNumber) vcsRevisionNumber, myProject, null);
        } catch (VcsException e) {
            // TODO :
            throw new RuntimeException(e);
        }


        //return new SimpleContentRevision(
        //        "Source file",
        //        new LocalFilePath(virtualFile.getPath(), false),
        //        "1"
        //);
    }

    @Nullable
    @Override
    public VcsRevisionNumber getLatestCommittedRevision(VirtualFile virtualFile) {
        // TODO :
        VirtualFile vcsRoot = LocalFileSystem.getInstance()
                .findFileByIoFile(new File("/home/elwood/proj/arcadia-arc/arcadia"));
        String currentRev = new ArcRepository(vcsRoot).getCurrentRevision();
        ArcRevisionNumber beforeRevisionNumber = new ArcRevisionNumber(currentRev);
        return beforeRevisionNumber;
    }
}
