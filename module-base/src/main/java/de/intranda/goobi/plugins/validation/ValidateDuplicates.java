package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

public class ValidateDuplicates {

    /**
     * Validate the XML structure starting from the root element for duplicate values in <DocstrctType>
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public static List<XMLError> validate(Element root) {
        List<XMLError> errors = new ArrayList<>();
        // Check all children of the root element       
        for (Element element : root.getChildren()) {
            // Check the children of this element       
            checkDocStructNodesForDuplicates(errors, element);
        }
        return errors;
    }

    /**
     * Checks a specific XML element and its children for duplicates.
     *
     * @param errors A list of XMLError objects to collect validation errors.
     * @param element The XML element to be checked for duplicates.
     */
    private static void checkDocStructNodesForDuplicates(List<XMLError> errors, Element element) {
        if (!"DocStrctType".equals(element.getName())) {
            return;
        }
        Set<String> valueSet = new HashSet<>();
        List<Element> childElements = element.getChildren();
        Element nameElement = element.getChild("Name");
        for (Element childElement : childElements) {
            String childSignature = childElement.getName() + ":" + childElement.getText();

            // Check if the childSignature already exists in the set
            if (valueSet.contains(childSignature)) {
                // Check if the child element name is "metadata"
                if ("metadata".equals(childElement.getName())) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_duplicates_metadata", childElement.getText(), nameElement.getText())));
                }
                // Check if the child element name is "group"
                else if ("group".equals(childElement.getName())) {
                    errors.add(
                            new XMLError("ERROR",
                                    Helper.getTranslation("ruleset_validation_duplicates_group", childElement.getText(),
                                            nameElement.getText())));
                }
                // Check if the child element name is "allowedchildtype"
                else if ("allowedchildtype".equals(childElement.getName())) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_duplicates_allowedchildtype", childElement.getText(), nameElement.getText())));
                }
            } else {
                // Add the signature to the set     
                valueSet.add(childSignature);
            }
        }

    }
}