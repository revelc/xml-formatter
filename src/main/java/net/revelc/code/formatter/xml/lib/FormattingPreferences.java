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
    private boolean splitMultiAttrs = false;
    private String saxValidation = WARN;

    public void setMaxLineLength(Integer maxLineLength) {
        if (maxLineLength != null)
            this.maxLineLength = maxLineLength;
    }

    public void setWrapLongLines(Boolean wrapLongLines) {
        if (wrapLongLines != null)
            this.wrapLongLines = wrapLongLines;
    }

    public void setTabInsteadOfSpaces(Boolean tabInsteadOfSpaces) {
        if (tabInsteadOfSpaces != null)
            this.tabInsteadOfSpaces = tabInsteadOfSpaces;
    }

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

    public int getMaximumLineWidth() {
        return maxLineLength;
    }

    public boolean wrapLongTags() {
        return wrapLongLines;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public void setTabWidth(Integer tabWidth) {
        if (tabWidth != null)
            this.tabWidth = tabWidth;
    }

    public boolean useTabInsteadOfSpaces() {
        return tabInsteadOfSpaces;
    }

    public boolean isSplitMultiAttrs() {
        return splitMultiAttrs;
    }

    public void setSplitMultiAttrs(Boolean setSplitMultiAttrs) {
        if (setSplitMultiAttrs != null)
            this.splitMultiAttrs = setSplitMultiAttrs;
    }

    public String getSaxValidation() { return saxValidation; }

    public void setSaxValidation(String saxValidation) {
        if (saxValidation.equals(IGNORE) || saxValidation.equals(FAIL)) {
            this.saxValidation = saxValidation;
        } else {
            this.saxValidation = WARN;
        }
    }
}