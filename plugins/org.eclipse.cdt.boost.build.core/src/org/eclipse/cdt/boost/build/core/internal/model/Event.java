/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Event {
	public static final String EVENT_TYPE_ACTION_STARTED = "build-action-started";
	public static final String EVENT_TYPE_ACTION_FINISHED = "build-action-finished";
	public static final String EVENT_TYPE_ACTION_OUTPUT = "build-action-output";
	public static final String EVENT_TYPE_FINISHED = "build-finished";

	public static final String PROPERTY_NAME_INCLUDEPATH = "include";
	public static final String PROPERTY_NAME_DEFINE = "define";
	public static final String PROPERTY_NAME_LIBPATH = "library-path";

	public static final String ACTION_FINISHED_MESSAGE = "\tDone";
	public static final String FINISH_MESSAGE = "** Build Finished **\n";

	private String fEventType;
	private String fActionKind;
	private String fActionName;
	private Long fToken;
	private String fContent;
	private Long fExitStatus;
	private Boolean fSuccess;
	private List<String> fSources;
	private List<String> fTargets;
	private Map<String, List<String>> fProperties;

	public static Event parseEvent(String event) {
		return new Event(event);
	}

	private Event(String event) {
		JSONObject responseObj = (JSONObject) JSONValue.parse(event);
		if ("event".equals(responseObj.get("type"))) {
			fEventType = (String) responseObj.get("event");
			fActionKind = (String) responseObj.get("action-kind");
			fActionName = (String) responseObj.get("action-name");
			Object tmp = responseObj.get("token");
			if (tmp != null)
				fToken = (Long) tmp;
			fContent = (String) responseObj.get("output");
			tmp = responseObj.get("exit-status");
			if (tmp != null)
				fExitStatus = (Long) tmp;
			tmp = responseObj.get("success");
			if (tmp != null)
				fSuccess = Boolean.parseBoolean(((String) tmp));
			fSources = (List<String>) responseObj.get("sources");
			fTargets = (List<String>) responseObj.get("targets");
			JSONObject properties = (JSONObject) responseObj.get("properties");
			if (properties != null) {
				fProperties = new HashMap<String, List<String>>();
				for (Object property : properties.keySet()) {
					Object val = properties.get(property);
					List<String> vals = null;
					if (val instanceof String) {
						vals = new ArrayList<>();
						vals.add((String) val);
					} else if (val instanceof JSONArray) {
						vals = (JSONArray) val;
					}
					fProperties.put((String) property, vals);
				}
			}
		}
	}

	public String getEventType() {
		return fEventType;
	}

	public String getActionKind() {
		return fActionKind;
	}

	public String getActionName() {
		return fActionName;
	}

	public Long getToken() {
		return fToken;
	}

	public String getContent() {
		return fContent;
	}

	public Long getExitStatus() {
		return fExitStatus;
	}

	public Boolean getSuccess() {
		return fSuccess;
	}

	public List<String> getSources() {
		return fSources;
	}

	public List<String> getTargets() {
		return fTargets;
	}

	public Map<String, List<String>> getProperties() {
		return fProperties;
	}

	public String getDisplayString() {
		StringBuilder sb = new StringBuilder();
		switch (fEventType) {
		case EVENT_TYPE_ACTION_STARTED:
			sb.append("Invoking ");
			sb.append(fActionName);
			sb.append("...\n");
			sb.append("\tSources:\t");
			sb.append(fSources.toString());
			sb.append("\n\tTargets:\t");
			sb.append(fTargets.toString());
			sb.append("\n\tProperties...");
			for (String property : fProperties.keySet()) {
				sb.append("\n\t\t");
				sb.append(property);
				sb.append(":\t");
				sb.append(fProperties.get(property).toString());
			}
			sb.append("\n\t...");
			break;
		case EVENT_TYPE_ACTION_OUTPUT:
			sb.append("\t");
			sb.append(fContent.replace("\n", "\n\t"));
			sb.append("\n");
			break;
		case EVENT_TYPE_ACTION_FINISHED:
			sb.append(ACTION_FINISHED_MESSAGE);
			sb.append("\n");
			break;
		case EVENT_TYPE_FINISHED:
			sb.append(FINISH_MESSAGE);
			if (fContent != null) {
				sb.append(fContent);
				sb.append("\n");
			}
			if (fSuccess != null && !fSuccess.booleanValue()) {
				sb.append("Build Failed\n");
			} else {
				sb.append("Build Succeeded\n");
			}
			break;
		}
		return sb.toString();
	}
}
