/* Copyright Frank Birbacher 2008
 * 
 * Use, modification and distribution are subject to the Boost Software License,
 * Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt).
 */
package org.boost.eclipse.bjam.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;

public class JamfileScanner {
    static public JamTargetInfo[] getTargets(final IFile file) {
        abort: try {
            if (!file.exists())
                break abort;
            final Reader reader = new InputStreamReader(file.getContents());
            final JamTargetInfo[] result = getTargets(new BufferedReader(reader));
            for (JamTargetInfo info : result)
                info.setResource(file);
            return result;
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
        return new JamTargetInfo[0];
    }

    static private JamTargetInfo[] getTargets(final BufferedReader reader) {
        final ArrayList<JamTargetInfo> targets = new ArrayList<JamTargetInfo>();
        try {
            while (true) {
                final String currentLine = reader.readLine();
                if (currentLine == null)
                    break;
                final String[] tokens = currentLine.split("\\s", 3);
                if (tokens.length < 2)
                    continue;
                if (!isKnownTargetType(tokens[0]))
                    continue;
                final boolean isProject = tokens[0].equals("project");
                final String uiTargetName = tokens[0] + " " + tokens[1];
                final String bjamTargetName = isProject ? "" : tokens[1];
                targets.add(new JamTargetInfo(uiTargetName, bjamTargetName));
            }
        } catch (Exception e) {
            targets.clear();
        }
        final JamTargetInfo[] result = new JamTargetInfo[targets.size()];
        return targets.toArray(result);
    }

    private static boolean isKnownTargetType(final String type) {
        final String[] targets = new String[] { "exe", "lib", "alias",
                "project", "boost-build", "install", "stage", "use-project",
                "doxygen", "xml", "boostbook", "bundle", "make", "notfile",
                "test-suite" };
        for (final String word : targets)
            if (word.equals(type))
                return true;
        return false;
    }
}
