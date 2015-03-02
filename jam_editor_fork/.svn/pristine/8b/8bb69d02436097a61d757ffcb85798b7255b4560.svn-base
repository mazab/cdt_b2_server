/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.views;

import java.io.File;
import java.util.ArrayList;

import org.boost.eclipse.bjam.system.JamTarget;
import org.boost.eclipse.bjam.system.JamTargetInfo;
import org.boost.eclipse.bjam.system.JamfileScanner;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

class ViewContentProvider implements ITreeContentProvider,
        IResourceChangeListener {
    private final IWorkspace workspace;
    private TreeViewer viewer = null;

    public ViewContentProvider() {
        this.workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(this,
                IResourceChangeEvent.POST_CHANGE);
    }

    public void resourceChanged(final IResourceChangeEvent event) {
        if (viewer != null)
            viewer.refresh(event.getResource());
    }

    public void inputChanged(final Viewer newViewer, final Object oldInput,
            final Object newInput) {
        viewer = (newViewer instanceof TreeViewer) ? (TreeViewer) newViewer
                : null;
    }

    public void dispose() {
        // workspace.removeResourceChangeListener(this);
    }

    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    public Object getParent(Object child) {
        // test for IProject before IContainer
        if (child instanceof IProject) {
            return null;
        }
        if (child instanceof IContainer) {
            IContainer container = (IContainer) child;
            return container.getParent();
        }
        return null;
    }

    public Object[] getChildren(Object parent) {
        final ArrayList<Object> result = new ArrayList<Object>();
        try {
            if (parent instanceof IWorkspaceRoot) {
                result.addAll(JamfileTargetsView
                        .getOpenProjects((IWorkspaceRoot) parent));
            } else if (parent instanceof IContainer) {
                final IContainer container = (IContainer) parent;
                for (IResource resource : container.members()) {
                    if (resource instanceof IFolder)
                        result.add(resource);
                    if (resource instanceof IFile) {
                        final IFile file = (IFile) resource;
                        if (isProperJamfile(file)) {
                            for (final JamTargetInfo info : JamfileScanner
                                    .getTargets(file))
                                result.add(new JamTarget(container, info));
                        }
                    }
                }
            }
        } catch (CoreException e) {
            return new Object[0];
        }
        return result.toArray();
    }

    private boolean isProperJamfile(final IFile file) {
        final String name = file.getName();
        return name.equalsIgnoreCase("jamfile")
                || name.equalsIgnoreCase("jamfile.v2")
                || name.equalsIgnoreCase("jamroot");
    }

    public boolean hasChildren(Object parent) {
        return getChildren(parent).length > 0;
    }
}