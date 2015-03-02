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
import org.boost.eclipse.bjam.editor.rules.CommandsRule;
import org.boost.eclipse.bjam.editor.rules.JamBuiltinRule;
import org.boost.eclipse.bjam.editor.rules.JamFlowRule;
import org.boost.eclipse.bjam.editor.rules.JamPunctuationRule;
import org.boost.eclipse.bjam.editor.rules.MainTargetRuleRule;
import org.boost.eclipse.bjam.editor.rules.PropertiesRule;
import org.boost.eclipse.bjam.editor.rules.PunctuationRule;
import org.boost.eclipse.bjam.editor.rules.VariablesRule;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;
import org.eclipse.swt.graphics.RGB;

/**
 * Specific scanner.
 */
public class Default extends RuleBasedScanner {
    private final ColorManager manager;

    private IToken getToken(final RGB color) {
        return new Token(new TextAttribute(manager.getColor(color)));
    }

    public Default(final ColorManager manager) {
        this.manager = manager;

        final ArrayList<IRule> rules = new ArrayList<IRule>();

        rules.add(new MainTargetRuleRule(getToken(IColors.MAIN_TARGET_RULE)));
        rules.add(new CommandsRule(getToken(IColors.COMMANDS)));
        rules.add(new PropertiesRule(getToken(IColors.PROPERTIES)));
        rules.add(new VariablesRule(getToken(IColors.VARIABLES)));
        rules.add(new JamBuiltinRule(getToken(IColors.JAM_BUILTIN)));
        rules.add(new JamFlowRule(getToken(IColors.JAM_FLOW)));
        rules.add(new JamPunctuationRule(getToken(IColors.JAM_PUNCTUATION)));
        rules.add(new PunctuationRule(getToken(IColors.PUNCTUATION)));
        rules.add(new WhitespaceRule(new JamfileWhitespaceDetector()));

        setRules(rules.toArray(new IRule[0]));
    }
}
