/*******************************************************************************
 * Copyright (c) 2004, 2011 John-Mason P. Shackelford and others.,
 *               2019 Jose Montoya
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John-Mason P. Shackelford - initial API and implementation
 * 	   IBM Corporation - bug fixes
 * 	   Jose Montoya - Modified implementation outside Eclipse Platform
 *******************************************************************************/
package net.revelc.code.formatter.xml.lib;

public class FormattingPreferences {
    public static final String IGNORE = "IGNORE";
    public static final String WARN = "WARN";
    public static final String FAIL = "FAIL";

    private int maxLineLength = 120;
    private boolean wrapLongLines = true;
    private boolean tabInsteadOfSpaces = true;
    private int tabWidth = 4;
    private boolean splitMultiAttrs;
    private String wellFormedValidation = WARN;
    private boolean deleteBlankLines;

    public String getCanonicalIndent() {
        String canonicalIndent;
        if (useTabInsteadOfSpaces()) {
            canonicalIndent = "\t"; //$NON-NLS-1$
        } else {
            String tab = "";
            for (int i = 0; i < getTabWidth(); i++) {
                tab = tab.concat(" "); //$NON-NLS-1$
            }
            canonicalIndent = tab;
        }

        return canonicalIndent;
    }

    public boolean getDeleteBlankLines() {
        return deleteBlankLines;
    }

    public int getMaximumLineWidth() {
        return maxLineLength;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public String getWellFormedValidation() {
        return wellFormedValidation;
    }

    public boolean isSplitMultiAttrs() {
        return splitMultiAttrs;
    }

    public void setDeleteBlankLines(boolean deleteBlankLines) {
        this.deleteBlankLines = deleteBlankLines;
    }

    public void setMaxLineLength(int maxLineLength) {
        this.maxLineLength = maxLineLength;
    }

    public void setSplitMultiAttrs(boolean setSplitMultiAttrs) {
        this.splitMultiAttrs = setSplitMultiAttrs;
    }

    public void setTabInsteadOfSpaces(boolean tabInsteadOfSpaces) {
        this.tabInsteadOfSpaces = tabInsteadOfSpaces;
    }

    public void setTabWidth(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    public void setWellFormedValidation(String wellFormedValidation) {
        if (!wellFormedValidation.equals(IGNORE) && !wellFormedValidation.equals(FAIL)
                && !wellFormedValidation.equals(WARN)) {
            throw new IllegalArgumentException(
                    "Invalid configuration value for well formed validation: " + wellFormedValidation);
        }
        this.wellFormedValidation = wellFormedValidation;
    }

    public void setWrapLongLines(boolean wrapLongLines) {
        this.wrapLongLines = wrapLongLines;
    }

    public boolean useTabInsteadOfSpaces() {
        return tabInsteadOfSpaces;
    }

    public boolean wrapLongTags() {
        return wrapLongLines;
    }
}
