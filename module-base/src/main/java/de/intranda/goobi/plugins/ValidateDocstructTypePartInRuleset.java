package de.intranda.goobi.plugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;

class ValidateDocstructTypePartInRuleset {

    public static void parseAndCheckDocument(org.w3c.dom.Document w3cDocument) {
        // Get the root element and print its name      
        DOMBuilder jdomBuilder = new DOMBuilder();
        org.jdom2.Document jdomDocument = jdomBuilder.build(w3cDocument);
        Element root = jdomDocument.getRootElement();
        // Check all children of the root element       
        iterateOverChildElements(root.getChildren());
    }

    private static void iterateOverChildElements(List<Element> elements) {
        for (Element element : elements) {
            // Check the children of this element       
            checkDocStructNodesForDuplicates(element);
        }
    }

    private static void checkDocStructNodesForDuplicates(Element element) {
        if (!"DocStrctType".equals(element.getName())) {
            return;
        }
        Set<String> valueSet = new HashSet<>();
        List<Element> childElements = element.getChildren();
        for (Element childElement : childElements) {
            String childSignature = childElement.getText();
            if (valueSet.contains(childSignature) && ("metadata".equals(childElement.getName()) || "group".equals(childElement.getName()))) {
                // Duplicate found, print a message     
                System.out.println("Duplicate value found: " + childSignature);
            } else {
                // Add the signature to the set     
                valueSet.add(childSignature);
            }
        }
        // Recursively process the children of the current element      
        iterateOverChildElements(element.getChildren());
    }
}