package arc4idea.status;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.VcsDirtyScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

abstract class ArcChangesCollector {
    @NotNull protected final Project myProject;
    @NotNull protected final VirtualFile myVcsRoot;

    @NotNull private final VcsDirtyScope myDirtyScope;
    @NotNull private final ChangeListManager myChangeListManager;
    @NotNull private final ProjectLevelVcsManager myVcsManager;
    @NotNull private final AbstractVcs myVcs;

    ArcChangesCollector(@NotNull Project project, @NotNull ChangeListManager changeListManager, @NotNull ProjectLevelVcsManager vcsManager,
            @NotNull AbstractVcs vcs, @NotNull VcsDirtyScope dirtyScope, @NotNull VirtualFile vcsRoot) {
        myProject = project;
        myChangeListManager = changeListManager;
        myVcsManager = vcsManager;
        myVcs = vcs;
        myDirtyScope = dirtyScope;
        myVcsRoot = vcsRoot;
    }

    /**
     * @return the set of unversioned files (from the specified dirty scope).
     */
    abstract @NotNull Collection<VirtualFile> getUnversionedFiles();

    /**
     * @return the set of changes (changed files) from the specified dirty scope.
     */
    abstract @NotNull Collection<Change> getChanges();

    /**
     * Collect dirty file paths
     *
     * @param includeChanges if true, previous changes are included in collection
     * @return the set of dirty paths to check, the paths are automatically collapsed if the summary length more than limit
     */
    protected Collection<FilePath> dirtyPaths(boolean includeChanges) {
        final List<String> allPaths = new ArrayList<>();

        for (FilePath p : myDirtyScope.getRecursivelyDirtyDirectories()) {
            addToPaths(p, allPaths);
        }
        for (FilePath p : myDirtyScope.getDirtyFilesNoExpand()) {
            addToPaths(p, allPaths);
        }

        if (includeChanges) {
            for (Change c : myChangeListManager.getChangesIn(myVcsRoot)) {
                switch (c.getType()) {
                    case NEW:
                    case DELETED:
                    case MOVED:
                        ContentRevision afterRevision = c.getAfterRevision();
                        if (afterRevision != null) {
                            addToPaths(afterRevision.getFile(), allPaths);
                        }
                        ContentRevision beforeRevision = c.getBeforeRevision();
                        if (beforeRevision != null) {
                            addToPaths(beforeRevision.getFile(), allPaths);
                        }
                    case MODIFICATION:
                    default:
                        // do nothing
                }
            }
        }

        removeCommonParents(allPaths);

        return ContainerUtil.map(allPaths, VcsUtil::getFilePath);
    }

    protected void addToPaths(FilePath pathToAdd, List<String> paths) {
        VcsRoot fileRoot = myVcsManager.getVcsRootObjectFor(pathToAdd);
        if (fileRoot != null && fileRoot.getVcs() != null && myVcs.equals(fileRoot.getVcs())
                // TODO : fix contentRoots to be consistent with vcsRoot
                /*&& myVcsRoot.equals(fileRoot.getPath())*/) {
            paths.add(pathToAdd.getPath());
        }
    }

    protected static void removeCommonParents(List<String> allPaths) {
        Collections.sort(allPaths);

        String prevPath = null;
        Iterator<String> it = allPaths.iterator();
        while (it.hasNext()) {
            String path = it.next();
            if (prevPath != null && FileUtil.startsWith(path, prevPath, true)) { // the file is under previous file, so enough to check the parent
                it.remove();
            }
            else {
                prevPath = path;
            }
        }
    }

}
