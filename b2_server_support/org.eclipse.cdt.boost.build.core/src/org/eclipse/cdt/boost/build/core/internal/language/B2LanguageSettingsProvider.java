/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core.internal.language;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.boost.build.core.B2ProcessManager;
import org.eclipse.cdt.boost.build.core.internal.builder.B2Process;
import org.eclipse.cdt.boost.build.core.internal.model.Action;
import org.eclipse.cdt.boost.build.core.internal.model.Event;
import org.eclipse.cdt.boost.build.core.internal.model.Session;
import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class B2LanguageSettingsProvider extends LanguageSettingsSerializableProvider implements ICBuildOutputParser {
	private static final Pattern ACTION_FINISHED_LINE_PATTERN = Pattern.compile(Event.ACTION_FINISHED_MESSAGE);
	private static final Pattern MACRO_PATTERN = Pattern.compile("([^\\s=\"']*)=([\"'])(.*?)\\2");
	// evaluates to "/${ProjName)/"
	private static final String PROJ_NAME_PREFIX = '/' + CdtVariableResolver
			.createVariableReference(CdtVariableResolver.VAR_PROJ_NAME) + '/';
	private static final String[] SUPPORTED_LANGUAGES = { "org.eclipse.cdt.core.g++", "org.eclipse.cdt.core.gcc" };

	private IProject fProject;
	private ICConfigurationDescription fCfgDescription;

	private static final EFSExtensionProvider efsProviderDefault = new EFSExtensionProvider() {
		final EFSExtensionManager efsManager = EFSExtensionManager.getDefault();

		@Override
		public String getPathFromURI(URI locationURI) {
			return efsManager.getPathFromURI(locationURI);
		}

		@Override
		public URI getLinkedURI(URI locationURI) {
			return efsManager.getLinkedURI(locationURI);
		}

		@Override
		public URI createNewURIFromPath(URI locationOnSameFilesystem, String path) {
			return efsManager.createNewURIFromPath(locationOnSameFilesystem, path);
		}

		@Override
		public String getMappedPath(URI locationURI) {
			return efsManager.getMappedPath(locationURI);
		}

		@Override
		public boolean isVirtual(URI locationURI) {
			return efsManager.isVirtual(locationURI);
		}

		@Override
		public URI append(URI baseURI, String extension) {
			return efsManager.append(baseURI, extension);
		}
	};

	@Override
	public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker)
			throws CoreException {
		fCfgDescription = cfgDescription;
		fProject = cfgDescription.getProjectDescription().getProject();
	}

	@Override
	public boolean processLine(String line) {
		Matcher m = ACTION_FINISHED_LINE_PATTERN.matcher(line);
		if (m.find()) {
			B2Process b2 = B2ProcessManager.getBuilderProcess(fProject);
			if (b2 != null) {
				Session session = b2.getLastSession();
				Action action = session.getLastAction();
				if (action != null && action.getFinishedEvent() != null
						&& action.getFinishedEvent().getExitStatus() == 0) {
					Map<String, List<String>> properties = action.getStartedEvent().getProperties();
					List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
					List<String> includePaths = new ArrayList<>();
					List<String> macros = new ArrayList<>();
					List<String> libPaths = new ArrayList<>();
					if (properties != null) {
						List<String> tmp = properties.get(Event.PROPERTY_NAME_INCLUDEPATH);
						if (tmp != null) {
							includePaths.addAll(tmp);
						}
						tmp = properties.get(Event.PROPERTY_NAME_LIBPATH);
						if (tmp != null) {
							libPaths.addAll(tmp);
						}
						tmp = properties.get(Event.PROPERTY_NAME_DEFINE);
						if (tmp != null) {
							macros.addAll(tmp);
						}
						URI baseURI = fProject.getLocationURI();
						for (String include : includePaths) {
							ICLanguageSettingEntry entry = createResolvedPathEntry(ICLanguageSettingEntry.INCLUDE_PATH,
									include, 0, baseURI);
							if (entry != null && !entries.contains(entry))
								entries.add(entry);
						}
						for (String lib : libPaths) {
							ICLanguageSettingEntry entry = createResolvedPathEntry(ICLanguageSettingEntry.LIBRARY_PATH,
									lib, 0, baseURI);
							if (entry != null && !entries.contains(entry))
								entries.add(entry);
						}
						for (String macro : macros) {
							Matcher m2 = MACRO_PATTERN.matcher(macro);
							if (m2.find()) {
								ICLanguageSettingEntry entry = createEntry(ICLanguageSettingEntry.MACRO, m2.group(1),
										m2.group(3), 0);
								if (entry != null && !entries.contains(entry))
									entries.add(entry);
							}
						}
						setSettingEntries(entries);
					}
				}
			}
		}
		return false;
	}

	@Override
	public void shutdown() {
		fProject = null;
		fCfgDescription = null;
	}

	/**
	 * Sets language settings entries for current configuration description,
	 * current resource and current language ID.
	 *
	 * @param entries
	 *            - language settings entries to set.
	 */
	protected void setSettingEntries(List<? extends ICLanguageSettingEntry> entries) {
		// TODO handle entries for specific sources.
		for (String lang : SUPPORTED_LANGUAGES) {
			setSettingEntries(fCfgDescription, fProject, lang, entries);
		}
	}

	/**
	 * Determine a language associated with the resource.
	 *
	 * @return language ID for the resource.
	 */
	protected String determineLanguage(IResource rc) {
		if (rc == null)
			return null;

		List<String> languageIds = LanguageSettingsManager.getLanguages(rc, fCfgDescription);
		if (languageIds.isEmpty())
			return null;

		return languageIds.get(0);
	}

	/**
	 * Determine resource in the workspace corresponding to the parsed resource
	 * name.
	 */
	private IResource findResource(String parsedResourceName) {
		if (parsedResourceName == null || parsedResourceName.isEmpty()) {
			return null;
		}

		IResource sourceFile = null;

		// try to find absolute path in the workspace
		if (sourceFile == null && new Path(parsedResourceName).isAbsolute()) {
			URI uri = org.eclipse.core.filesystem.URIUtil.toURI(parsedResourceName);
			sourceFile = findFileForLocationURI(uri, fProject, /* checkExistence */true);
		}

		// try path relative to the project
		if (sourceFile == null && fProject != null) {
			sourceFile = fProject.findMember(parsedResourceName);
		}

		return sourceFile;
	}

	/**
	 * Find file resource in the workspace for a given URI with a preference for
	 * the resource to reside in the given project.
	 */
	private static IResource findFileForLocationURI(URI uri, IProject preferredProject, boolean checkExistence) {
		IResource sourceFile = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource[] resources = root.findFilesForLocationURI(uri);
		for (IResource rc : resources) {
			if (!checkExistence || rc.isAccessible()) {
				if (rc.getProject().equals(preferredProject)) {
					sourceFile = rc;
					break;
				}
				if (sourceFile == null) {
					sourceFile = rc;
				}
			}
		}
		return sourceFile;
	}

	/**
	 * Resolve and create language settings path entry.
	 */
	private ICLanguageSettingEntry createResolvedPathEntry(int kind, String parsedPath, int flag, URI baseURI) {
		URI uri = determineMappedURI(parsedPath, baseURI);
		boolean isRelative = !new Path(parsedPath).isAbsolute();
		// is mapped something that is not a project root
		boolean isRemapped = baseURI != null && fProject != null && !baseURI.equals(fProject.getLocationURI());
		boolean presentAsRelative = isRelative || isRemapped;

		ICLanguageSettingEntry entry = resolvePathEntryInWorkspace(kind, uri, flag, presentAsRelative);
		if (entry != null) {
			return entry;
		}
		entry = resolvePathEntryInFilesystem(kind, uri, flag);
		if (entry != null) {
			return entry;
		}
		entry = resolvePathEntryInWorkspaceAsBestFit(kind, parsedPath, flag, presentAsRelative);
		if (entry != null) {
			return entry;
		}
		entry = resolvePathEntryInWorkspaceToNonexistingResource(kind, uri, flag, presentAsRelative);
		if (entry != null) {
			return entry;
		}
		entry = resolvePathEntryInFilesystemToNonExistingResource(kind, uri, flag);
		if (entry != null) {
			return entry;
		}
		return createEntry(kind, parsedPath, parsedPath, flag);
	}

	/**
	 * Try to map a resource on the file-system even if it does not exist and
	 * create a language settings entry for it.
	 */
	private ICLanguageSettingEntry resolvePathEntryInFilesystemToNonExistingResource(int kind, URI uri, int flag) {
		IPath location = getFilesystemLocation(uri);
		if (location != null) {
			return createEntry(kind, location.toString(), location.toString(), flag);
		}
		return null;
	}

	/**
	 * Determine URI on the local file-system considering possible mapping.
	 *
	 * @param pathStr
	 *            - path to the resource, can be absolute or relative
	 * @param baseURI
	 *            - base {@link URI} where path to the resource is rooted
	 * @return {@link URI} of the resource
	 */
	private URI determineMappedURI(String pathStr, URI baseURI) {
		URI uri = null;

		if (baseURI == null) {
			if (new Path(pathStr).isAbsolute()) {
				uri = resolvePathFromBaseLocation(pathStr, Path.ROOT);
			}
		} else if (baseURI.getScheme().equals(EFS.SCHEME_FILE)) {
			// location on the local file-system
			IPath baseLocation = org.eclipse.core.filesystem.URIUtil.toPath(baseURI);
			// careful not to use Path here but 'pathStr' as String as we want
			// to properly navigate symlinks
			uri = resolvePathFromBaseLocation(pathStr, baseLocation);
		}

		if (uri == null) {
			// if everything fails just wrap string to URI
			uri = org.eclipse.core.filesystem.URIUtil.toURI(pathStr);
		}
		return uri;
	}

	/**
	 * Try to map a resource in the workspace even if it does not exist and
	 * create a language settings entry for it.
	 */
	private ICLanguageSettingEntry resolvePathEntryInWorkspaceToNonexistingResource(int kind, URI uri, int flag,
			boolean isRelative) {
		if (uri != null && uri.isAbsolute()) {
			IResource rc = null;
			switch (kind) {
			case ICLanguageSettingEntry.INCLUDE_PATH:
			case ICLanguageSettingEntry.LIBRARY_PATH:
				rc = findContainerForLocationURI(uri, fProject, /* checkExistence */true);
				break;
			case ICLanguageSettingEntry.LIBRARY_FILE:
				rc = findFileForLocationURI(uri, fProject, /* checkExistence */true);
				break;
			}
			if (rc != null) {
				return createPathEntry(kind, rc, isRelative, flag);
			}
		}
		return null;
	}

	/**
	 * The manipulations here are done to resolve problems such as "../"
	 * navigation for symbolic links where "link/.." cannot be collapsed as it
	 * must follow the real file-system path.
	 * {@link java.io.File#getCanonicalPath()} deals with that correctly but
	 * {@link Path} or {@link URI} try to normalize the path which would be
	 * incorrect here. Another issue being resolved here is fixing drive letters
	 * in URI syntax.
	 */
	private static URI resolvePathFromBaseLocation(String pathStr0, IPath baseLocation) {
		String pathStr = pathStr0;
		if (baseLocation != null && !baseLocation.isEmpty()) {
			pathStr = pathStr.replace(File.separatorChar, '/');
			String device = new Path(pathStr).getDevice();
			if (device == null || device.equals(baseLocation.getDevice())) {
				if (device != null && device.length() > 0) {
					pathStr = pathStr.substring(device.length());
				}

				baseLocation = baseLocation.addTrailingSeparator();
				if (pathStr.startsWith("/")) { //$NON-NLS-1$
					pathStr = pathStr.substring(1);
				}
				pathStr = baseLocation.toString() + pathStr;
			}
		}

		try {
			File file = new File(pathStr);
			file = file.getCanonicalFile();
			URI uri = file.toURI();
			if (file.exists()) {
				return uri;
			}

			IPath path0 = new Path(pathStr0);
			if (!path0.isAbsolute()) {
				return uri;
			}

			String device = path0.getDevice();
			if (device == null || device.isEmpty()) {
				// Avoid spurious adding of drive letters on Windows
				pathStr = path0.setDevice(null).toString();
			} else {
				// On Windows "C:/folder/" -> "/C:/folder/"
				if (pathStr.charAt(0) != IPath.SEPARATOR) {
					pathStr = IPath.SEPARATOR + pathStr;
				}
			}

			return new URI(uri.getScheme(), uri.getAuthority(), pathStr, uri.getQuery(), uri.getFragment());

		} catch (Exception e) {
			// if error will leave it as is
			ManagedBuilderCorePlugin.log(e);
		}

		return org.eclipse.core.filesystem.URIUtil.toURI(pathStr);
	}

	public static ICLanguageSettingEntry createEntry(int kind, String name, String value, int flag) {
		return (ICLanguageSettingEntry) CDataUtil.createEntry(kind, name, value, null, flag);
	}

	/**
	 * Find a resource on the file-system and create a language settings entry
	 * for it.
	 */
	private ICLanguageSettingEntry resolvePathEntryInFilesystem(int kind, URI uri, int flag) {
		IPath location = getFilesystemLocation(uri);
		if (location != null) {
			String loc = location.toString();
			if (new File(loc).exists()) {
				return createEntry(kind, loc, loc, flag);
			}
		}
		return null;
	}

	/**
	 * Find a best fit for the resource in the workspace and create a language
	 * settings entry for it.
	 */
	private ICLanguageSettingEntry resolvePathEntryInWorkspaceAsBestFit(int kind, String parsedPath, int flag,
			boolean isRelative) {
		IResource rc = findBestFitInWorkspace(parsedPath);
		if (rc != null) {
			return createPathEntry(kind, rc, isRelative, flag);
		}
		return null;
	}

	/**
	 * Determine which resource in workspace is the best fit to parsedName
	 * passed.
	 */
	private IResource findBestFitInWorkspace(String parsedName) {
		Set<String> referencedProjectsNames = new LinkedHashSet<String>();
		if (fCfgDescription != null) {
			Map<String, String> refs = fCfgDescription.getReferenceInfo();
			referencedProjectsNames.addAll(refs.keySet());
		}

		IPath path = new Path(parsedName);
		if (path.equals(new Path(".")) || path.equals(new Path(".."))) { //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

		// prefer current project
		if (fProject != null) {
			List<IResource> result = findPathInFolder(path, fProject);
			int size = result.size();
			if (size == 1) { // found the one
				return result.get(0);
			} else if (size > 1) { // ambiguous
				return null;
			}
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		// then prefer referenced projects
		if (referencedProjectsNames.size() > 0) {
			IResource rc = null;
			for (String prjName : referencedProjectsNames) {
				IProject prj = root.getProject(prjName);
				if (prj.isOpen()) {
					List<IResource> result = findPathInFolder(path, prj);
					int size = result.size();
					if (size == 1 && rc == null) {
						rc = result.get(0);
					} else if (size > 0) {
						// ambiguous
						rc = null;
						break;
					}
				}
			}
			if (rc != null) {
				return rc;
			}
		}

		// then check all other projects in workspace
		IProject[] projects = root.getProjects();
		if (projects.length > 0) {
			IResource rc = null;
			for (IProject prj : projects) {
				if (!prj.equals(fProject) && !referencedProjectsNames.contains(prj.getName()) && prj.isOpen()) {
					List<IResource> result = findPathInFolder(path, prj);
					int size = result.size();
					if (size == 1 && rc == null) {
						rc = result.get(0);
					} else if (size > 0) {
						// ambiguous
						rc = null;
						break;
					}
				}
			}
			if (rc != null) {
				return rc;
			}
		}

		// not found or ambiguous
		return null;
	}

	/**
	 * Find all resources in the folder which might be represented by relative
	 * path passed.
	 */
	private static List<IResource> findPathInFolder(IPath path, IContainer folder) {
		List<IResource> paths = new ArrayList<IResource>();
		IResource resource = folder.findMember(path);
		if (resource != null) {
			paths.add(resource);
		}

		try {
			for (IResource res : folder.members()) {
				if (res instanceof IContainer) {
					paths.addAll(findPathInFolder(path, (IContainer) res));
				}
			}
		} catch (CoreException e) {
			// ignore
		}

		return paths;
	}

	/**
	 * Get location on the local file-system considering possible mapping by EFS
	 * provider. See {@link EFSExtensionManager}.
	 */
	private IPath getFilesystemLocation(URI uri) {
		if (uri == null)
			return null;

		String pathStr = efsProviderDefault.getMappedPath(uri);
		uri = org.eclipse.core.filesystem.URIUtil.toURI(pathStr);

		if (uri != null && uri.isAbsolute()) {
			try {
				File file = new java.io.File(uri);
				String canonicalPathStr = file.getCanonicalPath();
				if (new Path(pathStr).getDevice() == null) {
					return new Path(canonicalPathStr).setDevice(null);
				}
				return new Path(canonicalPathStr);
			} catch (Exception e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * Find an existing resource in the workspace and create a language settings
	 * entry for it.
	 */
	private ICLanguageSettingEntry resolvePathEntryInWorkspace(int kind, URI uri, int flag, boolean isRelative) {
		if (uri != null && uri.isAbsolute()) {
			IResource rc = null;
			switch (kind) {
			case ICLanguageSettingEntry.INCLUDE_PATH:
			case ICLanguageSettingEntry.LIBRARY_PATH:
				rc = findContainerForLocationURI(uri, fProject, /* checkExistence */true);
				break;
			case ICLanguageSettingEntry.LIBRARY_FILE:
				rc = findFileForLocationURI(uri, fProject, /* checkExistence */true);
				break;
			}
			if (rc != null) {
				return createPathEntry(kind, rc, isRelative, flag);
			}
		}
		return null;
	}

	/**
	 * Return a resource in workspace corresponding the given folder {@link URI}
	 * preferable residing in the provided project.
	 */
	private static IResource findContainerForLocationURI(URI uri, IProject preferredProject, boolean checkExistence) {
		IResource resource = null;
		IResource[] resources = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(uri);
		for (IResource rc : resources) {
			if ((rc instanceof IProject || rc instanceof IFolder) && (!checkExistence || rc.isAccessible())) { // treat
																												// IWorkspaceRoot
																												// as
																												// non-workspace
																												// path
				if (rc.getProject().equals(preferredProject)) {
					resource = rc;
					break;
				}
				if (resource == null) {
					resource = rc; // to be deterministic the first qualified
									// resource has preference
				}
			}
		}
		return resource;
	}

	/**
	 * Create a language settings entry for a given resource. This will
	 * represent relative path using CDT variable ${ProjName}.
	 */
	private ICLanguageSettingEntry createPathEntry(int kind, IResource rc, boolean isRelative, int flag) {
		String path;
		if (isRelative && rc.getProject().equals(fProject)) {
			path = PROJ_NAME_PREFIX + rc.getFullPath().removeFirstSegments(1);
			flag = flag | ICSettingEntry.VALUE_WORKSPACE_PATH;
		} else {
			path = rc.getFullPath().toString();
			flag = flag | ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED;
		}
		return createEntry(kind, path, path, flag);
	}

	/**
	 * In case when absolute path is mapped to the source tree in a project this
	 * function will try to figure mapping and return "mapped root", i.e URI
	 * where the root path would be mapped. The mapped root will be used to
	 * prepend to other "absolute" paths where appropriate.
	 *
	 * @param resource
	 *            - a resource referred by parsed path
	 * @param parsedResourceName
	 *            - path as appears in the output
	 * @return mapped path as URI
	 */
	protected URI getMappedRootURI(IResource resource, String parsedResourceName) {
		if (resource == null) {
			return null;
		}

		URI resourceURI = resource.getLocationURI();
		String mappedRoot = "/"; //$NON-NLS-1$

		if (parsedResourceName != null) {
			IPath parsedSrcPath = new Path(parsedResourceName);
			if (parsedSrcPath.isAbsolute()) {
				IPath absResourcePath = resource.getLocation();
				int absSegmentsCount = absResourcePath.segmentCount();
				int relSegmentsCount = parsedSrcPath.segmentCount();
				if (absSegmentsCount >= relSegmentsCount) {
					IPath ending = absResourcePath.removeFirstSegments(absSegmentsCount - relSegmentsCount);
					ending = ending.setDevice(parsedSrcPath.getDevice()).makeAbsolute();
					if (ending.equals(parsedSrcPath.makeAbsolute())) {
						// mappedRoot here is parsedSrcPath with removed
						// parsedResourceName trailing segments,
						// i.e. if
						// absResourcePath="/path/workspace/project/file.c" and
						// parsedResourceName="project/file.c"
						// then mappedRoot="/path/workspace/"
						mappedRoot = absResourcePath.removeLastSegments(relSegmentsCount).toString();
					}
				}
			}
		}
		// this creates URI with schema and other components from resourceURI
		// but path as mappedRoot
		URI uri = efsProviderDefault.createNewURIFromPath(resourceURI, mappedRoot);
		return uri;
	}
}