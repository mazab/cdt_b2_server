/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor.rules;

import org.eclipse.jface.text.rules.IToken;

public class JamBuiltinRule extends WordRuleBase {
    static private final String[] targets = new String[] { "DEPENDS",
            "INCLUDES", "IMPORT", "ALWAYS", "LEAVES", "NOCARE", "NOTFILE",
            "NOUPDATE", "TEMPORARY", "FAIL_EXPECTED", "RMOLD", "ISFILE",
            "ECHO", "EXIT", "GLOB", "MATCH", "BACKTRACE", "UPDATE",
            "W32_GETREG", "W32_GETREGNAMES", "SHELL", "SEARCH", "LOCATE",
            "HDRSCAN", "HDRRULE", "__TIMING_RULE__", "__ACTION_RULE__",
            "RULENAMES", "VARNAMES", "EXPORT", "CALLER_MODULE", "DELETE_MODULE" };

    public JamBuiltinRule(final IToken token) {
        super(targets, token);
    }
}
