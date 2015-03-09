/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.ui.internal.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;

public class B2BuildWizard extends AbstractCWizard {
	public static final String BOOST_BUILD_PROJECT_TYPE = "org.eclipse.cdt.build.core.projectType";
	public static final String BOOST_BUILD_TOOLCHAIN_ID = "org.eclipse.cdt.boost.build.core.toolchain"; //$NON-NLS-1$

	@Override
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(MBSWizardHandler.ARTIFACT);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		Arrays.sort(vs, BuildListComparator.getInstance());
		ArrayList<EntryDescriptor> items = new ArrayList<EntryDescriptor>();

		// look for project types that have a toolchain based on the boost
		// toolchain
		// and if so, add an entry for the project type.
		SortedMap<String, IProjectType> sm = ManagedBuildManager.getExtensionProjectTypeMap();
		for (Map.Entry<String, IProjectType> e : sm.entrySet()) {
			IProjectType pt = e.getValue();
			if (BOOST_BUILD_PROJECT_TYPE.equals(pt.getBaseId())) {
				MBSWizardHandler h = new MBSWizardHandler(pt, parent, wizard);
				IToolChain[] tcs = ManagedBuildManager.getExtensionToolChains(pt);
				for (int i = 0; i < tcs.length; i++) {
					IToolChain t = tcs[i];

					IToolChain parent = t;
					while (parent.getSuperClass() != null) {
						parent = parent.getSuperClass();
					}

					if (!parent.getId().equals(BOOST_BUILD_TOOLCHAIN_ID))
						continue;

					if (t.isSystemObject())
						continue;

					h.addTc(t);
				}

				if (h.getToolChainsCount() > 0)
					items.add(new EntryDescriptor(pt.getId(), null, pt.getName(), true, h, null));
			}
		}
		return (EntryDescriptor[]) items.toArray(new EntryDescriptor[items.size()]);
	}
}
