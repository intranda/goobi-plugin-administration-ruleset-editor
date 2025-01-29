package de.intranda.goobi.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.beans.Ruleset;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IAdministrationPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.intranda.goobi.plugins.xml.ReportErrorsErrorHandler;
import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.RulesetManager;
import lombok.Getter;
import lombok.Setter;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class RulesetEditorAdministrationPlugin implements IAdministrationPlugin {

    private static final long serialVersionUID = 2758642874624084899L;

    @Getter
    private String title = "intranda_administration_ruleset_editor";

    private List<Ruleset> rulesets;

    private List<Boolean> writable;

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

    @Getter
    private boolean validationError;

    @Getter
    private transient List<XMLError> validationErrors;

    @Getter
    private boolean showMore = false;

    /**
     * Constructor
     */
    public RulesetEditorAdministrationPlugin() {
        XMLConfiguration configuration = ConfigPlugins.getPluginConfig(this.title);
        RulesetFileUtils.init(configuration);
    }

    @Override
    public PluginType getType() {
        return PluginType.Administration;
    }

    @Override
    public String getGui() {
        return "/uii/plugin_administration_ruleset_editor.xhtml";
    }

    public String getCurrentEditorTitle() {
        if (this.currentRuleset != null) {
            return this.currentRuleset.getTitel() + " - " + this.currentRuleset.getDatei();
        } else {
            return "";
        }
    }

    public void toggleShowMore() {
        showMore = !showMore;
    }

    private void initRulesetDates() {
        this.rulesetDates = new ArrayList<>();
        StorageProviderInterface storageProvider = StorageProvider.getInstance();
        for (int index = 0; index < this.rulesets.size(); index++) {
            try {
                String pathName = RulesetFileUtils.getRulesetDirectory() + this.rulesets.get(index).getDatei();
                long lastModified = storageProvider.getLastModifiedDate(Paths.get(pathName));
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                this.rulesetDates.add(formatter.format(lastModified));
            } catch (IOException ioException) {
                ioException.printStackTrace();
                this.rulesetDates.add("[no date available]");
            }
        }
    }

    private void initWritePermissionFlags() {
        this.writable = new ArrayList<>();
        StorageProviderInterface storageProvider = StorageProvider.getInstance();
        for (int index = 0; index < this.rulesets.size(); index++) {
            String pathName = RulesetFileUtils.getRulesetDirectory() + this.rulesets.get(index).getDatei();
            this.writable.add(storageProvider.isWritable(Paths.get(pathName)));
        }
    }

    public boolean isCurrentRulesetWritable() {
        return this.isRulesetWritable(this.currentRuleset);
    }

    public boolean isRulesetWritable(Ruleset ruleset) {
        int index = 0;
        while (index < this.rulesets.size()) {
            if (this.rulesets.get(index).getDatei().equals(ruleset.getDatei())) {
                return this.writable.get(index);
            }
            index++;
        }
        return false;
    }

    public String getLastModifiedDateOfRuleset(Ruleset ruleset) {
        int index = this.findRulesetIndex(ruleset);
        return this.rulesetDates.get(index);
    }

    public void setCurrentRulesetFileContentBase64(String content) {
        if ("".equals(content)) {
            // content is not set up correctly, don't write into file!
            return;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decoded = decoder.decode(content);
        this.currentRulesetFileContent = new String(decoded, StandardCharsets.UTF_8);
    }

    public String getCurrentRulesetFileContentBase64() {
        // The return value must be empty to indicate that the text was not initialized until now.
        return "";
    }

    public List<Ruleset> getRulesets() {
        if (this.rulesets == null) {
            this.rulesets = RulesetManager.getAllRulesets();
            this.initRulesetDates();
            this.initWritePermissionFlags();
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
        validationErrors = null;
        int index = this.findRulesetIndex(ruleset);
        if (this.hasFileContentChanged()) {
            this.rulesetContentChanged = true;
            this.rulesetIndexAfterSaveOrIgnore = index;
            return;
        }
        this.setRuleset(index);
        if (!this.writable.get(index).booleanValue()) {
            String key = "plugin_administration_ruleset_editor_ruleset_not_writable_check_permissions";
            Helper.setMeldung("rulesetEditor", Helper.getTranslation(key), "");
        }
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

    public void save() throws ParserConfigurationException, SAXException, IOException {
        if (!checkXML()) {
            return;
        }
        // Only create a backup if the new file content differs from the existing file content
        if (this.hasFileContentChanged()) {
            RulesetFileUtils.createBackup(this.currentRuleset.getDatei());
            RulesetFileUtils.writeFile(this.getCurrentRulesetFileName(), this.currentRulesetFileContent);
        }

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

    public void validate() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        validationErrors = new ArrayList<>();
        checkRulesetXsd(this.currentRulesetFileContent);
        checkRulesetValid(this.currentRulesetFileContent);
    }

    private boolean hasFileContentChanged() {
        if (this.currentRuleset == null) {
            return false;
        }
        String fileContent = RulesetFileUtils.readFile(this.getCurrentRulesetFileName());
        fileContent = fileContent.replace("\r\n", "\n");
        fileContent = fileContent.replace("\r", "\n");
        String editorContent = this.currentRulesetFileContent;
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

    private boolean checkXML() throws ParserConfigurationException, SAXException, IOException {
        boolean ok = true;

        List<XMLError> errors = new ArrayList<>();
        errors.addAll(checkXMLWellformed(this.currentRulesetFileContent));

        if (!errors.isEmpty()) {
            for (XMLError error : errors) {
                Helper.setFehlerMeldung("rulesetEditor",
                        String.format("Line %d column %d: %s", error.getLine(), error.getColumn(), error.getMessage()), "");

            }
            if (errors.stream().anyMatch(e -> "ERROR".equals(e.getSeverity()) || "FATAL".equals(e.getSeverity()))) {
                this.validationError = true;
                //this needs to be done, so the modal won't appear repeatedly and ask the user if he wants to save.
                this.rulesetIndexAfterSaveOrIgnore = -1;
                this.rulesetContentChanged = false;
                Helper.setFehlerMeldung("rulesetEditor", "File was not saved, because the XML is not well-formed", "");
                ok = false;
            }
        } else {
            this.validationError = false;
        }
        return ok;
    }

    private List<XMLError> checkXMLWellformed(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        ReportErrorsErrorHandler eh = new ReportErrorsErrorHandler();
        builder.setErrorHandler(eh);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            builder.parse(bais);
        } catch (SAXParseException e) {
            //ignore this, because we collect the errors in the errorhandler and give them to the user.
        }

        return eh.getErrors();
    }

    private void checkRulesetValid(String xml) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        ReportErrorsErrorHandler eh = new ReportErrorsErrorHandler();
        builder.setErrorHandler(eh);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            Document document = builder.parse(bais);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            // ERROR: undefined but used
            String errorDescription = Helper.getTranslation("ruleset_validation_undefined_metadata_but_used");
            checkIssuesViaXPath(xpath, document, "//metadata[not(.=//MetadataType/Name)]", "ERROR", errorDescription);
            errorDescription = Helper.getTranslation("ruleset_validation_undefined_structure_data_but_used");
            checkIssuesViaXPath(xpath, document, "//allowedchildtype[not(.=//DocStrctType/Name)]", "ERROR", errorDescription);

            // ERROR: empty translations
            errorDescription = Helper.getTranslation("ruleset_validation_empty_translation");
            checkIssuesViaXPath(xpath, document, "//language[.='']/../Name", "ERROR", errorDescription);

            // ERROR: metadata used twice inside of structure element
            errorDescription = Helper.getTranslation("ruleset_validation_metadata_used_twice_inside_of_structure_element");
            checkIssuesViaXPath(xpath, document, "//DocStrctType/metadata[.=preceding-sibling::metadata]",
                    "ERROR", errorDescription);
            checkIssuesViaXPath(xpath, document, "//DocStrctType/metadata[.=preceding-sibling::metadata]/../Name", "ERROR",
                    errorDescription);

            // WARNING: defined twice
            errorDescription = Helper.getTranslation("ruleset_validation_metadata_defined_twice");
            checkIssuesViaXPath(xpath, document, "//MetadataType/Name[.=preceding::MetadataType/Name]", "WARNING", errorDescription);
            errorDescription = Helper.getTranslation("ruleset_validation_structure_data_defined_twice");
            checkIssuesViaXPath(xpath, document, "//DocStrctType/Name[.=preceding::DocStrctType/Name]", "WARNING", errorDescription);

            // WARNING: allowedchildtype defined twice
            errorDescription = Helper.getTranslation("ruleset_validation_allowedchildtype_defined_twice");
            checkIssuesViaXPath(xpath, document, "//DocStrctType/allowedchildtype[.=preceding-sibling::allowedchildtype]", "WARNING",
                    errorDescription);
            checkIssuesViaXPath(xpath, document, "//DocStrctType/allowedchildtype[.=preceding-sibling::allowedchildtype]/../Name", "WARNING",
                    errorDescription);

            // WARNING: undefined but used for export
            errorDescription = Helper.getTranslation("ruleset_validation_undefined_metadata_but_mapped_for_export");
            checkIssuesViaXPath(xpath, document, "//METS/Metadata/InternalName[not(.=//MetadataType/Name)]", "WARNING", errorDescription);
            errorDescription = Helper.getTranslation("ruleset_validation_undefined_structure_data_but_mapped_for_export");
            checkIssuesViaXPath(xpath, document, "//METS/DocStruct/InternalName[not(.=//DocStrctType/Name)]", "WARNING", errorDescription);

            // WARNING: topStructs used inside of allowedchildtype
            errorDescription = Helper.getTranslation("ruleset_validation_topstruct_used_as_allowed_child");
            checkIssuesViaXPath(xpath, document,
                    "//DocStrctType[not(@anchor=\"true\")]/allowedchildtype[.=//DocStrctType[@topStruct=\"true\"]/Name]",
                    "WARNING", errorDescription);
            checkIssuesViaXPath(xpath, document,
                    "//DocStrctType[not(@anchor=\"true\")][allowedchildtype=//DocStrctType[@topStruct=\"true\"]/Name]/Name",
                    "WARNING", errorDescription);

            // INFO: not mapped for export
            errorDescription = Helper.getTranslation("ruleset_validation_structure_data_not_mapped_for_export");
            checkIssuesViaXPath(xpath, document, "//DocStrctType/Name[not(.=//METS/DocStruct/InternalName)]",
                    "INFO", errorDescription);
            errorDescription = Helper.getTranslation("ruleset_validation_metadata_not_mapped_for_export");
            checkIssuesViaXPath(xpath, document, "//MetadataType/Name[not(.=//METS/Metadata/InternalName)]",
                    "INFO", errorDescription);

        } catch (SAXParseException e) {
            //ignore this, because we collect the errors in the errorhandler and give them to the user.
        }
    }

    private void checkIssuesViaXPath(XPath xpath, Document document, String expression, String severity, String errorType)
            throws XPathExpressionException {
        XPathExpression xpathExpression = xpath.compile(expression);
        NodeList nodeList = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            validationErrors.add(new XMLError(0, 0, severity, node.getTextContent() + " - " + errorType));
        }
    }

    private void checkRulesetXsd(String xml) {
        String xsdUrl = "https://github.com/intranda/ugh/raw/master/ugh/ruleset_schema.xsd";

        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(new URL(xsdUrl).openStream()));
            Validator validator = schema.newValidator();
            StreamSource source = new StreamSource(new StringReader(xml));
            validator.validate(source);
        } catch (Exception e) {
            if (e instanceof SAXParseException se) {
                validationErrors.add(new XMLError(se.getLineNumber(), 0, "ERROR", e.getMessage()));
            } else {
                validationErrors.add(new XMLError(0, 0, "ERROR", e.getMessage()));
            }

        }
    }

}