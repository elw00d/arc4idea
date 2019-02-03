package arc4idea.status;

import arc4idea.ArcContentRevision;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import arc4idea.ArcRevisionNumber;
import arc4idea.ArcVcs;
import arc4idea.repo.ArcRepository;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

import static com.intellij.util.ObjectUtils.assertNotNull;

/**
 * Arc repository change provider
 */
public class ArcChangeProvider implements ChangeProvider {
    private static final Logger LOG = Logger.getInstance("#ArcStatus");

    @NotNull private final Project myProject;
    @NotNull private final ChangeListManager myChangeListManager;
    @NotNull private final FileDocumentManager myFileDocumentManager;
    @NotNull private final ProjectLevelVcsManager myVcsManager;

    public ArcChangeProvider(@NotNull Project project,
            @NotNull ChangeListManager changeListManager,
            @NotNull FileDocumentManager fileDocumentManager,
            @NotNull ProjectLevelVcsManager vcsManager) {
        myProject = project;
        myChangeListManager = changeListManager;
        myFileDocumentManager = fileDocumentManager;
        myVcsManager = vcsManager;
    }

    @Override
    public void getChanges(@NotNull VcsDirtyScope dirtyScope,
            @NotNull final ChangelistBuilder builder,
            @NotNull final ProgressIndicator progress,
            @NotNull final ChangeListManagerGate addGate) throws VcsException {
        final ArcVcs vcs = ArcVcs.getInstance(myProject);
        if (LOG.isDebugEnabled()) LOG.debug("initial dirty scope: " + dirtyScope);
        if (LOG.isDebugEnabled()) LOG.debug("after adding nested vcs roots to dirt: " + dirtyScope);

        final Collection<VirtualFile> affected = dirtyScope.getAffectedContentRoots();
        // TODO:
        Collection<VirtualFile> roots = //GitUtil.gitRootsForPaths(affected);
                Collections.singletonList(
                        LocalFileSystem.getInstance().findFileByIoFile(new File("/home/elwood/proj/arcadia-arc/arcadia"))
                );

        List<FilePath> newDirtyPaths = new ArrayList<>();

        try {
            final MyNonChangedHolder holder = new MyNonChangedHolder(myProject, addGate,
                    myFileDocumentManager, myVcsManager);
            for (VirtualFile root : roots) {
                LOG.debug("checking root: " + root.getPath());
                ArcChangesCollector collector = new ArcNewChangesCollector(
                        myProject, myChangeListManager, myVcsManager, vcs, dirtyScope, root);
                final Collection<Change> changes = collector.getChanges();
                holder.changed(changes);
                for (Change file : changes) {
                    LOG.debug("process change: " + ChangesUtil.getFilePath(file).getPath());
                    builder.processChange(file, ArcVcs.getKey());

                    if (file.isMoved() || file.isRenamed()) {
                        FilePath beforePath = assertNotNull(ChangesUtil.getBeforePath(file));
                        FilePath afterPath = assertNotNull(ChangesUtil.getAfterPath(file));

                        if (dirtyScope.belongsTo(beforePath) != dirtyScope.belongsTo(afterPath)) {
                            newDirtyPaths.add(beforePath);
                            newDirtyPaths.add(afterPath);
                        }
                    }
                }
                for (VirtualFile f : collector.getUnversionedFiles()) {
                    builder.processUnversionedFile(f);
                    holder.unversioned(f);
                }
            }
            holder.feedBuilder(builder);

            VcsDirtyScopeManager.getInstance(myProject).filePathsDirty(newDirtyPaths, null);
        }
        catch (ProcessCanceledException pce) {
            if(pce.getCause() != null) throw new VcsException(pce.getCause().getMessage(), pce.getCause());
            else throw new VcsException("Cannot get changes from Git", pce);
        }
        catch (VcsException e) {
            LOG.info(e);
            throw e;
        }
    }

    private static class MyNonChangedHolder {
        private final Project myProject;
        private final Set<FilePath> myProcessedPaths;
        private final ChangeListManagerGate myAddGate;
        private final FileDocumentManager myFileDocumentManager;
        private final ProjectLevelVcsManager myVcsManager;

        private MyNonChangedHolder(final Project project,
                final ChangeListManagerGate addGate,
                FileDocumentManager fileDocumentManager, ProjectLevelVcsManager vcsManager) {
            myProject = project;
            myProcessedPaths = new HashSet<>();
            myAddGate = addGate;
            myFileDocumentManager = fileDocumentManager;
            myVcsManager = vcsManager;
        }

        public void changed(final Collection<Change> changes) {
            for (Change change : changes) {
                final FilePath beforePath = ChangesUtil.getBeforePath(change);
                if (beforePath != null) {
                    myProcessedPaths.add(beforePath);
                }
                final FilePath afterPath = ChangesUtil.getAfterPath(change);
                if (afterPath != null) {
                    myProcessedPaths.add(afterPath);
                }
            }
        }

        public void unversioned(final VirtualFile vf) {
            // NB: There was an exception that happened several times: vf == null.
            // Populating myUnversioned in the ChangeCollector makes nulls not possible in myUnversioned,
            // so proposing that the exception was fixed.
            // More detailed analysis will be needed in case the exception appears again. 2010-12-09.
            myProcessedPaths.add(VcsUtil.getFilePath(vf));
        }

        public void feedBuilder(final ChangelistBuilder builder) throws VcsException {
            final VcsKey gitKey = ArcVcs.getKey();

            //Map<VirtualFile, GitRevisionNumber> baseRevisions = new HashMap<>();

            for (Document document : myFileDocumentManager.getUnsavedDocuments()) {
                VirtualFile vf = myFileDocumentManager.getFile(document);
                if (vf == null || !vf.isValid()) continue;
                if (myAddGate.getStatus(vf) != null || !myFileDocumentManager.isFileModified(vf)) continue;

                FilePath filePath = VcsUtil.getFilePath(vf);
                if (myProcessedPaths.contains(filePath)) continue;

                //GitRepository repository = GitRepositoryManager.getInstance(myProject).getRepositoryForFile(vf);
                //if (repository == null) continue;
                //VirtualFile root = repository.getRoot();


                //GitRevisionNumber beforeRevisionNumber = baseRevisions.get(root);
                //if (beforeRevisionNumber == null) {
                //    beforeRevisionNumber = GitChangeUtils.resolveReference(myProject, root, "HEAD");
                //    baseRevisions.put(root, beforeRevisionNumber);
                //}

                //Change change = new Change(GitContentRevision.createRevision(vf, beforeRevisionNumber, myProject),
                //        GitContentRevision.createRevision(vf, null, myProject), FileStatus.MODIFIED);

                VirtualFile vcsRoot = LocalFileSystem.getInstance()
                        .findFileByIoFile(new File("/home/elwood/proj/arcadia-arc/arcadia"));
                String currentRev = new ArcRepository(vcsRoot).getCurrentRevision();
                ArcRevisionNumber beforeRevisionNumber = new ArcRevisionNumber(currentRev);

                Change change = new Change(ArcContentRevision.createRevision(vf, beforeRevisionNumber, myProject),
                        ArcContentRevision.createRevision(vf, null, myProject), FileStatus.MODIFIED);

                LOG.debug("process in-memory change " + change);
                builder.processChange(change, gitKey);
            }
        }
    }

    @Override
    public boolean isModifiedDocumentTrackingRequired() {
        return true;
    }

    @Override
    public void doCleanup(final List<VirtualFile> files) {
    }
}
