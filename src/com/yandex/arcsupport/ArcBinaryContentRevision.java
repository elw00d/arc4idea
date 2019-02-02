package com.yandex.arcsupport;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.BinaryContentRevision;
import org.jetbrains.annotations.NotNull;

public class ArcBinaryContentRevision extends ArcContentRevision implements BinaryContentRevision {
    public ArcBinaryContentRevision(@NotNull FilePath file, @NotNull ArcRevisionNumber revision,
            @NotNull Project project)
    {
        super(file, revision, project, null);
    }

    @Override
    public byte[] getBinaryContent() throws VcsException {
        return getContentAsBytes();
    }
}
