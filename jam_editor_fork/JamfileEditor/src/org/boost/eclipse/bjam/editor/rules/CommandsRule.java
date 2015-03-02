/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor.rules;

import org.eclipse.jface.text.rules.IToken;

public class CommandsRule extends WordRuleBase {
    static private final String[] targets = new String[] { "import", "using",
            "build-project", "explicit", "requirements", "usage-requirements",
            "build-dir" };

    public CommandsRule(final IToken token) {
        super(targets, token);
    }
}
