/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Set;

import org.eclipse.cdt.boost.build.core.B2ProcessManager;
import org.eclipse.cdt.boost.build.core.Options;
import org.eclipse.cdt.boost.build.core.ProjectSettingsPersistenceUtils;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PropertyPage;

public class B2PropertiesPage extends PropertyPage implements PropertyChangeListener {

	private IProject fProject;
	private Composite fComposite;
	private ArrayList<Font> fFontList = new ArrayList<>();

	@Override
	protected Control createContents(Composite parent) {
		fProject = (IProject) getElement().getAdapter(IResource.class);
		final Options options = B2ProcessManager.getBuildProperties(fProject);
		fComposite = new Composite(parent, SWT.NONE);
		resetPage(options);
		return fComposite;
	}

	private void updatePageForCategory(final String category, final Options options) {
		cleanParentComposite();

		Composite header = new Composite(fComposite, SWT.NONE);
		header.setLayout(new GridLayout(3, false));
		Link l = new Link(header, SWT.NONE);
		l.setText("<A>Home</A>");
		l.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetPage(options);
			}
		});

		FontData[] fD = l.getFont().getFontData();
		fD[0].setHeight(12);
		Font boldFont = new Font(l.getDisplay(), fD[0]);
		l.setFont(boldFont);
		fFontList.add(boldFont);

		Label seperator = new Label(header, SWT.NONE);
		seperator.setText("/");
		seperator.setFont(boldFont);

		Link title = new Link(header, SWT.NONE);
		title.setText("<A>" + category + "</A>");
		title.setFont(boldFont);

		Composite opts = new Composite(fComposite, SWT.NONE);
		opts.setLayout(new GridLayout(2, false));
		opts.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		for (String option : options.getCategoryItems(category)) {
			final Label lOption = new Label(opts, SWT.NONE);
			lOption.setText(option);
			final Combo list = new Combo(opts, SWT.DROP_DOWN | SWT.READ_ONLY);
			list.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Set<String> vals = options.getOptionValues(option);
			list.setItems(vals.toArray(new String[vals.size()]));
			String storedVal = getOption(option);
			if (storedVal != null && vals.contains(storedVal))
				list.select(list.indexOf(storedVal));
			else {
				list.select(0);
				storeOption(option, list.getText());
			}
			list.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					storeOption(lOption.getText(), list.getText());
				}
			});
		}
		fComposite.layout();
	}

	private String getOption(String name) {
		return ProjectSettingsPersistenceUtils.getProjectInfo(fProject, name);
	}

	private void storeOption(String name, String val) {
		ICProjectDescriptionManager pdMgr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription projDesc;
		projDesc = pdMgr.getProjectDescription(fProject);
		ProjectSettingsPersistenceUtils.storeProjectInfo(projDesc, name, val);
	}

	private void resetPage(final Options options) {
		cleanParentComposite();

		Font boldFont = null;
		Font smallFont = null;
		for (final String category : options.getCategories()) {
			Link link = new Link(fComposite, SWT.NONE);
			link.setText("<A>" + category + "</A>");
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updatePageForCategory(category, options);
				}
			});

			if (boldFont == null) {
				FontData[] fD = link.getFont().getFontData();
				fD[0].setHeight(12);
				boldFont = new Font(link.getDisplay(), fD[0]);
				fFontList.add(boldFont);
			}
			link.setFont(boldFont);

			Label seperator = new Label(fComposite, SWT.NONE);
			seperator.setLayoutData(new GridData());
			if (smallFont == null) {
				FontData[] fD = link.getFont().getFontData();
				fD[0].setHeight(8);
				smallFont = new Font(link.getDisplay(), fD[0]);
				fFontList.add(smallFont);
			}
			seperator.setFont(smallFont);
		}
		fComposite.layout();
	}

	private void cleanParentComposite() {
		for (Control child : fComposite.getChildren())
			child.dispose();
		fComposite.setLayout(new GridLayout(1, false));
		fComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	@Override
	public void dispose() {
		for (Font font : fFontList)
			font.dispose();
		super.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
	}

}
