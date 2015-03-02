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
import org.json.simple.JSONArray;

public class B2ProcessManager {
	private static final Map<String, B2Process> B2_PROCESS_HOLDER = new HashMap<>();
	private static final Map<String, Options> B2_PROPERTIES_HOLDER = new HashMap<>();

	private static B2Process createAndPersistBuilderProcess(IPath location) {
		try {
			B2Process process = B2Process.invokeProcess(location.toOSString());
			B2_PROCESS_HOLDER.put(location.toOSString(), process);
			return process;
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public static B2Process getBuilderProcess(IProject project) {
		IPath location = project.getLocation();
		String locationStr = location.toOSString();
		return B2_PROCESS_HOLDER.containsKey(locationStr) ? B2_PROCESS_HOLDER.get(locationStr)
				: createAndPersistBuilderProcess(location);
	}

	public static Options getBuildProperties(IProject project) {
		IPath location = project.getLocation();
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
}
