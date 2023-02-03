package net.revelc.code.formatter.xml.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentFormatter {

    private static final Pattern ORIGINAL_INDENT_PATTERN = Pattern.compile("^(?<indent>\\s*)-->");

    public String format(String tagText, String indent, String lineDelimiter) {
        String[] lines = tagText.split(lineDelimiter);
        String originalIndent = resolveOriginalIndent(lines);

        List<String> newLines = new ArrayList<>();
        for (String line : lines) {
            newLines.add(indent + removeOriginalIndent(line, originalIndent));
        }

        return String.join(lineDelimiter, newLines);
    }

    private String resolveOriginalIndent(String[] lines) {
        // only multi-line comments need replace original indentation.
        if (lines.length < 2) {
            return null;
        }

        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i];

            if (line.trim().endsWith("-->")) {
                Matcher m = ORIGINAL_INDENT_PATTERN.matcher(line);
                if (m.matches()) {
                    return m.group("indent");
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private static String removeOriginalIndent(String line, String indent) {
        if (indent != null && line.startsWith(indent)) {
            return line.substring(indent.length());
        } else {
            return line;
        }
    }
}
