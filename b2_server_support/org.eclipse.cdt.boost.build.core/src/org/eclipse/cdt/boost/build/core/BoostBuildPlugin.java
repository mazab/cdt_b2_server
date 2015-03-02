/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BoostBuildPlugin implements BundleActivator {
	public static final String PLUGIN_ID = "org.eclipse.cdt.boost.build.core";

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		BoostBuildPlugin.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		BoostBuildPlugin.context = null;
	}

}
