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
	public static final String OPTION_NAME = "name";
	public static final String OPTION_VALUES = "values";
	public static final String OPTION_CATEGORY = "category";

	List<String> fCategories = new ArrayList<>();
	Map<String, List<String>> fCategoryItems = new HashMap<>();
	Map<String, Set<String>> fOptionValues = new HashMap<>();

	public Options(List<Map<String, Object>> options) {
		for (Map<String, Object> option : options) {
			Object tmp = option.get(OPTION_NAME);
			if (tmp != null && tmp instanceof String) {
				String name = (String) tmp;
				tmp = option.get(OPTION_CATEGORY);
				String category;
				if (tmp != null && tmp instanceof String) {
					category = (String) tmp;
				} else {
					category = getDefaultTarget(name);
				}
				if (!fCategories.contains(category))
					fCategories.add(category);
				tmp = option.get(OPTION_VALUES);
				if (tmp != null && tmp instanceof List) {
					List<String> values = (List<String>) tmp;
					if (!fCategoryItems.containsKey(category)) {
						fCategoryItems.put(category, new ArrayList<String>());
					}
					fCategoryItems.get(category).add(name);
					fOptionValues.put(name, new HashSet<String>(values));
				}
			}
		}
	}

	private String getDefaultTarget(String optionName) {
		switch (optionName) {
		case "variant":
		case "toolset":
		case "target-os":
		case "architecture":
			return "General Settings";
		case "optimization":
			return "Compiler Settings";
		case "link":
			return "Linker Settings";
		}
		return "";
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
}
