/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor.rules;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.WordRule;

public class WordRuleBase extends WordRule {
    public WordRuleBase(final String[] targets, final IToken token) {
        super(new WhitespaceBoundWordDetector());

        for (final String target : targets)
            addWord(target, token);
    }
}
