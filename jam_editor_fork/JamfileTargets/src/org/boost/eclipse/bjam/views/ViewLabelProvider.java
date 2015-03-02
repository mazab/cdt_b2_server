/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.views;


import org.boost.eclipse.bjam.system.JamTarget;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

class ViewLabelProvider extends LabelProvider {
    public String getText(Object obj) {
        if (obj instanceof IResource) {
            IResource resource = (IResource) obj;
            return resource.getName();
        }
        return obj.toString();
    }

    @SuppressWarnings("deprecation")
    static private String testStd(final Object obj) {
        if (obj instanceof IProject) {
            // how to get project specific image??
            // final IProject project = (IProject) obj;
            return ISharedImages.IMG_OBJ_PROJECT;
        }
        if (obj instanceof IFolder)
            return ISharedImages.IMG_OBJ_FOLDER;
        if (obj instanceof JamTarget)
            return ISharedImages.IMG_OBJ_ELEMENT;
        return null;
    }

    public Image getImage(final Object obj) {
        String imageKey = testStd(obj);
        if (imageKey == null)
            imageKey = ISharedImages.IMG_OBJS_ERROR_TSK;
        return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
    }
}
