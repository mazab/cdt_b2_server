/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Options {
	public static final String NAME = "name";
	public static final String VALUES = "values";
	public static final String PROPERTIES = "properties";
	public static final String APPLICABLE = "applicable";

	List<String> fCategories = new ArrayList<>();
	Map<String, List<String>> fCategoryItems = new HashMap<>();
	Map<String, Set<String>> fOptionValues = new HashMap<>();
	Map<String, Boolean> fOptionApplicability = new HashMap<>();

	public Options(List<Map<String, Object>> categories) {
		for (Map<String, Object> category : categories) {
			Object tmp = category.get(NAME);
			if (tmp != null && tmp instanceof String) {
				String name = (String) tmp;
				tmp = category.get(PROPERTIES);
				if (tmp != null && tmp instanceof List) {
					List options = (List) tmp;
					ArrayList<String> categoryOpts = new ArrayList<String>();
					fCategories.add(name);
					fCategoryItems.put(name, categoryOpts);
					for (Object option : options) {
						Map<String, Object> optMap = (Map<String, Object>) option;
						tmp = optMap.get(NAME);
						if (tmp != null && tmp instanceof String) {
							String optName = (String) tmp;
							tmp = optMap.get(APPLICABLE);
							boolean applicable = true;
							if (tmp != null && tmp instanceof Boolean) {
								applicable = ((Boolean) tmp).booleanValue();
							}
							fOptionApplicability.put(optName, applicable);
							tmp = optMap.get(VALUES);
							if (tmp != null && tmp instanceof List) {
								List values = (List) tmp;
								categoryOpts.add(optName);
								fOptionValues.put(optName, new HashSet<String>(values));
							}
						}
					}
				}
			}
		}
	}

	public List<String> getCategories() {
		return fCategories;
	}

	public List<String> getCategoryItems(String category) {
		return fCategoryItems.get(category);
	}

	public Set<String> getOptionValues(String option) {
		return fOptionValues.get(option);
	}

	public Boolean getOptionApplicability(String option) {
		return fOptionApplicability.get(option);
	}
}
