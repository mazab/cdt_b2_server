/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.json.simple.JSONObject;

public class Request {
	public static final String REQUEST_TYPE_BUILD = "build";
	public static final String REQUEST_TYPE_CLEAN_BUILD = "clean-build";
	public static final String REQUEST_TYPE_GET = "get";
	public static final String REQUEST_TYPE_CANCEL = "cancel";

	private String fRequestType;
	private List<String> fTargets;
	private Map<String, String> fProperties;
	private String fPath;

	public Request(String requestType, List<String> targets, Map<String, String> properties, String path) {
		fRequestType = requestType;
		fTargets = targets;
		fProperties = properties;
		fPath = path;

	}

	public boolean isRequireResopnse() {
		return (REQUEST_TYPE_GET.equals(fRequestType));
	}

	@Override
	public String toString() {
		Map<String, Object> request = new HashMap<String, Object>();
		request.put("type", "request");
		request.put("request", fRequestType);
		if (fTargets != null)
			request.put("targets", fTargets);
		if (fProperties != null)
			request.put("properties", fProperties);
		if (fPath != null)
			request.put("path", fPath);
		try {
			return new JSONObject(request).toString();
		} catch (Exception e) {
			CCorePlugin.log("couldn't create request", e);
		}
		return null;
	}

	/*
	 * Initiating build request... project: hello1 targets: . properties...
	 * variant: debug ...
	 */
	public String getDisplayString() {
		StringBuilder sb = new StringBuilder();
		switch (fRequestType) {
		case REQUEST_TYPE_BUILD:
			sb.append("Initiating build request...");
			sb.append("\n\tTargets:\t");
			sb.append(fTargets.toString());
			sb.append("\n\tProperties...");
			for (String property : fProperties.keySet()) {
				sb.append("\n\t\t");
				sb.append(property);
				sb.append(":\t");
				sb.append(fProperties.get(property));
			}
			sb.append("\n\t...\n");
			break;
		case REQUEST_TYPE_CANCEL:
			sb.append("\nIntiating cancel request...\n");
			break;
		}
		return sb.toString();
	}
}
