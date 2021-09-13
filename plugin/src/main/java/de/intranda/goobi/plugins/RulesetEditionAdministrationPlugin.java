package de.intranda.goobi.plugins;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.beans.Ruleset;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IAdministrationPlugin;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.RulesetManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Log4j2
@PluginImplementation
public class RulesetEditionAdministrationPlugin implements IAdministrationPlugin {

    @Getter
    private String title = "intranda_administration_ruleset_edition";

    private List<Ruleset> rulesets;

    /**
     * -1 means that no ruleset is selected
     */
    @Getter
    private int currentRulesetIndex = -1;

    private int rulesetIndexAfterSaveOrIgnore = -1;

    @Getter
    private boolean rulesetContentChanged = false;

    /**
     * null means that no ruleset is selected
     */
    @Getter
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
        XMLConfiguration configuration = ConfigPlugins.getPluginConfig(this.title);
        RulesetFileUtils.init(configuration);
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
                long lastModified =
                        storageProvider.getLastModifiedDate(Paths.get(RulesetFileUtils.getRulesetDirectory() + this.rulesets.get(index).getDatei()));
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
        return RulesetFileUtils.getRulesetDirectory() + this.currentRuleset.getDatei();
    }

    public boolean isActiveRuleset(Ruleset ruleset) {
        return this.findRulesetIndex(ruleset) == this.currentRulesetIndex;
    }

    public void editRuleset(Ruleset ruleset) {
        int index = this.findRulesetIndex(ruleset);
        if (this.hasFileContentChanged()) {
            this.rulesetContentChanged = true;
            this.rulesetIndexAfterSaveOrIgnore = index;
            return;
        }
        this.setRuleset(index);
    }

    public void editRulesetIgnore() {
        this.rulesetContentChanged = false;
        this.setRuleset(this.rulesetIndexAfterSaveOrIgnore);
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
            RulesetFileUtils.createBackupFile(this.currentRuleset.getDatei());
        }
        RulesetFileUtils.writeFile(this.getCurrentRulesetFileName(), this.currentRulesetFileContent);
        // Uncomment this when the file should be closed after saving
        // this.setRuleset(-1);
        Helper.setMeldung("rulesetEditor", Helper.getTranslation("savedRulesetFileSuccessfully"), "");
        // Switch to an other file (rulesetIndexAfterSaveOrIgnore) when "Save" was clicked
        // because the file should be changed and an other file is already selected
        if (this.rulesetIndexAfterSaveOrIgnore != -1) {
            if (this.rulesetIndexAfterSaveOrIgnore != this.currentRulesetIndex) {
                this.setRuleset(this.rulesetIndexAfterSaveOrIgnore);
            }
            this.rulesetIndexAfterSaveOrIgnore = -1;
        }
        this.rulesetContentChanged = false;
    }

    public void saveAndChangeRuleset() {
        this.save();
    }

    private boolean hasFileContentChanged() {
        if (this.currentRuleset == null) {
            return false;
        }
        String fileContent = RulesetFileUtils.readFile(this.getCurrentRulesetFileName());
        String editorContent = this.currentRulesetFileContent.replace("\n", "\r\n");
        return !fileContent.equals(editorContent);
    }

    public void cancel() {
        this.setRuleset(-1);
    }

    private void setRuleset(int index) {
        // Change the (saved or unchanged) file
        if (index >= 0 && index < this.rulesets.size()) {
            this.currentRulesetIndex = index;
            this.currentRuleset = this.rulesets.get(index);
            this.currentRulesetFileContent = RulesetFileUtils.readFile(this.getCurrentRulesetFileName());
        } else {
            // Close the file
            this.currentRulesetIndex = -1;
            this.currentRuleset = null;
            this.currentRulesetFileContent = null;
        }
    }

}