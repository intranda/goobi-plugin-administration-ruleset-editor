package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

/**
 * Find Cardinality values in the <DocStrctType> elements which are not equal to "1o", "*", "1m" or "+" and return those into the errors list
 */
public class ValidateCardinality {

    /**
     * Validate the XML structure starting from the root element for not allowed values in the "num" field
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<XMLError> validate(org.jdom2.Element root) {
        List<XMLError> errors = new ArrayList<>();
        for (Element element : root.getChildren()) {
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
    private void checkForInvalidCardinatliy(List<XMLError> errors, Element element) {
        List<Element> childElements = element.getChildren();
        Element nameElement = element.getChild("Name");
        for (Element childElement : childElements) {
            // Skip elements that are not "group" or "metadata"
            if (!"group".equals(childElement.getName()) && !"metadata".equals(childElement.getName())) {
                continue;
            }
            String attributeValue = childElement.getAttributeValue("num");
            if (attributeValue != null && !"1o".equals(attributeValue) && !"*".equals(attributeValue) && !"1m".equals(attributeValue)
                    && !"+".equals(attributeValue)) {
                errors.add(new XMLError("ERROR",
                        Helper.getTranslation("ruleset_validation_wrong_cardinality", childElement.getText(), nameElement.getText(),
                                attributeValue)));
            }
        }
    }
}