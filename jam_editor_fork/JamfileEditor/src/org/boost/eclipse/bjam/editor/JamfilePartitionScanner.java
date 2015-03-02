/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor;

import java.util.ArrayList;

import org.eclipse.jface.text.rules.*;

/**
 * Our partition scanner.
 */
public class JamfilePartitionScanner extends RuleBasedPartitionScanner {
    static public final String SINGLE_LINE_COMMENT = "__jamfile_single_comment";
    static public final String CODE = "__jamfile_code";
    static public final String PARTITIONS[] = new String[] {
            SINGLE_LINE_COMMENT, CODE };

    public JamfilePartitionScanner() {

        final IToken singleComment = new Token(SINGLE_LINE_COMMENT);

        final ArrayList<IPredicateRule> rules = new ArrayList<IPredicateRule>();

        rules.add(new EndOfLineRule("#", singleComment));
        // Add rule for main target rules

        setPredicateRules(rules.toArray(new IPredicateRule[0]));
    }
}
