package de.intranda.goobi.plugins;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.RulesetManager;

import java.io.IOException;
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

    public Ruleset getCurrentRuleset() {
        return this.currentRuleset;
    }

    public void editRuleset(int index) {
        this.setRuleset(index);
        log.error("Edit: " + this.currentRulesetIndex);
    }

    public void save() {
        this.setRuleset(-1);
        log.error("Save");
    }

    public void cancel() {
        this.setRuleset(-1);
        log.error("Cancel");
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

}