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

public class Session {
	private Request fRequest;
	private Map<Long, Action> fActions = new HashMap<>();
	private List<Long> fOrderedActionsTokens = new ArrayList<>();
	private Long fLastActionToken;
	private Event fFinished;

	public Request getRequest() {
		return fRequest;
	}

	public Event getFinishedEvent() {
		return fFinished;
	}

	public Action getAction(Long token) {
		return fActions.get(token);
	}

	public List<Long> getOrderedActionsTokensList() {
		return fOrderedActionsTokens;
	}

	public void setFinishedEvent(Event finishedEvent) {
		fFinished = finishedEvent;
	}

	public void setRequest(Request fRequest) {
		this.fRequest = fRequest;
	}

	public void addAction(Action action) {
		Long token = action.getStartedEvent().getToken();
		fActions.put(token, action);
		fOrderedActionsTokens.add(token);
		fLastActionToken = token;
	}

	public Action getLastAction() {
		return fActions.get(fLastActionToken);
	}

	public boolean handleEvent(Event e) {
		Action a;
		switch (e.getEventType()) {
		case Event.EVENT_TYPE_ACTION_STARTED:
			a = new Action(e);
			addAction(a);
			break;
		case Event.EVENT_TYPE_ACTION_FINISHED:
			a = getAction(e.getToken());
			if (a != null) {
				a.setFinishedEvent(e);
			} else {
				return false;
			}
			break;
		case Event.EVENT_TYPE_ACTION_OUTPUT:
			a = getAction(e.getToken());
			if (a != null) {
				a.addOutput(e);
			} else {
				return false;
			}
			break;
		case Event.EVENT_TYPE_FINISHED:
			setFinishedEvent(e);
			break;
		}
		return true;
	}
}
