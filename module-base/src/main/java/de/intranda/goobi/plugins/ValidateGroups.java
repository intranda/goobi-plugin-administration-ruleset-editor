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
        iterateOverChildElements(errors, root.getChildren());
        System.out.println(errors);
        return errors;
    }

    private static void iterateOverChildElements(List<XMLError> errors, List<Element> elements) {
        for (Element element : elements) {
            // Check the children of this element       
            checkDocStructNodesForDuplicates(errors, element);
        }
    }

    private static void checkDocStructNodesForDuplicates(List<XMLError> errors, Element element) {
        if (!"Group".equals(element.getName())) {
            return;
        }
        Set<String> valueSet = new HashSet<>();
        List<Element> childElements = element.getChildren();
        for (Element childElement : childElements) {
            String childSignature = childElement.getName() + ":" + childElement.getText();
            if (valueSet.contains(childSignature) && ("metadata".equals(childElement.getName()) || ("group".equals(childElement.getName())))) {
                // Add found to errors List
                errors.add(new XMLError(0, 0, "Error", "Duplicate value found: " + childElement.getText()));
            } else {
                // Add the signature to the set
                valueSet.add(childSignature);
            }
        }
        // Recursively process the children of the current element      
        iterateOverChildElements(errors, element.getChildren());
    }
}