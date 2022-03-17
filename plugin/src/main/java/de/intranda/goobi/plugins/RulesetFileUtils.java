package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.goobi.io.BackupFileManager;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class RulesetFileUtils {

    @Getter
    private static String rulesetDirectory;

    @Getter
    private static String backupDirectory;

    @Getter
    private static int numberOfBackupFiles;

    private static Charset standardCharset;

    public static void init(XMLConfiguration configuration) {
        RulesetFileUtils.rulesetDirectory = configuration.getString("rulesetDirectory", "/opt/digiverso/goobi/rulesets/");
        RulesetFileUtils.backupDirectory = configuration.getString("rulesetBackupDirectory", "/opt/digiverso/goobi/rulesets/backup/");
        RulesetFileUtils.numberOfBackupFiles = configuration.getInt("numberOfBackupFiles", 10);
        RulesetFileUtils.standardCharset = Charset.forName("UTF-8");
    }

    public static void createBackup(String fileName) throws IOException {
        String path = RulesetFileUtils.getRulesetDirectory();
        String backupPath = RulesetFileUtils.getBackupDirectory();
        int number = RulesetFileUtils.getNumberOfBackupFiles();
        BackupFileManager.createBackup(path, backupPath, fileName, number, true);
    }

    public static String readFile(String fileName) {
        try {
            Charset charset = RulesetFileUtils.standardCharset;
            return FileUtils.readFileToString(new File(fileName), charset);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            String message = "RulesetEditorAdministrationPlugin could not read file " + fileName;
            log.error(message);
            Helper.setFehlerMeldung(message);
            return "";
        }
    }

    public static void writeFile(String fileName, String content) {
        if (!Paths.get(RulesetFileUtils.backupDirectory).toFile().exists()) {
            RulesetFileUtils.createDirectory(RulesetFileUtils.backupDirectory);
        }
        if (!Paths.get(fileName).toFile().exists()) {
            RulesetFileUtils.createFile(fileName);
        }
        try {
            Charset charset = RulesetFileUtils.standardCharset;
            FileUtils.write(new File(fileName), content, charset);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            String message = "RulesetEditorAdministrationPlugin could not write file " + fileName;
            log.error(message);
            Helper.setFehlerMeldung(message);
        }
    }

    public static void createFile(String fileName) {
        Path path = Paths.get(fileName);
        try {
            StorageProvider.getInstance().createFile(path);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            log.error("RulesetEditorAdministrationPlugin could not create file " + fileName);
        }
    }

    public static void createDirectory(String directoryName) {
        Path path = Paths.get(directoryName);
        try {
            StorageProvider.getInstance().createDirectories(path);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            log.error("RulesetEditorAdministrationPlugin could not create directory " + directoryName);
        }
    }

}