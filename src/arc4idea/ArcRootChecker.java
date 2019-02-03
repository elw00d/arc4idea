package arc4idea;

import java.io.File;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.VcsRootChecker;
import org.jetbrains.annotations.NotNull;

public class ArcRootChecker extends VcsRootChecker {
    private static final Logger logger = Logger.getInstance("#ArcRootChecker");

    @NotNull
    @Override
    public VcsKey getSupportedVcs() {
        return ArcVcs.getKey();
    }

    @Override
    public boolean isRoot(@NotNull String path) {
        logger.info("isRoot(\"" + path + "\")");
        File vcsDir = new File(path, ".arc");
        return vcsDir.exists() && vcsDir.isDirectory();
    }

    @Override
    public boolean isVcsDir(@NotNull String dirName) {
        logger.info("isVcsDir(\"" + dirName + "\")");
        return ".arc".equalsIgnoreCase(dirName);
    }
}
