/*******************************************************************************
 * Copyright (c) 2019 Jose Montoya
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package net.revelc.code.formatter.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class FormatterTest {

    @Test
    public void testDefaultPreferences() throws Exception {
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), new FormattingPreferences());

        String inXml = FileUtils.readFileToString(new File("src/test/resources/test-input.xml"), "UTF-8");
        String outXml = formatter.format(inXml);

        assertEquals(outXml, FileUtils.readFileToString(new File("src/test/resources/default-output.xml"), "UTF-8"));
    }

    @Test
    public void testMultiLinedAttrs() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setSplitMultiAttrs(true);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);

        String inXml = FileUtils.readFileToString(new File("src/test/resources/test-input.xml"), "UTF-8");
        String outXml = formatter.format(inXml);

        assertEquals(outXml,
                FileUtils.readFileToString(new File("src/test/resources/multi-lined-attrs-output.xml"), "UTF-8"));
    }

    @Test
    public void testNoWrapTags() throws Exception {
        FormattingPreferences prefs = new FormattingPreferences();
        prefs.setWrapLongLines(false);
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), prefs);

        String inXml = FileUtils.readFileToString(new File("src/test/resources/test-input.xml"), "UTF-8");
        String outXml = formatter.format(inXml);

        assertEquals(outXml,
                FileUtils.readFileToString(new File("src/test/resources/no-wrap-tags-output.xml"), "UTF-8"));
    }

    @Test
    public void testMalformedCaught() throws Exception {
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(System.lineSeparator(), new FormattingPreferences());
        String inXml = FileUtils.readFileToString(new File("src/test/resources/malformed.xml"), "UTF-8");

        assertThrows(IllegalArgumentException.class, () -> formatter.format(inXml));
    }
}
