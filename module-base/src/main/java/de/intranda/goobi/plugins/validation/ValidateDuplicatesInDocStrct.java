package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

/**
 * Find duplicate metadata, group and allowedChildType values in <DocstrctType> Element and return those into the errors list
 * 
 * @author Paul Hankiewicz Lopez
 * @version 28.01.2025
 */
public class ValidateDuplicatesInDocStrct {

    /**
     * Validate the XML structure starting from the root element for duplicate metadata, group and allowedChildType values in <DocstrctType>
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<XMLError> validate(Element root) {
        List<XMLError> errors = new ArrayList<>();
        for (Element element : root.getChildren()) {
            checkDocStructNodesForDuplicates(errors, root, element);
        }
        return errors;
    }

    /**
     * Checks a specific XML element and its children for duplicates.
     *
     * @param errors A list of XMLError objects to collect validation errors.
     * @param element The XML element to be checked for duplicates.
     */
    private void checkDocStructNodesForDuplicates(List<XMLError> errors, Element root, Element element) {
        if (!"DocStrctType".equals(element.getName())) {
            return;
        }
        Set<String> valueSet = new HashSet<>();
        List<Element> childElements = element.getChildren();
        Element nameElement = element.getChild("Name");
        String nameText = (nameElement != null) ? nameElement.getText().trim() : "";

        for (Element childElement : childElements) {
            String childName = childElement.getName();
            String childText = childElement.getText().trim();
            String childSignature = childName + ":" + childText;

            // Check if the childSignature already exists in the set
            if (valueSet.contains(childSignature)) {
                // Check if the child element name is "metadata"
                if ("metadata".equals(childName)) {
                    // Check if the found duplicate value is a person, corporate or metadata
                    findMetadataType(errors, root, childText, nameText);

                }
                // Check if the child element name is "group"
                else if ("group".equals(childName)) {
                    errors.add(
                            new XMLError("ERROR",
                                    Helper.getTranslation("ruleset_validation_duplicates_group_group", childText, nameText)));
                }
                // Check if the child element name is "allowedchildtype"
                else if ("allowedchildtype".equals(childName)) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_duplicates_group_allowedchildtype", childText, nameText)));
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
    private void findMetadataType(List<XMLError> errors, Element root, String childElementText, String nameElementText) {
        for (Element element : root.getChildren()) {
            if (!"MetadataType".equals(element.getName()) && !"group".equals(element.getName()) && !"metadata".equals(element.getName())) {
                continue;
            }

            String nameValue = element.getChildText("Name");
            if (nameValue != null && childElementText.equals(nameValue.trim())) {
                String typeValue = element.getAttributeValue("type");

                if ("person".equals(typeValue)) {
                    errors.add(
                            new XMLError("ERROR", Helper.getTranslation("ruleset_validation_duplicates_person", childElementText, nameElementText)));
                    return;
                }
                if ("corporate".equals(typeValue)) {
                    errors.add(
                            new XMLError("ERROR",
                                    Helper.getTranslation("ruleset_validation_duplicates_corporate", childElementText, nameElementText)));
                    return;

                } else {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_duplicates_metadata", childElementText, nameElementText)));
                    return;
                }
            }
        }
    }

}