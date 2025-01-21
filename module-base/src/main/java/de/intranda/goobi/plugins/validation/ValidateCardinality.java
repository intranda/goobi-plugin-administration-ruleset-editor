package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

public class ValidateCardinality {

    /**
     * Validate the XML structure starting from the root element for not allowed values in the "num" field
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public static List<XMLError> validate(org.jdom2.Element root) {
        List<XMLError> errors = new ArrayList<>();
        // Check all children of the root element       
        for (Element element : root.getChildren()) {
            // Check the children of this element       
            checkForInvalidCardinatliy(errors, element);
        }
        return errors;
    }

    /**
     * Checks the child elements of a given XML element for invalid cardinality values.
     *
     * @param errors A list of XMLError objects to collect validation errors.
     * @param element The XML element to be checked for invalid cardinality.
     */
    private static void checkForInvalidCardinatliy(List<XMLError> errors, Element element) {
        List<Element> childElements = element.getChildren();
        Element nameElement = element.getChild("Name");
        for (Element childElement : childElements) {
            String attributeValue = childElement.getAttributeValue("num");
            if (attributeValue != null && !"1o".equals(attributeValue) && !"*".equals(attributeValue) && !"1m".equals(attributeValue)
                    && !"+".equals(attributeValue)) {
                errors.add(new XMLError("ERROR",
                        Helper.getTranslation("ruleset_validation_wrong_cardinality", childElement.getText(), nameElement.getText())));
            }
        }
    }
}