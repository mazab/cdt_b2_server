/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.boost.build.core.internal.builder.B2Process;
import org.eclipse.cdt.boost.build.core.internal.model.Request;
import org.eclipse.cdt.boost.build.core.internal.model.Response;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class B2ProcessManager {
	private static final Map<String, B2Process> B2_PROCESS_HOLDER = new HashMap<>();
	private static final Map<String, Options> B2_PROPERTIES_HOLDER = new HashMap<>();

	private static B2Process createAndPersistBuilderProcess(IProject project) {
		try {
			B2Process process = B2Process.invokeProcess(project);
			B2_PROCESS_HOLDER.put(project.getFullPath().toOSString(), process);
			return process;
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public static B2Process getBuilderProcess(IProject project) {
		IPath location = project.getFullPath();
		String locationStr = location.toOSString();
		return B2_PROCESS_HOLDER.containsKey(locationStr) ? B2_PROCESS_HOLDER.get(locationStr)
				: createAndPersistBuilderProcess(project);
	}

	public static Options getBuildProperties(IProject project) {
		IPath location = project.getFullPath();
		String locationStr = location.toOSString();
		if (B2_PROPERTIES_HOLDER.containsKey(locationStr))
			return B2_PROPERTIES_HOLDER.get(locationStr);
		B2Process process = getBuilderProcess(project);
		Response response = process.invokeRequest(new Request(Request.REQUEST_TYPE_GET, null, null, "properties"));
		if (response != null) {
			List<Map<String, Object>> properties = response.getResponse();
			Options opts = new Options(properties);
			B2_PROPERTIES_HOLDER.put(locationStr, opts);
			return opts;
		}
		return null;
	}

	public static Map<String, Object> getApplicability(IProject project, Map<String, String> properties) {
		B2Process process = getBuilderProcess(project);
		Response response = process.invokeRequest(new Request(Request.REQUEST_TYPE_GET_APPLICABILITY, null, properties, "properties"));
		if (response != null) {
			Map<String, Object> ret = response.getResponse().get(0);
			return ret;
		}
		return null;
	}
}
