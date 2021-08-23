package de.intranda.goobi.plugins;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.RulesetManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.goobi.beans.Ruleset;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IAdministrationPlugin;

@PluginImplementation
@Log4j2
public class RulesetEditionAdministrationPlugin implements IAdministrationPlugin {

    @Getter
    private String title = "intranda_administration_ruleset_edition";

    private static final String RULESET_DIRECTORY = "/opt/digiverso/goobi/rulesets/";

    private static final String BACKUP_DIRECTORY = RULESET_DIRECTORY + "backup/";

    @Getter
    private String value;

    private List<Ruleset> rulesets;

    /**
     * -1 means that no ruleset is selected
     */
    private int currentRulesetIndex = -1;

    /**
     * null means that no ruleset is selected
     */
    private Ruleset currentRuleset = null;

    /**
     * null means that no ruleset is selected
     */
    @Getter
    @Setter
    private String currentRulesetFileContent = null;

    @Getter
    private List<String> rulesetDates;

    /**
     * Constructor
     */
    public RulesetEditionAdministrationPlugin() {
        log.info("Sample admnistration plugin started");
        value = ConfigPlugins.getPluginConfig(title).getString("value", "default value");
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
                long lastModified = storageProvider.getLastModifiedDate(Paths.get(RULESET_DIRECTORY + this.rulesets.get(index).getDatei()));
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
        log.error("Set ruleset index");
        this.setRuleset(index);
    }

    public Ruleset getCurrentRuleset() {
        return this.currentRuleset;
    }

    public void setCurrentRulesetFileContent(String content) {
        this.currentRulesetFileContent = content;
    }

    public void editRuleset(int index) {
        this.setRuleset(index);
        List<String> lines = this.readFile(RULESET_DIRECTORY + this.currentRuleset.getDatei());
        this.currentRulesetFileContent = this.concatenateStrings(lines, "\n");
    }

    public void save() {
        File file = new File(RULESET_DIRECTORY + this.currentRuleset.getDatei());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(this.currentRulesetFileContent);
            writer.close();
        } catch (IOException ioException) {
            log.error("Could not write file " + file.getAbsoluteFile().getAbsolutePath());
        }
        this.setRuleset(-1);
    }

    public void cancel() {
        this.setRuleset(-1);
    }

    private void setRuleset(int index) {
        if (index >= 0 && index < this.rulesets.size()) {
            this.currentRulesetIndex = index;
            this.currentRuleset = this.rulesets.get(index);
            this.currentRulesetFileContent = this.currentRuleset.getDatei();
        } else {
            this.currentRulesetIndex = -1;
            this.currentRuleset = null;
            this.currentRulesetFileContent = null;
        }
    }

    public List<String> readFile(String fileName) {
        try {
            return Files.readAllLines(Paths.get(fileName));
        } catch (IOException ioException) {
            return new ArrayList<>();
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