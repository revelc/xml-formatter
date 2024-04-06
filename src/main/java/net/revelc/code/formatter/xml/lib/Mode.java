/*
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.revelc.code.formatter.xml.lib;

public class Mode {

    private int modeValue;

    public void setAttributeNameSearching() {
        modeValue = 0;
    }

    public void setAttributeNameFound() {
        modeValue = 1;
    }

    public void setAttributeValueSearching() {
        modeValue = 2;
    }

    public void setAttributeValueFound() {
        modeValue = 3;
    }

    public void setFinished() {
        modeValue = 4;
    }

    public boolean isAttributeNameSearching() {
        return modeValue == 0;
    }

    public boolean isAttributeNameFound() {
        return modeValue == 1;
    }

    public boolean isAttributeValueSearching() {
        return modeValue == 2;
    }

    public boolean isAttributeValueFound() {
        return modeValue == 3;
    }

    public boolean isFinished() {
        return modeValue == 4;
    }
}
