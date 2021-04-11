/*******************************************************************************
 * Copyright (c) 2019 Jose Montoya
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Saikiran Revuru - Added test case testDeleteBlankLines 
 *******************************************************************************/
package net.revelc.code.formatter.xml.lib;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class FormatterTest {
    
    @Test
    public void testDeleteBlankLines() throws IOException {
	FormattingPreferences prefs = new FormattingPreferences();
	prefs.setSplitMultiAttrs(true);
	XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);

	String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/test-space-input.xml")),
		StandardCharsets.UTF_8);
	String outXml = formatter.format(inXml);

	assertEquals(outXml, new String(Files.readAllBytes(Paths.get("src/test/resources/test-space-expected.xml")),
		StandardCharsets.UTF_8));
    }

    @Test
    public void testDefaultPreferences() throws Exception {
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), new FormattingPreferences());

        String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/test-input.xml")),
                StandardCharsets.UTF_8);
        String outXml = formatter.format(inXml);

        assertEquals(outXml, new String(Files.readAllBytes(Paths.get("src/test/resources/default-output.xml")),
                StandardCharsets.UTF_8));
    }

    @Test
    public void testMultiLinedAttrs() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setSplitMultiAttrs(true);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);

        String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/test-input.xml")),
                StandardCharsets.UTF_8);
        String outXml = formatter.format(inXml);

        assertEquals(outXml,
                new String(Files.readAllBytes(Paths.get("src/test/resources/multi-lined-attrs-output.xml")),
                        StandardCharsets.UTF_8));
    }

    @Test
    public void testNoWrapTags() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWrapLongLines(false);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);

        String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/test-input.xml")),
                StandardCharsets.UTF_8);
        String outXml = formatter.format(inXml);

        assertEquals(outXml, new String(Files.readAllBytes(Paths.get("src/test/resources/no-wrap-tags-output.xml")),
                StandardCharsets.UTF_8));
    }

    @Test
    public void testMalformedCaught() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWellFormedValidation(FormattingPreferences.FAIL);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);
        String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/malformed.xml")),
                StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class, () -> formatter.format(inXml));
    }

    @Test
    public void testIndentationReset() throws Exception {
        // reusing a formatter on a malformed xml document without a balanced pair of end tags should
        // not interfere with the formatter's indentation of subsequent files
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWellFormedValidation(FormattingPreferences.IGNORE);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);
        // this file contains an opening tag, but not a corresponding closing tag
        String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/malformed.xml")),
                StandardCharsets.UTF_8);
        // format the malformed file once and ignore any errors
        String outXml = formatter.format(inXml);
        // format the malformed file a second time and ignore any errors
        String outXml2 = formatter.format(inXml);
        // both format attempts should result in content indented the same
        assertEquals(outXml, outXml2);
    }

    @Test
    public void testNoRootElement() throws Exception {
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), new FormattingPreferences());
        String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/sample-orca5-deps.xml")),
                StandardCharsets.UTF_8);

        assertDoesNotThrow(() -> formatter.format(inXml));
    }

    @Test
    public void testNoRootElementFails() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWellFormedValidation(FormattingPreferences.FAIL);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);
        String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/sample-orca5-deps.xml")),
                StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class, () -> formatter.format(inXml));
    }

    @Test
    public void testNoDtdValidation() throws Exception {
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), new FormattingPreferences());
        String inXml = new String(Files.readAllBytes(Paths.get("src/test/resources/dtd-test-input.xml")),
                StandardCharsets.UTF_8);

        assertDoesNotThrow(() -> formatter.format(inXml));
    }

}
