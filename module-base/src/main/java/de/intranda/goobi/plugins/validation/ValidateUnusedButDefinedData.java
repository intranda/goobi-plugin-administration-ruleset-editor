
package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

/**
 * Find metadata, groups, persons and corporates which are defined but never actually used and return those into the errors list
 * 
 * @author Paul Hankiewicz Lopez
 * @version 04.02.2025
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
        List<String> allAllowedchildtypeValues = new ArrayList<>();
        List<String> allUnusedAllowedchildtypeValues = new ArrayList<>();
        List<String> allFormatChildrenNameValues = new ArrayList<>();
        List<String> allDefinedFormatChildrenNameValues = new ArrayList<>();

        for (Element element : root.getChildren()) {
            // Check the children of this element and add them to the list
            getAllMetadataTypeNameValues(errors, element, allMetadataTypeNameValues, allAllowedchildtypeValues);
        }
        for (Element element : root.getChildren()) {
            // Go through all children of the element and search for unused values
            searchInDocstrctTypesForUnusedValues(errors, element, allMetadataTypeNameValues, allAllowedchildtypeValues);
        }
        for (Element element : root.getChildren()) {
            // Only go through the elements under the Formats Element and add the "Name" and "InternalName" values to the allFormatChildrenNameValues list
            if ("Formats".equals(element.getName())) {
                for (Element ChildElement : element.getChildren()) {
                    if (ChildElement.getName().equals("LIDO")) {
                        continue;
                    }
                    for (Element ChildElement2 : ChildElement.getChildren()) {
                        if (ChildElement2.getChild("Name") != null) {
                            allFormatChildrenNameValues.add(ChildElement2.getChild("Name").getText().trim());
                        } else if (ChildElement2.getChild("InternalName") != null) {
                            allFormatChildrenNameValues.add(ChildElement2.getChild("InternalName").getText().trim());
                        }
                    }
                }
            }
        }
        for (Element element : root.getChildren()) {
            if ("Formats".equals(element.getName())) {
                continue;
            }
            String name = element.getChild("Name").getText().trim();

            // If this Value is being used add it to the allUsedFormatChildrenNameValues list
            if (allFormatChildrenNameValues.contains(name) && !allDefinedFormatChildrenNameValues.contains(name)) {
                allDefinedFormatChildrenNameValues.add(name);
            }
        }
        // Go through all list elements in allFormatChildrenNameValues and if they are not 
        // in the allDefinedFormatChildrenNameValues list they are used but not defined
        for (String formatChildrenNameValue : allFormatChildrenNameValues) {
            if (allDefinedFormatChildrenNameValues.contains(formatChildrenNameValue)) {
                continue;
            } else {
                errors.add(new XMLError("ERROR",
                        Helper.getTranslation("ruleset_validation_used_but_undefined_value_for_export", formatChildrenNameValue)));
            }
        }

        // Add all unused values to the errors list
        for (String unusedValue : allMetadataTypeNameValues) {
            findMetadataType(errors, root, unusedValue);
        }
        for (String unusedValue : allAllowedchildtypeValues) {
            for (Element element : root.getChildren()) {
                // Check if the element is a DocStrctType and if the Name value is inside the allAllowedchildtypeValues. 
                // If so it will be added to allUnusedAllowedchildtypeValues and a message will be displayed 
                if ("DocStrctType".equals(element.getName()) && allAllowedchildtypeValues.contains(element.getChildText("Name").trim())
                        && !allUnusedAllowedchildtypeValues.contains(unusedValue)) {
                    allUnusedAllowedchildtypeValues.add(unusedValue);
                    errors.add(new XMLError("WARNING", Helper.getTranslation("ruleset_validation_unused_values_allowedchildtype", unusedValue)));
                }
            }
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
    private void getAllMetadataTypeNameValues(List<XMLError> errors, Element element, List<String> allMetadataTypeNameValues,
            List<String> allAllowedchildtypeValues) {
        if ("MetadataType".equals(element.getName()) || "Group".equals(element.getName())) {
            // Add the Name text to the list
            allMetadataTypeNameValues.add(element.getChild("Name").getText().trim());
        } else if ("DocStrctType".equals(element.getName())) {
            List<Element> allowedChildTypeElements = element.getChildren("Name");
            for (Element child : allowedChildTypeElements) {
                String topStructValue = element.getAttributeValue("topStruct");
                if (topStructValue != null && topStructValue.equals("true")) {
                    continue;
                }
                allAllowedchildtypeValues.add(child.getText().trim());
            }
        }
    }

    /**
     * Go through all DocStrctType and Group Elements and if a value of allMetadataTypeNameValues was found it will be removed
     * 
     * @param errors
     * @param element
     * @param allMetadataTypeNameValues
     */
    private void searchInDocstrctTypesForUnusedValues(List<XMLError> errors, Element element, List<String> allMetadataTypeNameValues,
            List<String> allAllowedchildtypeValues) {
        if (!"DocStrctType".equals(element.getName()) && !"Group".equals(element.getName())) {
            return;
        }

        List<Element> childElements = element.getChildren();

        for (Element childElement : childElements) {
            if (!"group".equals(childElement.getName()) && !"metadata".equals(childElement.getName())
                    && !"allowedchildtype".equals(childElement.getName())) {
                continue;
            }
            // If a value was found it is being used therefore it will be removed from the list
            if ("metadata".equals(childElement.getName())) {
                if (allMetadataTypeNameValues.contains(childElement.getText())) {
                    allMetadataTypeNameValues.remove(childElement.getText());
                }
            }

            if ("DocStrctType".equals(element.getName())) {
                if ("allowedchildtype".equals(childElement.getName())) {
                    if (allAllowedchildtypeValues.contains(childElement.getText())) {
                        allAllowedchildtypeValues.remove(childElement.getText());
                    }
                }
            }

            // Group used in same group
            if ("Group".equals(element.getName())) {
                if ("group".equals(childElement.getName()) && allMetadataTypeNameValues.contains(childElement.getText())) {
                    allMetadataTypeNameValues.remove(childElement.getText());
                }
            }
            // Groups in DocstrcyTypes
            else if (!childElement.getText().equals(element.getChildText("Name"))) {
                allMetadataTypeNameValues.remove(childElement.getText());
            }

        }
    }

    /**
     * Find out if the unused value is either a person, corporate or a metadata.
     * 
     * @param errors
     * @param rootjj
     * @param text
     * @param childElementText
     * @param nameElementText
     */
    private void findMetadataType(List<XMLError> errors, Element root, String text) {
        for (Element element : root.getChildren()) {
            if ("Group".equals(element.getName()) || "MetadataType".equals(element.getName())) {
                Element nameChild = element.getChild("Name");

                if (nameChild != null && text.equals(nameChild.getText())) {
                    String type = element.getAttributeValue("type");

                    if ("person".equals(type)) {
                        errors.add(new XMLError("WARNING", Helper.getTranslation("ruleset_validation_unused_values_person", text)));
                        continue;
                    }

                    if ("corporate".equals(type)) {
                        errors.add(new XMLError("WARNING", Helper.getTranslation("ruleset_validation_unused_values_corporate", text)));
                        continue;
                    }

                    if ("Group".equals(element.getName())) {
                        errors.add(new XMLError("WARNING", Helper.getTranslation("ruleset_validation_unused_values_groups", text)));
                        continue;
                    } else {
                        errors.add(new XMLError("WARNING", Helper.getTranslation("ruleset_validation_unused_values_metadata", text)));
                        continue;
                    }
                }
            }
        }
    }
}