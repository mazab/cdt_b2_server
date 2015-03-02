/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor.rules;


import org.boost.eclipse.bjam.editor.JamfileWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;

class WhitespaceBoundWordDetector implements IWordDetector {
    public boolean isWordStart(final char c) {
        return !JamfileWhitespaceDetector.staticIsWhitespace(c);
    }

    public boolean isWordPart(final char c) {
        return !JamfileWhitespaceDetector.staticIsWhitespace(c);
    }
}