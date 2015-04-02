/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core.internal.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.eclipse.cdt.boost.build.core.BoostBuildPlugin;
import org.eclipse.cdt.boost.build.core.internal.model.Action;
import org.eclipse.cdt.boost.build.core.internal.model.Event;
import org.eclipse.cdt.boost.build.core.internal.model.Request;
import org.eclipse.cdt.boost.build.core.internal.model.Response;
import org.eclipse.cdt.boost.build.core.internal.model.Session;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.Bundle;

public class B2Process {
	private static final String B2_BIN_NAME = "b2";
	private static final String[] DAEMON_MODE_ARGS = { "--server", "--python", "-a", "-d0" };
	private static String b2BinPath = null;
	private OutputStreamWriter fOut;
	private BufferedReader fIn;
	private Session fLastSession;

	private B2Process(Process process) {
		fOut = new OutputStreamWriter(process.getOutputStream());
		fIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
	}

	private static String getPlatformSpecificB2FileName() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			return B2_BIN_NAME + ".exe";
		}
		return B2_BIN_NAME;
	}

	private static String getB2BinPath() {
		if (b2BinPath == null) {
			String bundleName = BoostBuildPlugin.PLUGIN_ID + "." + Platform.getOS() + "." + Platform.getOSArch();
			Bundle bundle = Platform.getBundle(bundleName);
			URL url = FileLocator.find(bundle, new Path("/os/bin/" + getPlatformSpecificB2FileName()), null);
			if (url != null) {
				try {
					url = FileLocator.resolve(url);
				} catch (IOException e) {
					CCorePlugin.log(e);
					b2BinPath = getPlatformSpecificB2FileName();
				}
				b2BinPath = (new Path(url.getFile())).toOSString();
			}
		}
		return b2BinPath;
	}

	public static B2Process invokeProcess(IProject project) {
		try {
			B2ProcessLauncher rcl = new B2ProcessLauncher();
			String b2Bin;
			IPath path;
			if (project.getLocation() == null) { // Remote project
				b2Bin = B2_BIN_NAME;
				path = project.getFullPath();
			} else {
				b2Bin = getB2BinPath();
				path = project.getLocation();
			}
			Process process = rcl.execute(new Path(b2Bin), DAEMON_MODE_ARGS, null, project, path, null);
			B2Process b2 = new B2Process(process);
			return b2;
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * The valid output line should be a JSON object
	 * 
	 * @return
	 */
	private static boolean isValidB2OutputLine(String line) {
		JSONObject obj = null;
		try {
			obj = (JSONObject) JSONValue.parse(line);
			if ("request".equals(obj.get("type"))) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return obj != null;
	}

	public String readLine() {
		try {
			while (true) {
				String line = fIn.readLine();
				if (isValidB2OutputLine(line))
					return line;
			}
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public void writeLine(String line) {
		try {
			fOut.write(line + "\n");
			fOut.flush();
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
	}

	public Response invokeRequest(Request request) {
		writeLine(request.toString());
		Response response = null;
		if (request.isRequireResopnse()) {
			response = Response.parseResponse(readLine());
		} else {
			fLastSession = new Session();
			fLastSession.setRequest(request);
		}
		return response;
	}

	public Event waitForEvent() {
		if (fLastSession != null) {
			Event e;
			// TODO remove after fixing the issue with finish event
			//Action action  = fLastSession.getLastAction();
			//if (action != null && action.getFinishedEvent() != null && 
			//		(action.getStartedEvent().getActionName().contains("link") || action.getFinishedEvent().getExitStatus() != 0)) {
			//	e = Event.parseEvent("{\"exit-status\": " + action.getFinishedEvent().getExitStatus() + ", \"type\": \"event\", \"event\": \"build-finished\"}");
			//} else {
				e = Event.parseEvent(readLine());
			//}
			boolean result = fLastSession.handleEvent(e);
			if (result)
				return e;
		}
		return null;
	}

	public Session getLastSession() {
		return fLastSession;
	}
}
