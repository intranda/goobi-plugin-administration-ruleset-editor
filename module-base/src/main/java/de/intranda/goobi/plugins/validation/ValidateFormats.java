
package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

public class ValidateFormats {
    public List<XMLError> validate(org.jdom2.Element root) {
        List<XMLError> errors = new ArrayList<>();

        for (Element element : root.getChildren()) {
            if ("Formats".equals(element.getName())) {
                for (Element ChildElement : element.getChildren()) {
                    System.out.println(ChildElement.getName());

                    List<String> formatChildrenNameValues = new ArrayList<>();
                    if (ChildElement.getName().equals("PicaPlus")) {

                    } else if (ChildElement.getName().equals("Marc")) {

                        validateMarc(root, ChildElement, formatChildrenNameValues);
                        throwErrors(errors, "Marc", formatChildrenNameValues);
                        System.out.println(formatChildrenNameValues);
                        formatChildrenNameValues.clear();

                    } else if (ChildElement.getName().equals("METS")) {

                        validateMETS(root, ChildElement, formatChildrenNameValues);
                        throwErrors(errors, "METS", formatChildrenNameValues);
                        formatChildrenNameValues.clear();

                    } else if (ChildElement.getName().equals("LIDO")) {

                    }
                }
            }
        }
        return errors;
    }

    private void validateMarc(Element root, Element childElement, List<String> formatChildrenNameValues) {
        for (Element ChildChildElement : childElement.getChildren()) {
            // Only do this, if the ChildChild element is named Metadata, Group or DocStruct
            if (ChildChildElement.getName().equals("Metadata") || ChildChildElement.getName().equals("Group")
                    || ChildChildElement.getName().equals("DocStruct") || ChildChildElement.getName().equals("Person")) {

                // Grab the InternalName Value of the ChildChildElement
                if (ChildChildElement.getChild("Name") != null) {
                    String childChildName = ChildChildElement.getName() + ":" + ChildChildElement.getChild("Name").getText().trim();
                    formatChildrenNameValues.add(childChildName);
                    for (Element element2 : root.getChildren()) {
                        if (childChildName == null || childChildName.isEmpty() || element2.getChild("Name") == null) {
                            continue;
                        }
                        // If the "name" equals MetadataType then 
                        if (("MetadataType".equals(element2.getName())
                                && element2.getChildText("Name").equals(childChildName.substring(childChildName.indexOf(":") + 1)))
                                && formatChildrenNameValues.contains(childChildName)) {
                            if (childChildName.substring(0, childChildName.indexOf(":")).equals(("Person"))) {
                                if (element2.getAttributeValue("type") != null && "person".equals(element2.getAttributeValue("type"))) {
                                    formatChildrenNameValues.remove(childChildName);
                                }

                            } else {
                                formatChildrenNameValues.remove(childChildName);
                            }
                            continue;
                        } else if ("Group".equals(element2.getName())
                                && element2.getChildText("Name").equals(childChildName.substring(childChildName.indexOf(":") + 1))
                                && formatChildrenNameValues.contains("Group:" + childChildName)) {
                            formatChildrenNameValues.remove("Group:" + childChildName);
                        } else if ("DocStrctType".equals(element2.getName())
                                && element2.getChildText("Name").equals(childChildName.substring(childChildName.indexOf(":") + 1))
                                && formatChildrenNameValues.contains("DocStrctType:" + childChildName)) {
                            formatChildrenNameValues.remove("DocStrctType:" + childChildName);
                        }
                    }
                }
            }

        }

    }

    private void validateMETS(Element root, Element childElement, List<String> formatChildrenNameValues) {
        for (Element ChildChildElement : childElement.getChildren()) {
            // Only do this, if the ChildChild element is named Metadata, Group or DocStruct
            if (ChildChildElement.getName().equals("Metadata") || ChildChildElement.getName().equals("Group")
                    || ChildChildElement.getName().equals("DocStruct")) {

                // Grab the InternalName Value of the ChildChildElement
                if (ChildChildElement.getChild("InternalName") != null) {
                    String childChildName = ChildChildElement.getName() + ":" + ChildChildElement.getChild("InternalName").getText().trim();
                    formatChildrenNameValues.add(childChildName);
                    for (Element element2 : root.getChildren()) {
                        if (childChildName == null || childChildName.isEmpty() || element2.getChild("Name") == null) {
                            continue;
                        }
                        // If the "name" equals MetadataType then 
                        if (("MetadataType".equals(element2.getName())
                                && element2.getChildText("Name").equals(childChildName.substring(childChildName.indexOf(":") + 1)))
                                && formatChildrenNameValues.contains(childChildName)) {
                            formatChildrenNameValues.remove(childChildName);
                            continue;
                        } else if ("Group".equals(element2.getName())
                                && element2.getChildText("Name").equals(childChildName.substring(childChildName.indexOf(":") + 1))
                                && formatChildrenNameValues.contains("Group:" + childChildName)) {
                            formatChildrenNameValues.remove("Group:" + childChildName);
                        } else if ("DocStrctType".equals(element2.getName())
                                && element2.getChildText("Name").equals(childChildName.substring(childChildName.indexOf(":") + 1))
                                && formatChildrenNameValues.contains("DocStrctType:" + childChildName)) {
                            formatChildrenNameValues.remove("DocStrctType:" + childChildName);
                        }
                    }
                }
            }

        }

    }

    private void throwErrors(List<XMLError> errors, String formatType, List<String> formatChildrenNameValues) {
        for (int i = 0; i < formatChildrenNameValues.size(); i++) {
            String formatChildrenValue = formatChildrenNameValues.get(i);
            int colonIndex = formatChildrenValue.indexOf(":");
            if (colonIndex != -1) {
                String beforeColon = formatChildrenValue.substring(0, colonIndex).trim();
                String afterColon = formatChildrenValue.substring(colonIndex + 1).trim();
                if (beforeColon.equals("Metadata")) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_value_for_export", afterColon, formatType)));
                } else if (beforeColon.equals("Group")) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_group_for_export", afterColon, formatType)));
                } else if (beforeColon.equals("DocStruct")) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_docstrct_for_export", afterColon, formatType)));
                } else if (beforeColon.equals("Person")) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_person_for_export", afterColon, formatType)));
                }

            }
        }

    }

}