
package de.intranda.goobi.plugins.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.intranda.goobi.plugins.xml.XMLError;
import de.sub.goobi.helper.Helper;

public class ValidateFormats {

    /**
     * Validates the structure of an XML document by checking the export formats
     *
     * @param root The root element of the XML document to validate.
     * @param format The format of the export part which will be checked
     * @param name This value is either "name" or "InternalName" depends on the format
     * @return A list of {@link XMLError} objects containing validation errors, if any.
     */
    public List<XMLError> validate(Element root, String format) {
        List<XMLError> errors = new ArrayList<>();
        String name = "Name";
        if (format.equals("METS") || format.equals("LIDO")) {
            name = "InternalName";
        }
        checkElements(root, errors, "DocStruct", "DocStrctType", format, name);
        checkElements(root, errors, "Metadata", "MetadataType", format, name);

        // In the following cases there is a <Person> value which also has to be checked
        if (format.equals("PicaPlus") || format.equals("Marc")) {
            checkElements(root, errors, "Person", "MetadataType", format, name);
        }
        if (format.equals("PicaPlus")) {
            checkElements(root, errors, "Corporate", "MetadataType", format, name);
        }
        checkElements(root, errors, "Group", "Group", format, name);
        return errors;
    }

    /**
     * Checks if specific export elements in the given XML document are defined but not used.
     *
     * @param root The root element of the XML document.
     * @param errors A list of {@link XMLError} objects to which validation errors are added.
     * @param type The type of the XML element to check (e.g., "DocStruct", "Metadata", "Person").
     * @param definition The expected definition of the element (e.g., "DocStrctType", "MetadataType").
     * @param format The format of the XML document.
     * @param name This value is either "name" or "InternalName" depends on the format
     */
    private void checkElements(Element root, List<XMLError> errors, String type, String definition, String format, String name) {
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<Element> xp = xpfac.compile("//Formats//" + format + "//" + type, Filters.element());
        List<Element> Elements = xp.evaluate(root);

        // run through all elements in formats section
        for (Element element : Elements) {
            String formatName = element.getChild(name).getText().trim();
            String lineNumber = element.getAttributeValue("lineNumber");
            String lineInfo = (lineNumber != null) ? lineNumber.trim() : "0";
            XPathExpression<Element> xp1 = xpfac.compile("//" + definition + "[Name='" + formatName + "']", Filters.element());

            // If the type is a Person, check if the MetadataType has a type attribute valued with "person"
            if (type.equals("Person")) {
                XPathExpression<Element> xp2 = xpfac.compile("//MetadataType[@type='person' and Name='" + formatName + "']", Filters.element());
                if (xp1.evaluate(root).size() < 1 && xp2.evaluate(root).size() < 1) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_" + type.toLowerCase() + "_for_export",
                                    formatName, format),lineInfo));
                }
            }
            
            // If the type is a Corporate, check if the MetadataType has a type attribute valued with "corporate"
            if (type.equals("Corporate")) {
                XPathExpression<Element> xp3 = xpfac.compile("//MetadataType[@type='corporate' and Name='" + formatName + "']", Filters.element());
                if (xp1.evaluate(root).size() < 1 && xp3.evaluate(root).size() < 1) {
                    errors.add(new XMLError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_" + type.toLowerCase() + "_for_export",
                                    formatName, format), lineInfo));
                }
            }
            
            // If a value of a Metadata, Group or DocStrct is not defined above throw out an error
            if (!type.equals("Person") && !type.equals("Corporate")) {
	            if (xp1.evaluate(root).size() < 1) {
	                errors.add(new XMLError("ERROR",
	                        Helper.getTranslation("ruleset_validation_used_but_undefined_" + type.toLowerCase() + "_for_export",
	                                formatName, format), lineInfo));
	            }
            }
        }
    }
}