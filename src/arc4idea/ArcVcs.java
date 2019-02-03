package arc4idea;

import arc4idea.status.ArcChangeProvider;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArcVcs extends AbstractVcs<CommittedChangeList> {
    public static final String NAME = "Arc";
    private static final VcsKey ourKey = createKey(NAME);

    private final ChangeProvider changeProvider;
    private final DiffProvider diffProvider = new ArcDiffProvider(myProject);

    public ArcVcs(@NotNull Project project) {
        super(project, NAME);
        changeProvider = project.isDefault() ? null : ServiceManager.getService(project, ArcChangeProvider.class);
    }

    @NotNull
    public static ArcVcs getInstance(@NotNull Project project) {
        return ObjectUtils.notNull((ArcVcs) ProjectLevelVcsManager.getInstance(project).findVcsByName(NAME));
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Arc";
    }

    @Override
    public Configurable getConfigurable() {
        return null;
    }

    public static VcsKey getKey() {
        return ourKey;
    }

    @Nullable
    @Override
    public ChangeProvider getChangeProvider() {
        return changeProvider;
    }

    @Nullable
    @Override
    public DiffProvider getDiffProvider() {
        return diffProvider;
    }
}
