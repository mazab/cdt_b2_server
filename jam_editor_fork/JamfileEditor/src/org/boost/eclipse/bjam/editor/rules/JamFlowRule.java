/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor.rules;

import org.eclipse.jface.text.rules.IToken;

public class JamFlowRule extends WordRuleBase {
    static private final String[] targets = new String[] { "for", "in", "if",
            "else", "include", "local", "return", "switch", "case", "while",
            "rule", "actions", "module", "class" };

    public JamFlowRule(final IToken token) {
        super(targets, token);
    }
}
