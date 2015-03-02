/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor.rules;

import org.eclipse.jface.text.rules.IToken;

public class PunctuationRule extends WordRuleBase {
    static private final String[] targets = new String[] { ";", ":", "[", "]",
            "{", "}" };

    public PunctuationRule(final IToken token) {
        super(targets, token);
    }
}
