/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor.rules;

import org.eclipse.jface.text.rules.IToken;

public class MainTargetRuleRule extends WordRuleBase {
    public static final String[] targets = new String[] { "exe", "lib",
            "alias", "project", "boost-build", "install", "stage",
            "use-project", "rc", "obj", "h", "cpp", "slices", "bundle", "make",
            "notfile", "test-suite" };

    public MainTargetRuleRule(final IToken token) {
        super(targets, token);
    }
}
