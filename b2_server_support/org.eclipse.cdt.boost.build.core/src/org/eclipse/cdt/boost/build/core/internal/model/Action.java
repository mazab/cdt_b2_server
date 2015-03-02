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

public class Action {
	private Event fStarted;
	private Event fFinished;
	private List<Event> fOutput = new ArrayList<>();

	public Action(Event started) {
		fStarted = started;
	}

	public Event getStartedEvent() {
		return fStarted;
	}

	public Event getFinishedEvent() {
		return fFinished;
	}

	public List<Event> getOutput() {
		return fOutput;
	}

	public void setFinishedEvent(Event finished) {
		fFinished = finished;
	}

	public void addOutput(Event output) {
		fOutput.add(output);
	}
}
