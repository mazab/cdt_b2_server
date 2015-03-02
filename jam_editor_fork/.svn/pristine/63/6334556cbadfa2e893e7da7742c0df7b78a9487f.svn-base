/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.system;

import org.eclipse.core.resources.IFile;

public class JamTargetInfo {
    /**
     * Name of this target which is shown in the UI.
     */
    private String name;
    /**
     * The commandline string which will be appended to the bjam invocation.
     */
    private String targetName;
    private IFile resource;

    public JamTargetInfo(String name, String targetName) {
        this.name = name;
        this.targetName = targetName;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setResource(final IFile file) {
        resource = file;
    }

    public IFile getResource() {
        return resource;
    }
}
