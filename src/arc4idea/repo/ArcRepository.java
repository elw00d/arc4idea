package arc4idea.repo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

import arc4idea.ArcRevisionNumber;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ArcRepository {
    private final VirtualFile vcsRoot;

    public ArcRepository(VirtualFile vcsRoot) {
        this.vcsRoot = vcsRoot;
    }

    public String getCurrentRevision() {
        // arc log --oneline -n 1 --no-walk
        Runtime rt = Runtime.getRuntime();

        String[] commands = {"/home/elwood/arcadia/ya", "tool", "arc", "log", "--oneline", "-n", "1"};
        Process proc;
        try {
            proc = rt.exec(commands, new String[]{}, new File(vcsRoot.getPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            try (BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                String s = stdInput.readLine();
                return s.split(" ")[0];
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] getFileContent(Project project, VirtualFile root, ArcRevisionNumber revision, String relativePath) {
        // TODO : deal with binary files
        Runtime rt = Runtime.getRuntime();

        String[] commands = {"/home/elwood/arcadia/ya", "tool", "arc", "show", revision.asString(), relativePath};
        Process proc;
        try {
            proc = rt.exec(commands, new String[]{}, new File(vcsRoot.getPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return readAll(proc.getInputStream()).getBytes();
    }

    private String readAll(InputStream inputStream) {
        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(inputStream))) {
            // TODO : optimize, correctly read last line
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = stdInput.readLine()) != null) {
                sb.append(s).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getStatus() {
        Runtime rt = Runtime.getRuntime();

        String[] commands = {"/home/elwood/arcadia/ya", "tool", "arc", "status", "-s"};
        Process proc;
        try {
            proc = rt.exec(commands, new String[]{}, new File(vcsRoot.getPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return readAll(proc.getInputStream());
    }
}
