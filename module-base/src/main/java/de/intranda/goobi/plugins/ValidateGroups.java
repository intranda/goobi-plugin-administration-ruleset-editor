package de.intranda.goobi.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;

class ValidateGroups {

    public static List<XMLError> validate(org.jdom2.Element root) {
        List<XMLError> errors = new ArrayList<>();
        // Check all children of the root element       
        for (Element element : root.getChildren()) {
            // Check the children of this element       
            checkForUnvalidGroups(errors, element);
        }
        return errors;
    }

    private static void checkForUnvalidGroups(List<XMLError> errors, Element element) {
        if (!"Group".equals(element.getName())) {
            return;
        }
        Set<String> valueSet = new HashSet<>();
        List<Element> childElements = element.getChildren();
        for (Element childElement : childElements) {
            String childSignature = childElement.getName() + ":" + childElement.getText();
            if (valueSet.contains(childSignature) && ("metadata".equals(childElement.getName()) || ("group".equals(childElement.getName())))) {
                // Add found to errors List
                errors.add(new XMLError(0, 0, "ERROR", "Duplicate value found: " + childElement.getText()));
            } else {
                // Add the signature to the set
                valueSet.add(childSignature);
            }
        }
    }
}