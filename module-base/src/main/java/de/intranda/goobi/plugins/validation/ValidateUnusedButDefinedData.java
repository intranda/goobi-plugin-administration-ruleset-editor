
package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

/**
 * Find metadata, groups, persons and corporates which are defined but never actually used and return those into the errors list
 */
public class ValidateUnusedButDefinedData {

    /**
     * Validate the XML structure starting from the root element to find unused values
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<XMLError> validate(org.jdom2.Element root) {
        List<XMLError> errors = new ArrayList<>();
        List<String> allMetadataTypeNameValues = new ArrayList<>();
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
            findMetadataType(errors, root, unusedValue);
        }
        return errors;
    }

    /**
     * Add the elements name to allMetadataTypeNameValues
     * 
     * @param errors
     * @param element
     * @param allMetadataTypeNameValues
     */
    private void getAllMetadataTypeNameValues(List<XMLError> errors, Element element, List<String> allMetadataTypeNameValues) {
        if (!"MetadataType".equals(element.getName()) && !"Group".equals(element.getName())) {
            return;
        }
        // Add the Name text to the list
        allMetadataTypeNameValues.add(element.getChild("Name").getText().trim());
    }

    /**
     * Go through all DocStrctType and Group Elements and if a value of allMetadataTypeNameValues was found it will be removed
     * 
     * @param errors
     * @param element
     * @param allMetadataTypeNameValues
     */
    private void searchInDocstrctTypesForUnusedValues(List<XMLError> errors, Element element, List<String> allMetadataTypeNameValues) {
        if (!"DocStrctType".equals(element.getName()) && !"Group".equals(element.getName())) {
            return;
        }

        // Go trough all child Elements
        List<Element> childElements = element.getChildren();
        for (Element childElement : childElements) {
            // If a value was found it is being used therefore it will be removed from the list
            if ("metadata".equals(childElement.getName())) {
                if (allMetadataTypeNameValues.contains(childElement.getText())) {
                    allMetadataTypeNameValues.remove(childElement.getText());
                }
            }

            // Group used in same group
            if ("Group".equals(element.getName())) {
                if ("group".equals(childElement.getName())) {
                    if ("group".equals(childElement.getName()) && allMetadataTypeNameValues.contains(childElement.getText())) {
                        allMetadataTypeNameValues.remove(childElement.getText());
                    }
                }
            }
            // Groups in DocstrcyTypes
            else if (!childElement.getText().equals(element.getChildText("Name"))) {
                allMetadataTypeNameValues.remove(childElement.getText());
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
    private void findMetadataType(List<XMLError> errors, Element root, String text) {

        for (Element element : root.getChildren()) {
            if (!"group".equals(element.getName()) && !"metadata".equals(element.getName())) {
                continue;
            }
            Element nameChild = element.getChild("Name");

            if (nameChild != null && text.trim().equals(nameChild.getText().trim())) {
                String type = element.getAttributeValue("type");

                if ("person".equals(type)) {
                    errors.add(new XMLError("ERROR", Helper.getTranslation("ruleset_validation_unused_values_person", text)));
                    return;
                }

                if ("corporate".equals(type)) {
                    errors.add(new XMLError("ERROR", Helper.getTranslation("ruleset_validation_unused_values_corporate", text)));
                    return;
                }

                if ("Group".equals(element.getName())) {
                    errors.add(new XMLError("ERROR", Helper.getTranslation("ruleset_validation_unused_values_groups", text)));
                    return;
                } else {
                    errors.add(new XMLError("ERROR", Helper.getTranslation("ruleset_validation_unused_values_metadata", text)));
                    return;
                }
            }
        }
    }
}