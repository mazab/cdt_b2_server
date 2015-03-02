/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.system;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;

public class JamTarget {
    private final JamTargetInfo targetInfo;
    /**
     * Resource where bjam will be invoked.
     */
    private IContainer container;

    public JamTarget(final IContainer container, final JamTargetInfo info) {
        this.targetInfo = info;
        this.container = container;
    }

    public JamTarget(IContainer container, String name, String targetName) {
        this.targetInfo = new JamTargetInfo(name, targetName);
        this.container = container;
    }

    public String toString() {
        return getName();
    }

    public IContainer getFolder() {
        return container;
    }

    public String getName() {
        return targetInfo.getName();
    }

    public String getTargetName() {
        return targetInfo.getTargetName();
    }

    public IFile getResource() {
        return targetInfo.getResource();
    }
}
