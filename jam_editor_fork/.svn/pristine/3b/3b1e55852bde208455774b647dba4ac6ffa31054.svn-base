/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.views;

import org.boost.eclipse.bjam.system.JamTarget;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;

class MakeTargetAction extends Action {
    private final TreeViewer viewer;
    private final String arguments;
    /**
     * Using the toString() method just when a target is built. That way this
     * object can pass additional options to bjam.
     */
    private final Object dynamicOptions;

    MakeTargetAction(final TreeViewer viewer, final Object dynamicOptions) {
        this(viewer, null, dynamicOptions);
    }

    MakeTargetAction(final TreeViewer viewer, final String arguments,
            final Object dynamicOptions) {
        this.viewer = viewer;
        this.arguments = arguments;
        this.dynamicOptions = dynamicOptions;
    }

    private class RecursiveBuilder {
        private final ITreeContentProvider provider = (ITreeContentProvider) viewer
                .getContentProvider();

        private void buildAll(final Object what) throws CoreException {
            buildAll(provider.getChildren(what));
        }

        private void buildAll(final Object which[]) throws CoreException {
            for (final Object object : which) {
                if (object instanceof IFolder)
                    buildAll(object);
                else if (object instanceof JamTarget)
                    buildJamTarget((JamTarget) object);
            }
        }
    }

    private void recursiveBuild(final Object what) {
        try {
            new RecursiveBuilder().buildAll(what);
        } catch (final CoreException e) {
            // ignore
        }
    }

    public void run() {
        final IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        for (final Object object : selection.toList()) {
            if (object instanceof JamTarget)
                buildJamTarget((JamTarget) object);
            if (object instanceof IContainer)
                recursiveBuild(object);
        }
    }

    @SuppressWarnings("deprecation")
    private void buildJamTarget(final JamTarget target) {
        // showMessage("building " + target.getName());
        final Shell shell = viewer.getControl().getShell();
        try {
            final IMakeTarget makeTarget = MakeCorePlugin.getDefault()
                    .getTargetManager().createTarget(
                            target.getFolder().getProject(), target.getName(),
                            MakeTargetAction.getBuilderID(target.getFolder()));
            makeTarget.setContainer(target.getFolder());
            if (arguments != null)
                makeTarget.setBuildArguments(makeTarget.getBuildArguments()
                        + " " + arguments);
            makeTarget.setBuildArguments(makeTarget.getBuildArguments() + " "
                    + dynamicOptions);
            makeTarget.setBuildTarget(target.getTargetName());
            TargetBuild.buildTargets(shell, new IMakeTarget[] { makeTarget });
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    static private String getBuilderID(final IContainer container) {
        final IMakeTargetManager fTargetManager = MakeCorePlugin.getDefault()
                .getTargetManager();
        final String[] id = fTargetManager.getTargetBuilders(container
                .getProject());
        if (id.length == 0) {
            final String resource = MakeUIPlugin
                    .getResourceString("MakeTargetDialog.exception.noTargetBuilderOnProject");
            final Status status = new Status(IStatus.ERROR, MakeUIPlugin
                    .getUniqueIdentifier(), -1, resource, null);
            throw new RuntimeException(new CoreException(status));
        }
        return id[0];
    }
}
