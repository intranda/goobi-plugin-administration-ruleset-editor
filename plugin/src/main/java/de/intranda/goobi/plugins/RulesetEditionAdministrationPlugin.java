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
import java.util.Date;
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
        this.createBackupFile();
        this.writeFile(this.getCurrentRulesetFileName(), this.currentRulesetFileContent);
        this.setRuleset(-1);
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

    public void createBackupFile() {
        List<String> lines = this.readFile(this.getCurrentRulesetFileName());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String fileName = this.BACKUP_DIRECTORY + "backup_" + formatter.format(new Date()) + "_" + this.currentRuleset.getDatei();
        String content = this.concatenateStrings(lines, "\n");
        this.writeFile(fileName, content);
        log.error("Wrote backup file: " + fileName);
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

}