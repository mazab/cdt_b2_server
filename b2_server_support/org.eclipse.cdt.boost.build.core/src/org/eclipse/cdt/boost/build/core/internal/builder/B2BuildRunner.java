/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core.internal.builder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.boost.build.core.B2ProcessManager;
import org.eclipse.cdt.boost.build.core.ProjectSettingsPersistenceUtils;
import org.eclipse.cdt.boost.build.core.internal.model.Event;
import org.eclipse.cdt.boost.build.core.internal.model.Request;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.internal.core.StreamProgressMonitor;
import org.eclipse.cdt.managedbuilder.core.AbstractBuildRunner;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

public class B2BuildRunner extends AbstractBuildRunner {
	private static final int PROGRESS_MONITOR_SCALE = 100;

	boolean isCancelled = false;

	@Override
	public boolean invokeBuild(int kind, IProject project, IConfiguration configuration,
			IBuilder builder, IConsole console, IMarkerGenerator markerGenerator,
			IncrementalProjectBuilder projectBuilder, IProgressMonitor monitor) throws CoreException {
		return invokeExternalBuild(kind, project, configuration, builder, console,
				markerGenerator, projectBuilder, monitor);
	}

	protected boolean invokeExternalBuild(int kind, IProject project, IConfiguration configuration,
			IBuilder builder, IConsole console, IMarkerGenerator markerGenerator,
			IncrementalProjectBuilder projectBuilder, IProgressMonitor monitor) throws CoreException {

		boolean isClean = false;

		monitor.setTaskName("Invoking Boost builder on project " + project.getName());

		IPath buildCommand = builder.getBuildCommand();
		if (buildCommand != null) {
			String cfgName = configuration.getName();
			String[] targets = new String[0]; // TODO
			String buildType = getTarget(kind, builder);
			if (buildType.startsWith("clean"))
				isClean = true;
			URI workingDirectoryURI = ManagedBuildManager.getBuildLocationURI(configuration, builder);
			String[] errorParsers = builder.getErrorParsers();
			ErrorParserManager epm = new ErrorParserManager(project, workingDirectoryURI, markerGenerator, errorParsers);
			epm.setOutputStream(console.getOutputStream());
			List<IConsoleParser> parsers = new ArrayList<IConsoleParser>();
			parsers.add(epm);
			ICConfigurationDescription cfgDescription = ManagedBuildManager.getDescriptionForConfiguration(configuration);
			collectLanguageSettingsConsoleParsers(cfgDescription, epm, parsers);
			boolean bPerformBuild = true;
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			if (!shouldBuild(kind, info)) {
				return isClean;
			}
			if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
				IResourceDelta delta = projectBuilder.getDelta(project);
				if (delta != null) {
					IResource res = delta.getResource();
					if (res != null) {
						bPerformBuild = res.getProject().equals(project);
					}
				} else {
					bPerformBuild = false;
				}
			}
			if (bPerformBuild) {
				StreamProgressMonitor streamProgressMonitor = new StreamProgressMonitor(monitor, null, PROGRESS_MONITOR_SCALE);
				ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(streamProgressMonitor, streamProgressMonitor, parsers.toArray(new IConsoleParser[parsers.size()]));
				OutputStream out = sniffer.getOutputStream();
				removeOldMarkers(project, monitor);
				HashMap<String, String> properties = new HashMap<>();
				properties.putAll(ProjectSettingsPersistenceUtils.getAllProjectInfo(project));
				B2Process process = B2ProcessManager.getBuilderProcess(project);
				Request request = new Request(buildType, Arrays.asList(targets), properties, null);
				writeLine(out, epm, request.getDisplayString());
				process.invokeRequest(request);
				Event event;
				do {
					try {
						checkCancel(monitor);
					} catch (OperationCanceledException e) {
						request = new Request(Request.REQUEST_TYPE_CANCEL, Arrays.asList(targets), properties, null);
						writeLine(out, epm, request.getDisplayString());
					}
					event = process.waitForEvent();
					writeLine(out, epm, event.getDisplayString());
				} while (event != null && !event.getEventType().equals(Event.EVENT_TYPE_FINISHED));
				refreshProject(project, cfgName, monitor);
			}
		}
		return isClean;
	}

	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	protected boolean shouldBuild(int kind, IManagedBuildInfo info) {
		IConfiguration cfg = info.getDefaultConfiguration();
		IBuilder builder = null;
		if (cfg != null) {
			builder = cfg.getEditableBuilder();
			switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				return builder.isAutoBuildEnable();
			case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
			case IncrementalProjectBuilder.FULL_BUILD :
				return builder.isFullBuildEnabled() | builder.isIncrementalBuildEnabled() ;
			case IncrementalProjectBuilder.CLEAN_BUILD :
				return builder.isCleanBuildEnabled();
			}
		}
		return true;
	}

	protected String getTarget(int kind, IBuilder builder) {
		String targetsArray[] = null;

		if(kind != IncrementalProjectBuilder.CLEAN_BUILD && !builder.isCustomBuilder() && builder.isManagedBuildOn()){
			IConfiguration cfg = builder.getParent().getParent();
			String preBuildStep = cfg.getPrebuildStep();
			try {
				preBuildStep = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						preBuildStep,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						cfg);
			} catch (BuildMacroException e) {
			}

			if(preBuildStep != null && preBuildStep.length() != 0){
				targetsArray = new String[]{"pre-build", "main-build"}; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		if(targetsArray == null){
			String targets = ""; //$NON-NLS-1$
			switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				targets = builder.getAutoBuildTarget();
				break;
			case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
			case IncrementalProjectBuilder.FULL_BUILD :
				targets = builder.getIncrementalBuildTarget();
				break;
			case IncrementalProjectBuilder.CLEAN_BUILD :
				targets = builder.getCleanBuildTarget();
				break;
			}

			targetsArray = CommandLineUtil.argumentsToArray(targets);
		}
		
		String ret = "build";
		// search for clean target
		for (String target : targetsArray) {
			if ("clean".equals(target.toLowerCase())) {
				ret = "clean-build";
			}
		}
		return ret;
	}
/*
	protected Map<String, String> getEnvironment(IBuilder builder) throws CoreException {
		Map<String, String> envMap = new HashMap<String, String>();
		if (builder.appendEnvironment()) {
			ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(builder.getParent().getParent());
			IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IEnvironmentVariable[] vars = mngr.getVariables(cfgDes, true);
			for (IEnvironmentVariable var : vars) {
				envMap.put(var.getName(), var.getValue());
			}
		}

		// Add variables from build info
		Map<String, String> builderEnv = builder.getExpandedEnvironment();
		if (builderEnv != null)
			envMap.putAll(builderEnv);

		return envMap;
	}
*/
	private static void collectLanguageSettingsConsoleParsers(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker, List<IConsoleParser> parsers) {
		if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			List<ILanguageSettingsProvider> lsProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			for (ILanguageSettingsProvider lsProvider : lsProviders) {
				ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(lsProvider);
				if (rawProvider instanceof ICBuildOutputParser) {
					ICBuildOutputParser consoleParser = (ICBuildOutputParser) rawProvider;
					try {
						consoleParser.startup(cfgDescription, cwdTracker);
						parsers.add(consoleParser);
					} catch (CoreException e) {
						ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
								"Language Settings Provider failed to start up", e)); //$NON-NLS-1$
					}
				}
			}
		}
	}
	
	private static void writeLine(OutputStream out, ErrorParserManager epm, String line) {
		try {
			out.write((line + "\n").getBytes());
			out.flush();
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
	}

	private void refreshProject(IProject project, String configName, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask(CCorePlugin.getFormattedString("BuildRunnerHelper.refreshingProject", project.getName()), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			monitor.subTask(""); //$NON-NLS-1$

			// Do not allow the cancel of the refresh, since the builder is external
			// to Eclipse, files may have been created/modified and we will be out-of-sync.
			// The caveat is for huge projects, it may take sometimes at every build.
			// Use the refresh scope manager to refresh
			RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
			IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project, configName);
			ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			// ignore exceptions
		} finally {
			monitor.done();
		}
	}

	public void removeOldMarkers(IProject project, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			try {
				if (project != null) {
					monitor.subTask(CCorePlugin.getFormattedString("BuildRunnerHelper.removingMarkers", project.getFullPath().toString())); //$NON-NLS-1$
					project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false,  IResource.DEPTH_INFINITE);
				}
			} catch (CoreException e) {
				// ignore
			}
			if (project != null) {
				// Remove markers which source is this project from other projects
				try {
					IWorkspace workspace = project.getWorkspace();
					IMarker[] markers = workspace.getRoot().findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
					String projectName = project.getName();
					List<IMarker> markersList = new ArrayList<IMarker>();
					for (IMarker marker : markers) {
						if (projectName.equals(marker.getAttribute(IMarker.SOURCE_ID))) {
							markersList.add(marker);
						}
					}
					if (markersList.size() > 0) {
						workspace.deleteMarkers(markersList.toArray(new IMarker[markersList.size()]));
					}
				} catch (CoreException e) {
					// ignore
				}
			}

		} finally {
			monitor.done();
		}
	}
}
