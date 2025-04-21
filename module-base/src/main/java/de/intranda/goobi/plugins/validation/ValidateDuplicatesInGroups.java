package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

/**
 * Find duplicate metadata and group values in <Groups> and return those into the error list
 * 
 * @author Paul Hankiewicz Lopez
 * @version 04.02.2025
 */
public class ValidateDuplicatesInGroups {

    /**
     * Validate the XML structure starting from the root element for duplicate metadata and group values in Groups
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<XMLError> validate(Element root) {
        List<XMLError> errors = new ArrayList<>();
        for (Element element : root.getChildren()) {
            checkForUnvalidGroups(errors, root, element);
        }
        return errors;
    }

    /**
     * Checks a specific XML element and its children for duplicates.
     *
     * @param errors A list of XMLError objects to collect validation errors.
     * @param element The XML element to be checked for duplicates.
     */
    private void checkForUnvalidGroups(List<XMLError> errors, Element root, Element element) {
        if (!"Group".equals(element.getName())) {
            return;
        }
        Set<String> valueSet = new HashSet<>();
        List<Element> childElements = element.getChildren();
        Element nameElement = element.getChild("Name");
        String nameText = (nameElement != null) ? nameElement.getText().trim() : "";

        for (Element childElement : childElements) {
            String childName = childElement.getName().trim();
            String childText = childElement.getText().trim();
            String childSignature = childName + ":" + childText;

            String lineNumber = childElement.getAttributeValue("lineNumber");
            String lineInfo = (lineNumber != null) ? lineNumber.trim() : "0";

            if (valueSet.contains(childSignature)) {

                // Check if the child element name is "metadata"
                if ("metadata".equals(childName)) {
                    findMetadataType(errors, root, childText, nameText, lineInfo);
                }
                // Check if the child element name is "group"
                else if ("group".equals(childName)) {
                    errors.add(
                            new XMLError("ERROR",
                                    Helper.getTranslation("ruleset_validation_duplicates_group_metadata", childText,
                                            nameText),lineInfo));
                }
            } else {
                // Add the signature to the set
                valueSet.add(childSignature);
            }
        }
    }

    /**
     * Find out if the duplicate value is either a person, corporate or a metadata.
     * 
     * @param errors
     * @param root
     * @param text
     * @param childElementText
     * @param nameElementText
     */
    private void findMetadataType(List<XMLError> errors, Element root, String childElementText, String nameElementText, String lineInfo) {

        for (Element element : root.getChildren()) {

            if (!"MetadataType".equals(element.getName()) || element.getChild("Name") == null) {
                continue;
            }

            Element nameChild = element.getChild("Name");

            // Check if the element is a person with the same name 
            if ("person".equals(element.getAttributeValue("type")) && childElementText.equals(nameChild.getText().trim())) {
                errors.add(new XMLError("ERROR",
                        Helper.getTranslation("ruleset_validation_duplicates_group_person", childElementText, nameElementText), lineInfo));
                return;
            }

            if ("corporate".equals(element.getAttributeValue("type")) && childElementText.equals(nameChild.getText().trim())) {
                errors.add(new XMLError("ERROR",
                        Helper.getTranslation("ruleset_validation_duplicates_group_corporate", childElementText, nameElementText), lineInfo));
                return;

            } else if (childElementText.equals(nameChild.getText().trim())) {
                errors.add(new XMLError("ERROR",
                        Helper.getTranslation("ruleset_validation_duplicates_group_metadata", childElementText, nameElementText), lineInfo));
                return;
            }

        }
    }
}