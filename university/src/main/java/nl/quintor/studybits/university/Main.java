package nl.quintor.studybits.university;

import nl.quintor.studybits.university.dto.EnrolmentProof;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.Console;
import java.io.File;
import java.nio.file.Paths;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws Exception {
        removeIndyClientDirectory();
        SpringApplication.run(Main.class, args);
    }

    private static void removeIndyClientDirectory() throws Exception {
        String homeDir = System.getProperty("user.home");
        File indyClientDir = Paths.get(homeDir, ".indy_client").toFile();
        FileUtils.deleteDirectory(indyClientDir);
    }
}