/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.boost.build.core.internal.builder;

import java.net.URI;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteResource;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;

public class B2ProcessLauncher {
	private static final String CYGWIN_PREFIX = "cygdrive"; //$NON-NLS-1$
	
	private final ICommandLauncher fLocalLauncher = new CommandLauncher();
	private String[] fCommandArgs;
	private IRemoteConnection fConnection;
	private final Properties fEnvironment = new Properties();

	/**
	 * Convert a local (workspace) path into the remote equivalent. If the local path is not
	 * absolute, then do nothing.
	 * 
	 * e.g. Suppose the local path is /u/local_user/workspace/local_project/subdir1/subdir2
	 *      Suppose the remote project location is /home/remote_user/remote_project
	 *      Then the resulting path will be /home/remote_user/remote_project/subdir1/subdir2
	 * 
	 * @param localPath absolute local path in the workspace
	 * @param remote remote project
	 * @return remote path that is the equivalent of the local path
	 */
	public static String makeRemote(String local, IRemoteResource remote) {
		return makeRemote(new Path(local), remote).toString();
	}

	/**
	 * Convert a local (workspace) path into the remote equivalent. If the local path is not
	 * absolute, then do nothing.
	 * 
	 * e.g. Suppose the local path is /u/local_user/workspace/local_project/subdir1/subdir2
	 *      Suppose the remote project location is /home/remote_user/remote_project
	 *      Then the resulting path will be /home/remote_user/remote_project/subdir1/subdir2
	 * 
	 * @param localPath absolute local path in the workspace
	 * @param remote remote project
	 * @return remote path that is the equivalent of the local path
	 */
	public static IPath makeRemote(IPath localPath, IRemoteResource remote) {
		if (!localPath.isAbsolute()) {
			return localPath;
		}
		
		IPath remoteLocation = remote.getResource().getFullPath();
		IPath remotePath = new Path(remote.getActiveLocationURI().getPath());
		
		// Device mismatch, we might be in the presence of Cygwin or MinGW
		if (remoteLocation != null && remoteLocation.getDevice() != null && localPath.getDevice() == null) {
			boolean isCygwin = localPath.segment(0).equals(CYGWIN_PREFIX);
			remoteLocation = new Path(getPathString(remoteLocation, isCygwin));
			remotePath = new Path(getPathString(remotePath, isCygwin));
		}

		IPath relativePath = localPath.makeRelativeTo(remoteLocation);
		if (!relativePath.isEmpty()) {
			remotePath = remotePath.append(relativePath);
		}
		return remotePath;
	}

	private static String getPathString(IPath path, boolean isCygwin) {
		String s = path.toString();
		if (isCygwin) {
			s = s.replaceAll("^([a-zA-Z]):", "/" + CYGWIN_PREFIX + "/$1");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		} else {
			s = s.replaceAll("^([a-zA-Z]):", "/$1"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return s;
	}

	/**
	 * Constructs a command array that will be passed to the process
	 */
	private String[] constructCommandArray(String command, String[] commandArgs, IRemoteResource remote) {
		String[] args = new String[1 + commandArgs.length];
		args[0] = makeRemote(command, remote);
		for (int i = 0; i < commandArgs.length; i++) {
			args[i + 1] = makeRemote(commandArgs[i], remote);
		}
		return args;
	}

	public Process execute(IPath commandPath, String[] args, String[] env, IProject project, IPath workingDirectory, IProgressMonitor monitor)
			throws CoreException {
		if (project != null) {
			IRemoteResource remRes = (IRemoteResource) project.getAdapter(IRemoteResource.class);
			if (remRes != null) {
				URI uri = remRes.getActiveLocationURI();
				IRemoteServices remServices = RemoteServices.getRemoteServices(uri);
				if (remServices != null) {
					fConnection = remServices.getConnectionManager().getConnection(uri);
					if (fConnection != null) {
						parseEnvironment(env);
						fCommandArgs = constructCommandArray(commandPath.toString(), args, remRes);
						//IRemoteProcessBuilder processBuilder = fConnection.getProcessBuilder(fCommandArgs);
						if (workingDirectory != null) {
							String remoteWorkingPath = makeRemote(workingDirectory.toString(), remRes);
							IFileStore wd = fConnection.getFileManager().getResource(remoteWorkingPath);
							ISystemProfile [] profiles = RSECorePlugin.getTheSystemProfileManager().getSystemProfiles();
							IHost host = RSECorePlugin.getTheSystemRegistry().getHost(profiles[0], fConnection.getName());
							IShellService shellService = null;
							for (ISubSystem subSystem : host.getSubSystems()) {
								if (subSystem instanceof IShellServiceSubSystem) {
									shellService = ((IShellServiceSubSystem)subSystem).getShellService();
									break;
								}
							}
							if (shellService != null) {
								IHostShell hostShell;
								try {
									hostShell = shellService.launchShell(wd.toURI().getPath(), env, new NullProgressMonitor());
									hostShell.writeToShell(toString());
									return new HostShellProcessAdapter(hostShell);
								} catch (Exception e) {
									CCorePlugin.log(e);
								}
							}
						}
					}
				}
			}
		}
		return fLocalLauncher.execute(commandPath, args, env, workingDirectory, monitor);
	}

	protected String getCommandLine(String[] commandArgs) {
		return getCommandLineQuoted(commandArgs, false);
	}

	@SuppressWarnings("nls")
	private String getCommandLineQuoted(String[] commandArgs, boolean quote) {
		String nl = System.getProperty("line.separator", "\n");
		if (fConnection != null) {
			nl = fConnection.getProperty(IRemoteConnection.LINE_SEPARATOR_PROPERTY);
		}
		StringBuffer buf = new StringBuffer();
		if (commandArgs != null) {
			for (String commandArg : commandArgs) {
				if (quote && (commandArg.contains(" ") || commandArg.contains("\"") || commandArg.contains("\\"))) {
					commandArg = '"' + commandArg.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + '"';
				}
				buf.append(commandArg);
				buf.append(' ');
			}
			buf.append(nl);
		}
		return buf.toString();
	}

	/**
	 * Parse array of "ENV=value" pairs to Properties.
	 */
	private void parseEnvironment(String[] env) {
		if (env != null) {
			fEnvironment.clear();
			for (String envStr : env) {
				// Split "ENV=value" and put in Properties
				int pos = envStr.indexOf('=');
				if (pos < 0) {
					pos = envStr.length();
				}
				String key = envStr.substring(0, pos);
				String value = envStr.substring(pos + 1);
				fEnvironment.put(key, value);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String str : fCommandArgs) {
			sb.append(" ");
			sb.append(str); 
		}
		return sb.toString();
	}
}

