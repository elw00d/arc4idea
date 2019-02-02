package com.yandex.arcsupport;

import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.VcsRootChecker;
import org.jetbrains.annotations.NotNull;

public class ArcRootChecker extends VcsRootChecker {
    @NotNull
    @Override
    public VcsKey getSupportedVcs() {
        return ArcVcs.getKey();
    }

    @Override
    public boolean isRoot(@NotNull String path) {
        // TODO :
        return true;
    }

    @Override
    public boolean isVcsDir(@NotNull String dirName) {
        return ".arc".equalsIgnoreCase(dirName);
    }
}
