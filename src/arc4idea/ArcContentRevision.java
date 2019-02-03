package arc4idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ByteBackedContentRevision;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.impl.ContentRevisionCache;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsFileUtil;
import com.intellij.vcsUtil.VcsUtil;
import arc4idea.repo.ArcRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import static com.intellij.openapi.vcs.impl.ContentRevisionCache.UniqueType.REPOSITORY_CONTENT;

public class ArcContentRevision implements ByteBackedContentRevision {
    @NotNull protected final FilePath myFile;
    @NotNull private final ArcRevisionNumber myRevision;
    @NotNull private final Project myProject;
    @Nullable private final Charset myCharset;

    protected ArcContentRevision(@NotNull FilePath file,
            @NotNull ArcRevisionNumber revision,
            @NotNull Project project,
            @Nullable Charset charset) {
        myProject = project;
        myFile = file;
        myRevision = revision;
        myCharset = charset;
    }

    @Override
    @Nullable
    public String getContent() throws VcsException {
        byte[] bytes = getContentAsBytes();
        if (bytes == null) return null;
        return ContentRevisionCache.getAsString(bytes, myFile, myCharset);
    }

    @Nullable
    @Override
    public byte[] getContentAsBytes() throws VcsException {
        if (myFile.isDirectory()) {
            return null;
        }
        try {
            return ContentRevisionCache.getOrLoadAsBytes(myProject, myFile, myRevision, ArcVcs.getKey(), REPOSITORY_CONTENT, this::loadContent);
        }
        catch (IOException e) {
            throw new VcsException(e);
        }
    }

    private byte[] loadContent() throws VcsException {
        // TODO :
        VirtualFile root = //GitUtil.getGitRoot(myFile);
                LocalFileSystem.getInstance().findFileByIoFile(new File("/home/elwood/proj/arcadia-arc/arcadia"));
        //return GitFileUtils.getFileContent(myProject, root, myRevision.getRev(), VcsFileUtil.relativePath(root, myFile));
        return new ArcRepository(root)
                .getFileContent(myProject, root, myRevision, VcsFileUtil.relativePath(root, myFile));
    }

    @Override
    @NotNull
    public FilePath getFile() {
        return myFile;
    }

    @Override
    @NotNull
    public VcsRevisionNumber getRevisionNumber() {
        return myRevision;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ((obj == null) || (obj.getClass() != getClass())) return false;

        ArcContentRevision test = (ArcContentRevision)obj;
        return (myFile.equals(test.myFile) && myRevision.equals(test.myRevision));
    }

    public int hashCode() {
        return myFile.hashCode() + myRevision.hashCode();
    }

    /**
     * Create revision
     *
     *
     * @param vcsRoot        a vcs root for the repository
     * @param path           an path inside with possibly escape sequences
     * @param revisionNumber a revision number, if null the current revision will be created
     * @param project        the context project
     * @return a created revision
     * @throws VcsException if there is a problem with creating revision
     */
    @NotNull
    public static ContentRevision createRevision(@NotNull VirtualFile vcsRoot,
            @NotNull String path,
            @Nullable VcsRevisionNumber revisionNumber,
            Project project) {
        FilePath file = createPath(vcsRoot, path);
        return createRevision(file, revisionNumber, project, null);
    }

    @NotNull
    public static ContentRevision createRevisionForTypeChange(@NotNull Project project,
            @NotNull VirtualFile vcsRoot,
            @NotNull String path,
            @Nullable VcsRevisionNumber revisionNumber) {
        FilePath filePath;
        if (revisionNumber == null) {
            File file = new File(makeAbsolutePath(vcsRoot, path));
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            filePath = virtualFile == null ? VcsUtil.getFilePath(file, false) : VcsUtil.getFilePath(virtualFile);
        } else {
            filePath = createPath(vcsRoot, path);
        }
        return createRevision(filePath, revisionNumber, project, null);
    }

    @NotNull
    public static FilePath createPath(@NotNull VirtualFile vcsRoot, @NotNull String path) {
        String absolutePath = makeAbsolutePath(vcsRoot, path);
        return VcsUtil.getFilePath(absolutePath, false);
    }

    @NotNull
    private static String makeAbsolutePath(@NotNull VirtualFile vcsRoot, @NotNull String path) {
        return Paths.get(vcsRoot.getPath(), path).toString();
    }

    @NotNull
    public static ContentRevision createRevision(@NotNull VirtualFile file,
            @Nullable VcsRevisionNumber revisionNumber,
            @NotNull Project project) {
        FilePath filePath = VcsUtil.getFilePath(file);
        return createRevision(filePath, revisionNumber, project, null);
    }

    @NotNull
    public static ContentRevision createRevision(@NotNull FilePath filePath,
            @Nullable VcsRevisionNumber revisionNumber,
            @NotNull Project project,
            @Nullable Charset charset) {
        if (revisionNumber != null && revisionNumber != VcsRevisionNumber.NULL) {
            return createRevisionImpl(filePath, (ArcRevisionNumber)revisionNumber, project, charset);
        } else {
            return CurrentContentRevision.create(filePath);
        }
    }

    @NotNull
    private static ArcContentRevision createRevisionImpl(@NotNull FilePath path,
            @NotNull ArcRevisionNumber revisionNumber,
            @NotNull Project project,
            @Nullable Charset charset) {
        if (path.getFileType().isBinary()) {
            return new ArcBinaryContentRevision(path, revisionNumber, project);
        } else {
            return new ArcContentRevision(path, revisionNumber, project, charset);
        }
    }

    @Override
    public String toString() {
        return myFile.getPath();
    }
}
