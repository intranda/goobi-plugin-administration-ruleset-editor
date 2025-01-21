package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

public class ValidateUnusedButDefinedData {

    public static List<XMLError> validate(org.jdom2.Element root) {
        List<XMLError> errors = new ArrayList<>();
        List<String> allMetadataTypeNameValues = new ArrayList<>();
        // Check all children of the root element       
        for (Element element : root.getChildren()) {
            // Check the children of this element and add them to the list
            getAllMetadataTypeNameValues(errors, element, allMetadataTypeNameValues);
        }
        for (Element element : root.getChildren()) {
            // Go through all children of the element and search for unused values
            searchInDocstrctTypesForUnusedValues(errors, element, allMetadataTypeNameValues);
        }
        // Add all unused values to the errors list
        for (String unusedValue : allMetadataTypeNameValues) {
            errors.add(
                    new XMLError("ERROR", Helper.getTranslation("ruleset_validation_unused_values", unusedValue)));

        }
        return errors;
    }

    private static void getAllMetadataTypeNameValues(List<XMLError> errors, Element element, List<String> allMetadataTypeNameValues) {
        if (!"MetadataType".equals(element.getName())) {
            return;
        }
        // Add the Name text to the list
        allMetadataTypeNameValues.add(element.getChild("Name").getText());
    }

    private static void searchInDocstrctTypesForUnusedValues(List<XMLError> errors, Element element, List<String> allMetadataTypeNameValues) {
        if (!"DocStrctType".equals(element.getName())) {
            return;
        }
        // Go trough all child Elements
        List<Element> childElements = element.getChildren();
        for (Element childElement : childElements) {
            // If a value was found it is being used therefore it will be removed from the list
            if (allMetadataTypeNameValues.contains(childElement.getText())) {
                allMetadataTypeNameValues.remove(childElement.getText());
            }
        }
    }
}