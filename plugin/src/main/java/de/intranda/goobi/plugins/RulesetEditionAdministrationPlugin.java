package de.intranda.goobi.plugins;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.RulesetManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.commons.configuration.XMLConfiguration;

import org.goobi.beans.Ruleset;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IAdministrationPlugin;

@PluginImplementation
@Log4j2
public class RulesetEditionAdministrationPlugin implements IAdministrationPlugin {

    private final String RULESET_DIRECTORY;

    private final String BACKUP_DIRECTORY;

    private final int NUMBER_OF_BACKUP_FILES;

    @Getter
    private String title = "intranda_administration_ruleset_edition";

    private List<Ruleset> rulesets;

    /**
     * -1 means that no ruleset is selected
     */
    @Getter
    private int currentRulesetIndex = -1;

    /**
     * null means that no ruleset is selected
     */
    @Getter
    private Ruleset currentRuleset = null;

    /**
     * null means that no ruleset is selected
     */
    @Setter
    @Getter
    private String currentRulesetFileContent = null;

    @Getter
    private List<String> rulesetDates;

    /**
     * Constructor
     */
    public RulesetEditionAdministrationPlugin() {
        XMLConfiguration configuration = ConfigPlugins.getPluginConfig(this.title);
        this.RULESET_DIRECTORY = configuration.getString("rulesetDirectory", "/opt/digiverso/goobi/rulesets/");
        this.BACKUP_DIRECTORY = configuration.getString("rulesetBackupDirectory", "/opt/digiverso/goobi/rulesets/backup/");
        this.NUMBER_OF_BACKUP_FILES = configuration.getInt("numberOfBackupFiles", 10);
    }

    @Override
    public PluginType getType() {
        return PluginType.Administration;
    }

    @Override
    public String getGui() {
        return "/uii/plugin_administration_ruleset_edition.xhtml";
    }

    public String getCurrentEditorTitle() {
        if (this.currentRuleset != null) {
            return this.currentRuleset.getTitel() + " - " + this.currentRuleset.getDatei();
        } else {
            return "";
        }
    }

    public void initRulesetDates() {
        this.rulesetDates = new ArrayList<>();
        StorageProviderInterface storageProvider = StorageProvider.getInstance();
        for (int index = 0; index < this.rulesets.size(); index++) {
            try {
                long lastModified = storageProvider.getLastModifiedDate(Paths.get(this.RULESET_DIRECTORY + this.rulesets.get(index).getDatei()));
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                this.rulesetDates.add(formatter.format(lastModified));
            } catch (IOException ioException) {
                this.rulesetDates.add("[no date available]");
            }
        }
    }

    public List<Ruleset> getRulesets() {
        if (this.rulesets == null) {
            this.rulesets = RulesetManager.getAllRulesets();
            this.initRulesetDates();
        }
        if (this.rulesets != null) {
            return this.rulesets;
        } else {
            return new ArrayList<>();
        }
    }

    public void setCurrentRulesetIndex(int index) {
        this.setRuleset(index);
    }

    public String getCurrentRulesetFileName() {
        return this.RULESET_DIRECTORY + this.currentRuleset.getDatei();
    }

    public boolean isActiveRuleset(Ruleset ruleset) {
        return this.findRulesetIndex(ruleset) == this.currentRulesetIndex;
    }

    public void editRuleset(Ruleset ruleset) {
        int index = this.findRulesetIndex(ruleset);
        this.setRuleset(index);
    }

    public int findRulesetIndex(Ruleset ruleset) {
        for (int index = 0; index < this.rulesets.size(); index++) {
            if (ruleset == this.rulesets.get(index)) {
                return index;
            }
        }
        return -1;
    }

    public void save() {
        // Only create a backup if the new file content differs from the existing file content
        if (this.hasFileContentChanged()) {
            this.createBackupFile();
        }
        this.writeFile(this.getCurrentRulesetFileName(), this.currentRulesetFileContent);
        this.setRuleset(-1);
    }

    private boolean hasFileContentChanged() {
        List<String> oldLines = this.addLinebreakToEachLine(this.readFile(this.getCurrentRulesetFileName()));
        List<String> newLines = Arrays.asList(this.currentRulesetFileContent.split("\n"));
        return !this.isContentEqual(oldLines, newLines);
    }

    public boolean isContentEqual(List<String> first, List<String> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int index = 0; index < first.size(); index++) {
            String string1 = first.get(index);
            String string2 = second.get(index);
            int length1 = first.get(index).length();
            int length2 = second.get(index).length();
            // It is important to ignore the linebreaks because the first string has \n and the second one has \r
            if (!(string1.substring(0, length1 - 1).equals(string2.substring(0, length2 - 1)))) {
                return false;
            }
        }
        return true;
    }

    public void cancel() {
        this.setRuleset(-1);
    }

    private void setRuleset(int index) {
        if (index >= 0 && index < this.rulesets.size()) {
            this.currentRulesetIndex = index;
            this.currentRuleset = this.rulesets.get(index);
            List<String> lines = this.readFile(this.getCurrentRulesetFileName());
            this.currentRulesetFileContent = this.concatenateStrings(lines, "\n");
        } else {
            this.currentRulesetIndex = -1;
            this.currentRuleset = null;
            this.currentRulesetFileContent = null;
        }
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
    public void createBackupFile() {
        String fileName = this.currentRuleset.getDatei();
        StorageProviderInterface storage = StorageProvider.getInstance();
        try {
            // Delete oldest file when existing...
            String lastFileName = this.combineBackupFileName(fileName, this.NUMBER_OF_BACKUP_FILES);
            Path lastFile = Paths.get(lastFileName);
            if (storage.isFileExists(lastFile)) {
                storage.deleteFile(lastFile);
            }
            // Rename all other backup files...
            // This is the number of the file that should be renamed to the file with the higher number
            int backupId = this.NUMBER_OF_BACKUP_FILES - 1;
            while (backupId > 0) {
                String newerFileName = this.combineBackupFileName(fileName, backupId);
                String olderFileName = this.combineBackupFileName(fileName, backupId + 1);
                Path newerFile = Paths.get(newerFileName);
                if (storage.isFileExists(newerFile)) {
                    storage.renameTo(newerFile, olderFileName);
                }
                backupId--;
            }
            // Create backup file...
            List<String> lines = this.readFile(this.RULESET_DIRECTORY + fileName);
            String content = this.concatenateStrings(lines, "\n");
            this.writeFile(this.combineBackupFileName(fileName, 1), content);
            log.info("Wrote backup file: " + fileName);
        } catch (IOException ioException) {
            log.error(ioException);
        }
    }

    private String combineBackupFileName(String fileName, int backupId) {
        return this.BACKUP_DIRECTORY + fileName + "." + backupId;
    }

    public List<String> readFile(String fileName) {
        Path path = Paths.get(fileName);
        try {
            return Files.readAllLines(path);
        } catch (IOException ioException) {
            log.error("RulesetEditionAdministrationPlugin could not read file " + fileName);
            return new ArrayList<>();
        }
    }

    public void writeFile(String fileName, String content) {
        if (!Paths.get(this.BACKUP_DIRECTORY).toFile().exists()) {
            this.createDirectory(this.BACKUP_DIRECTORY);
        }
        if (!Paths.get(fileName).toFile().exists()) {
            this.createFile(fileName);
        }
        try {
            Files.write(Paths.get(fileName), content.getBytes());
        } catch (IOException ioException) {
            ioException.printStackTrace();
            log.error("RulesetEditionAdministrationPlugin could not write file " + fileName);
        }
    }

    public void createFile(String fileName) {
        Path path = Paths.get(fileName);
        try {
            StorageProvider.getInstance().createFile(path);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            log.error("RulesetEditionAdministrationPlugin could not create file " + fileName);
        }
    }

    public void createDirectory(String directoryName) {
        Path path = Paths.get(directoryName);
        try {
            StorageProvider.getInstance().createDirectories(path);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            log.error("RulesetEditionAdministrationPlugin could not create directory " + directoryName);
        }
    }

    public String concatenateStrings(List<String> strings, String connector) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int index = 0; index < strings.size(); index++) {
            stringBuffer.append(strings.get(index));
            if (index < strings.size() - 1) {
                stringBuffer.append(connector);
            }
        }
        return stringBuffer.toString();
    }

    public List<String> addLinebreakToEachLine(List<String> lines) {
        for (int index = 0; index < lines.size(); index++) {
            if (index < lines.size() - 1) {
                lines.set(index, lines.get(index) + "\n");
            }
        }
        return lines;
    }

}