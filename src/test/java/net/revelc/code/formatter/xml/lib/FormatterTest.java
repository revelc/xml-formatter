package net.revelc.code.formatter.xml.lib;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class FormatterTest {

    @Test
    void testDeleteBlankLines() throws IOException {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setDeleteBlankLines(true);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);

        String inXml = Files.readString(Paths.get("src/test/resources/test-space-input.xml"));
        String outXml = formatter.format(inXml);
        assertEquals(outXml, Files.readString(Paths.get("src/test/resources/test-space-expected.xml")));
    }

    @Test
    void testDefaultPreferences() throws Exception {
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), new FormattingPreferences());

        String inXml = Files.readString(Paths.get("src/test/resources/test-input.xml"));
        String outXml = formatter.format(inXml);

        assertEquals(outXml, Files.readString(Paths.get("src/test/resources/default-output.xml")));
    }

    @Test
    void testMultiLinedAttrs() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setSplitMultiAttrs(true);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);

        String inXml = Files.readString(Paths.get("src/test/resources/test-input.xml"));
        String outXml = formatter.format(inXml);

        assertEquals(outXml, Files.readString(Paths.get("src/test/resources/multi-lined-attrs-output.xml")));
    }

    @Test
    void testNoWrapTags() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWrapLongLines(false);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);

        String inXml = Files.readString(Paths.get("src/test/resources/test-input.xml"));
        String outXml = formatter.format(inXml);

        assertEquals(outXml, Files.readString(Paths.get("src/test/resources/no-wrap-tags-output.xml")));
    }

    @Test
    void testMalformedCaught() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWellFormedValidation(FormattingPreferences.FAIL);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);
        String inXml = Files.readString(Paths.get("src/test/resources/malformed.xml"));

        assertThrows(IllegalArgumentException.class, () -> formatter.format(inXml));
    }

    @Test
    void testIndentationReset() throws Exception {
        // reusing a formatter on a malformed xml document without a balanced pair of end tags should
        // not interfere with the formatter's indentation of subsequent files
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWellFormedValidation(FormattingPreferences.IGNORE);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);
        // this file contains an opening tag, but not a corresponding closing tag
        String inXml = Files.readString(Paths.get("src/test/resources/malformed.xml"));
        // format the malformed file once and ignore any errors
        String outXml = formatter.format(inXml);
        // format the malformed file a second time and ignore any errors
        String outXml2 = formatter.format(inXml);
        // both format attempts should result in content indented the same
        assertEquals(outXml, outXml2);
    }

    @Test
    void testNoRootElement() throws Exception {
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), new FormattingPreferences());
        String inXml = Files.readString(Paths.get("src/test/resources/sample-orca5-deps.xml"));

        assertDoesNotThrow(() -> formatter.format(inXml));
    }

    @Test
    void testNoRootElementFails() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWellFormedValidation(FormattingPreferences.FAIL);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);
        String inXml = Files.readString(Paths.get("src/test/resources/sample-orca5-deps.xml"));

        assertThrows(IllegalArgumentException.class, () -> formatter.format(inXml));
    }

    @Test
    void testNoDtdValidation() throws Exception {
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), new FormattingPreferences());
        String inXml = Files.readString(Paths.get("src/test/resources/dtd-test-input.xml"));

        assertDoesNotThrow(() -> formatter.format(inXml));
    }

}
