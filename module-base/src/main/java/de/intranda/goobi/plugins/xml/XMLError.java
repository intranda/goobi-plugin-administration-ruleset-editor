package de.intranda.goobi.plugins.xml;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class XMLError {
    private int line;
    private int column;
    private String severity;
    private String message;

    public XMLError(String severity, String message, String line) {
        this.severity = severity;
        this.message = message;
        this.line = Integer.parseInt(line);
        column = 0;
    }
}
