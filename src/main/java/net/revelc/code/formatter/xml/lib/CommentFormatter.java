/*
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.revelc.code.formatter.xml.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommentFormatter {

    private static final Logger logger = LoggerFactory.getLogger(CommentFormatter.class);

    public String format(String tagText, String indent, String lineDelimiter, FormattingPreferences prefs) {
        String[] lines = tagText.split(lineDelimiter, -1);

        if (logger.isDebugEnabled()) {
            logger.debug("input: {}\n", Arrays.toString(lines));
        }

        // Caller sets initial indents to method (no indent before <!-- and indent before -->)
        List<String> newLines = new ArrayList<>();
        for (String line : lines) {
            // do not trim leading space on multi-line comments (just add one indent)
            if (lines.length < 2) {
                newLines.add(indent + line);
            } else if (line.trim().equals("<!--") || line.trim().equals("-->") || line.contains("<!--")) {
                // add one indent for begin comment / end comment / line starting comment with text
                // allowing last line with comment to be indented twice on else
                newLines.add(indent + line.stripLeading());
            } else if (indent.equals("") && !line.stripLeading().equals("")) {
                // If indent is empty and line is not empty, use canonicalIndent
                newLines.add(prefs.getCanonicalIndent() + line.stripLeading());
            } else {
                // Add two intent for others
                newLines.add(indent + indent + line.stripLeading());
            }
        }

        logger.debug("output: {}\n", newLines);

        // Returned data to caller will be properly positioned by caller
        return String.join(lineDelimiter, newLines);
    }

}
