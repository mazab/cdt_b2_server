/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class JamfileWhitespaceDetector implements IWhitespaceDetector {

    public boolean isWhitespace(final char c) {
        return staticIsWhitespace(c);
    }

    static public boolean staticIsWhitespace(final char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
    }
}
