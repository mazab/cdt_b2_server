/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Response {

	private String fResponseMessage;
	private List<Map<String, Object>> fResponse;

	public static Response parseResponse(String response) {
		return new Response(response);
	}

	private Response(String response) {
		JSONObject responseObj = (JSONObject) JSONValue.parse(response);
		if ("response".equals(responseObj.get("type"))) {
			if (responseObj.containsKey("response")) {
				Object r = responseObj.get("response");
				if (r instanceof List) {
					fResponse = (List<Map<String, Object>>) responseObj.get("response");
				} else {
					fResponse = new ArrayList<Map<String, Object>>();
					fResponse.add((Map<String, Object>) r);
				}
			}
		}
	}

	public List<Map<String, Object>> getResponse() {
		return fResponse;
	}

	public String getResponseMessage() {
		return fResponseMessage;
	}
}
