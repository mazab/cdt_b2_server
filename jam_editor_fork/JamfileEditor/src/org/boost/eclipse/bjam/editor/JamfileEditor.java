/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.editor;

import org.eclipse.ui.editors.text.TextEditor;

public class JamfileEditor extends TextEditor {

    private final ColorManager colorManager;

    public JamfileEditor() {
        super();
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new JamfileConfiguration(colorManager));
        setDocumentProvider(new JamfileDocumentProvider());
    }

    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }

}
