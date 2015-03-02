/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor;


import org.boost.eclipse.bjam.editor.scanners.Default;
import org.boost.eclipse.bjam.editor.scanners.SingleComment;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * Our viewer config.
 */
public class JamfileConfiguration extends SourceViewerConfiguration {
    private final RuleBasedScanner defaultScanner;
    private final SingleComment singleCommentScanner;

    public JamfileConfiguration(final ColorManager colorManager) {
        defaultScanner = new Default(colorManager);
        defaultScanner.setDefaultReturnToken(new Token(new TextAttribute(
                colorManager.getColor(IColors.DEFAULT))));

        singleCommentScanner = new SingleComment(colorManager);
        singleCommentScanner.setDefaultReturnToken(new Token(new TextAttribute(
                colorManager.getColor(IColors.DEFAULT))));
    }

    public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
        return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
                JamfilePartitionScanner.SINGLE_LINE_COMMENT };
    }

    public IPresentationReconciler getPresentationReconciler(
            final ISourceViewer sourceViewer) {
        final PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(
                singleCommentScanner);
        reconciler.setDamager(dr, JamfilePartitionScanner.SINGLE_LINE_COMMENT);
        reconciler.setRepairer(dr, JamfilePartitionScanner.SINGLE_LINE_COMMENT);

        dr = new DefaultDamagerRepairer(defaultScanner);
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        return reconciler;
    }

}
