/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.views;


import org.boost.eclipse.bjam.system.JamTarget;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

class NameSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        final boolean isJam1 = e1 instanceof JamTarget;
        final boolean isJam2 = e2 instanceof JamTarget;
        if (isJam1 && isJam2)
            return ((JamTarget) e1).getTargetName().compareTo(
                    ((JamTarget) e2).getTargetName());
        if (isJam1 == isJam2)
            return super.compare(viewer, e1, e2);
        return isJam1 ? 1 : -1;
    }
}