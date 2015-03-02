/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor.scanners;


import java.util.ArrayList;

import org.boost.eclipse.bjam.editor.ColorManager;
import org.boost.eclipse.bjam.editor.IColors;
import org.boost.eclipse.bjam.editor.JamfileWhitespaceDetector;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

/**
 * Specific scanner.
 */
public class SingleComment extends RuleBasedScanner {

    public SingleComment(final ColorManager manager) {
        final IToken singleComment = new Token(new TextAttribute(manager
                .getColor(IColors.SINGLE_COMMENT)));

        final ArrayList<IRule> rules = new ArrayList<IRule>();

        // Add rule for processing instructions
        rules.add(new EndOfLineRule("#", singleComment));
        // Add generic whitespace rule.
        rules.add(new WhitespaceRule(new JamfileWhitespaceDetector()));

        setRules(rules.toArray(new IRule[0]));
    }
}
