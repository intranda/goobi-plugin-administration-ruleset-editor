package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import lombok.extern.log4j.Log4j2;
import lombok.Getter;

@Log4j2
public abstract class RulesetFileUtils {

    @Getter
    private static String rulesetDirectory;

    private static String backupDirectory;

    private static int numberOfBackupFiles;

    private static Charset standardCharset;

    public static void init(XMLConfiguration configuration) {
        RulesetFileUtils.rulesetDirectory = configuration.getString("rulesetDirectory", "/opt/digiverso/goobi/rulesets/");
        RulesetFileUtils.backupDirectory = configuration.getString("rulesetBackupDirectory", "/opt/digiverso/goobi/rulesets/backup/");
        RulesetFileUtils.numberOfBackupFiles = configuration.getInt("numberOfBackupFiles", 10);
        RulesetFileUtils.standardCharset = Charset.forName("UTF-8");
    }

    /**
     * Rotates the backup files (older files get a higher number) and creates a backup file in "fileName.xml.1".
     * The oldest file (e.g. "fileName.xml.10") will be removed
     *
     * How the algorithm works (e.g. this.NUMBER_OF_BACKUP_FILES == 10):
     *
     * delete(backup/fileName.xml.10)
     * rename(backup/fileName.xml.9, backup/fileName.xml.10)
     * rename(backup/fileName.xml.8, backup/fileName.xml.9)
     *...
     * rename(backup/fileName.xml.2, backup/fileName.xml.3)
     * rename(backup/fileName.xml.1, backup/fileName.xml.2)
     * copy(fileName.xml, backup/fileName.xml.1)
     */
    public static void createBackupFile(String fileName) {
        StorageProviderInterface storage = StorageProvider.getInstance();
        try {
            // Delete oldest file if it exists...
            String lastFileName = RulesetFileUtils.createBackupFileNameWithoutTimestamp(fileName, RulesetFileUtils.numberOfBackupFiles);
            lastFileName = RulesetFileUtils.backupDirectory + RulesetFileUtils.findBackupFileNameByEnding(lastFileName);
            if (lastFileName != null) {
                Path lastFile = Paths.get(lastFileName);
                if (storage.isFileExists(lastFile)) {
                    storage.deleteFile(lastFile);
                }
            }
            // Rename all other backup files...
            // This is the number of the file that should be renamed to the file with the higher number
            int backupId = RulesetFileUtils.numberOfBackupFiles - 1;
            while (backupId > 0) {
                String newerFileName = RulesetFileUtils.createBackupFileNameWithoutTimestamp(fileName, backupId);
                newerFileName = RulesetFileUtils.findBackupFileNameByEnding(newerFileName);
                String newerFileNameWithPath = RulesetFileUtils.backupDirectory + newerFileName;
                String olderFileName = RulesetFileUtils.createBackupFileNameWithoutTimestamp(fileName, backupId + 1);
                if (newerFileName != null) {
                    String timestamp = RulesetFileUtils.extractTimestampFromFileName(newerFileName);
                    olderFileName = timestamp + "_" + olderFileName;
                    Path newerFile = Paths.get(newerFileNameWithPath);
                    if (storage.isFileExists(newerFile)) {
                        storage.renameTo(newerFile, olderFileName);
                    }
                }
                backupId--;
            }
            // Create backup file...
            String content = RulesetFileUtils.readFile(RulesetFileUtils.rulesetDirectory + fileName);
            RulesetFileUtils.writeFile(RulesetFileUtils.createBackupFileNameWithTimestamp(fileName, 1), content);
            log.info("Wrote backup file: " + fileName);
        } catch (IOException ioException) {
            log.error(ioException);
        }
    }

    private static String extractTimestampFromFileName(String fileName) {
        // The date format must be "yyyy_MM_dd_HH_mm_ss"
        return fileName.substring(0, 19);
    }

    private static String createBackupFileNameWithoutTimestamp(String fileName, int backupId) {
        return fileName + "." + backupId;
    }

    private static String createBackupFileNameWithTimestamp(String fileName, int backupId) {
        String name = RulesetFileUtils.createBackupFileNameWithoutTimestamp(fileName, backupId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String timestamp = formatter.format(new Date());
        return RulesetFileUtils.backupDirectory + timestamp + "_" + name;
    }

    private static String findBackupFileNameByEnding(String ending) {
        StorageProviderInterface storage = StorageProvider.getInstance();
        List<String> fileNames = storage.list(RulesetFileUtils.backupDirectory);
        for (int index = 0; index < fileNames.size(); index++) {
            if (fileNames.get(index).endsWith(ending)) {
                return fileNames.get(index);
            }
        }
        return null;
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