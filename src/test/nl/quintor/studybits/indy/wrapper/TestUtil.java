package nl.quintor.studybits.indy.wrapper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;

public class TestUtil {
    public static void removeIndyClientDirectory() throws Exception {
        String homeDir = System.getProperty("user.home");
        File indyClientDir = Paths.get(homeDir, ".indy_client")
                .toFile();
        FileUtils.deleteDirectory(indyClientDir);
    }
}
