/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * A set of Utils to store/retrieve project info needed by the editor.
 */
public class ProjectSettingsPersistenceUtils {
	public static final String PERSISTENCE_UNIT_ID = BoostBuildPlugin.PLUGIN_ID;
	public static final String PERSISTENCE_NODE_ID = "project";

	/**
	 * Store the project info in the project persistence storage.
	 * 
	 * @param project
	 *            The C project to deal with.
	 * @param info
	 *            A map contains the info to store.
	 * @return true if storing succeeded and false otherwise.
	 */
	public static boolean storeProjectInfo(ICProjectDescription projectDesc, String name, String value) {
		ICStorageElement root = getProjectStorageElement(projectDesc);
		if (root != null) {
			root.setAttribute(name, value);
			try {
				CoreModel.getDefault().setProjectDescription(projectDesc.getProject(), projectDesc);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			return true;
		}
		return false;
	}

	public static String getProjectInfo(IProject project, String infoId) {
		String ret = null;
		ICStorageElement root = getProjectStorageElement(CoreModel.getDefault().getProjectDescription(project));
		if (root != null) {
			ret = root.getAttribute(infoId);
		}
		return ret;
	}

	public static Map<String, String> getAllProjectInfo(IProject project) {
		Map<String, String> ret = new HashMap<>();
		ICStorageElement root = getProjectStorageElement(CoreModel.getDefault().getProjectDescription(project));
		if (root != null) {
			String[] ids = root.getAttributeNames();
			for (String id : ids) {
				ret.put(id, root.getAttribute(id));
			}
		}
		return ret;
	}

	/**
	 * @return The project storage element that has
	 *         PERSISTENCE_UNIT_ID.PERSISTENCE_NODE_ID or null.
	 */
	private static ICStorageElement getProjectStorageElement(ICProjectDescription pDesc) {
		if (pDesc != null) {
			// Get storage element.
			ICStorageElement root;
			try {
				root = pDesc.getStorage(PERSISTENCE_UNIT_ID, true);
				if (root != null) {
					ICStorageElement[] children = root.getChildrenByName(PERSISTENCE_NODE_ID);
					if (children.length == 1) {
						return children[0];
					} else if (children.length == 0) {
						if (!pDesc.isReadOnly()) {
							return root.createChild(PERSISTENCE_NODE_ID);
						}
					}
				}
			} catch (CoreException e) {
				// Ignore
			}
		}
		return null;
	}
}
