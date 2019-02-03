package arc4idea;

import java.util.Date;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;

public class ArcRevisionNumber implements VcsRevisionNumber {
    private final String rev;
    private final Date timestamp;

    public ArcRevisionNumber(String rev) {
        this.rev = rev;
        this.timestamp = new Date();
    }

    public ArcRevisionNumber(String rev, Date timestamp) {
        this.rev = rev;
        this.timestamp = timestamp;
    }

    @Override
    public String asString() {
        return rev;
    }

    @Override
    public int compareTo(VcsRevisionNumber o) {
        return timestamp.compareTo(((ArcRevisionNumber) o).timestamp);
    }
}
