/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class JamfileTargetsView extends ViewPart {
    private org.eclipse.swt.widgets.Text optionsField;
    private TreeViewer viewer;
    private DrillDownAdapter drillDownAdapter;
    private Action rebuildAction;
    private Action cleanAction;
    private Action buildAction;

    static List<Object> getOpenProjects(final IWorkspaceRoot root) {
        List<Object> result = new ArrayList<Object>();
        for (IProject project : root.getProjects())
            if (project.isOpen())
                result.add(project);
        return result;
    }

    /**
     * The constructor.
     */
    public JamfileTargetsView() {
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(final Composite parent) {
        createTextField(parent);
        createTreeView(parent);
        drillDownAdapter = new DrillDownAdapter(viewer);
        parent.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));

        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new NameSorter());
        viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private void createTreeView(final Composite parent) {
        final Composite viewerParent = new Composite(parent, 0);
        viewerParent.setLayout(new FillLayout());
        final GridData data2 = new GridData();
        data2.horizontalAlignment = GridData.FILL;
        data2.grabExcessHorizontalSpace = true;
        data2.verticalAlignment = GridData.FILL;
        data2.grabExcessVerticalSpace = true;
        viewerParent.setLayoutData(data2);

        viewer = new TreeViewer(viewerParent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL);
    }

    private void createTextField(final Composite parent) {
        optionsField = new Text(parent, SWT.SINGLE);
        optionsField
                .setToolTipText("Specify additional options here. They will be passed to BJam.");
        final GridData data1 = new GridData();
        data1.horizontalAlignment = GridData.FILL;
        data1.grabExcessHorizontalSpace = true;
        optionsField.setLayoutData(data1);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(final IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(rebuildAction);
        manager.add(new Separator());
        manager.add(cleanAction);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(buildAction);
        manager.add(rebuildAction);
        manager.add(cleanAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(buildAction);
        manager.add(rebuildAction);
        manager.add(cleanAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
    }

    private void makeActions() {
        final Object dynamicOptions = new Object() {
            @Override
            public String toString() {
                return getOptions();
            }
        };

        buildAction = new MakeTargetAction(viewer, dynamicOptions);
        buildAction.setText("build");
        buildAction.setToolTipText("update target as needed");
        buildAction.setImageDescriptor(MakeUIImages.DESC_BUILD_TARGET);

        rebuildAction = new MakeTargetAction(viewer, "-a", dynamicOptions);
        rebuildAction.setText("rebuild");
        rebuildAction.setToolTipText("forces a rebuild of all files");
        MakeUIImages.setImageDescriptors(rebuildAction, "tool16",
                MakeUIImages.IMG_TOOLS_MAKE_TARGET_BUILD);

        cleanAction = new MakeTargetAction(viewer, "--clean", dynamicOptions);
        cleanAction.setText("clean");
        cleanAction.setToolTipText("clean this target");
        MakeUIImages.setImageDescriptors(cleanAction, "tool16",
                MakeUIImages.IMG_TOOLS_MAKE_TARGET_DELETE);
    }

    public String getOptions() {
        return optionsField.getText();
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                final ISelection selection = viewer.getSelection();
                final Object obj = ((IStructuredSelection) selection)
                        .getFirstElement();
                if (obj instanceof IContainer) {
                    if (viewer.getExpandedState(obj))
                        viewer.collapseToLevel(obj, 1);
                    else
                        viewer.expandToLevel(obj, 1);
                } else
                    buildAction.run();
            }
        });
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}
