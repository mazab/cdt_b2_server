/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core.internal.language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

public class B2ErrorParser implements IErrorParser {
	private static Pattern error = Pattern.compile("([^:]+):([0-9]+):([^:]+):(.*)");

	@Override
	public boolean processLine(String line, ErrorParserManager eoParser) {
		try {
			Matcher m = error.matcher(line);
			if (m.matches()) {
				String fileName = m.group(1); //$NON-NLS-1$
				IFile file = eoParser.findFileName(fileName);
				int num = Integer.parseInt(m.group(2));
				int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
				String severityStr = m.group(3).trim();
				if ("warning".equals(severityStr))
					severity = IMarkerGenerator.SEVERITY_WARNING;
				String desc = m.group(4);
				eoParser.generateMarker(file, num, desc, severity, null);
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return false;
	}
}
