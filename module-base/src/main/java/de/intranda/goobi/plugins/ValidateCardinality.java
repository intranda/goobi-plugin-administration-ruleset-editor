package de.intranda.goobi.plugins;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;

class ValidateCardinality {

    public static List<XMLError> validate(org.jdom2.Element root) {
        List<XMLError> errors = new ArrayList<>();
        // Check all children of the root element       
        iterateOverChildElements(errors, root.getChildren());
        return errors;
    }

    private static void iterateOverChildElements(List<XMLError> errors, List<Element> elements) {
        for (Element element : elements) {
            // Check the children of this element       
            checkForUnvalidCardinatliy(errors, element);
        }
    }

    private static void checkForUnvalidCardinatliy(List<XMLError> errors, Element element) {
        List<Element> childElements = element.getChildren();
        for (Element childElement : childElements) {
            String attributeValue = childElement.getAttributeValue("num");
            if (attributeValue != null && !"1o".equals(attributeValue) && !"*".equals(attributeValue) && !"1m".equals(attributeValue)
                    && !"+".equals(attributeValue)) {
                System.out.println(attributeValue);
            }
        }

        // Recursively process the children of the current element      
        iterateOverChildElements(errors, element.getChildren());
    }
}