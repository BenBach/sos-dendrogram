/*
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.tuwien.ifs.somtoolbox.apps;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import at.tuwien.ifs.commons.gui.SOMToolboxAppChooser;
import at.tuwien.ifs.commons.gui.jsap.GenericGUI;
import at.tuwien.ifs.somtoolbox.SOMToolboxMetaConstants;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxApp.Type;
import at.tuwien.ifs.somtoolbox.apps.config.AbstractOptionFactory;
import at.tuwien.ifs.somtoolbox.apps.config.OptionFactory;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import at.tuwien.ifs.somtoolbox.util.SubClassFinder;
import at.tuwien.ifs.somtoolbox.util.UiUtils;

/**
 * Searches the classpath for classes implementing the {@link SOMToolboxApp} interface.
 * 
 * @author Jakob Frank
 * @version $Id: SOMToolboxMain.java 3985 2011-01-10 13:50:20Z frank $
 */
public class SOMToolboxMain {

    /**
     * Central Main for SOMToolbox.
     * 
     * @param args the command line args
     */
    public static void main(String[] args) {
        int screenWidth = 80;
        try {
            screenWidth = Integer.parseInt(System.getenv("COLUMNS"));
        } catch (Exception e) {
            screenWidth = 80;
        }

        JSAP jsap = new JSAP();
        try {
            jsap.registerParameter(new UnflaggedOption("main", JSAP.STRING_PARSER, null, false, false));
            jsap.registerParameter(new Switch("gui", 'G', "gui"));
            jsap.registerParameter(new Switch("version", JSAP.NO_SHORTFLAG, "version"));
            jsap.registerParameter(new Switch("listmains", JSAP.NO_SHORTFLAG, "list-mains"));
            jsap.registerParameter(new FlaggedOption("help", JSAP.STRING_PARSER, null, false, JSAP.NO_SHORTFLAG, "help"));
        } catch (JSAPException e) {
            assert false;
        }
        JSAPResult result = jsap.parse(args);

        ArrayList<Class<? extends SOMToolboxApp>> runnables = SubClassFinder.findSubclassesOf(SOMToolboxApp.class, true);
        Collections.sort(runnables, SOMToolboxApp.TYPE_GROUPED_COMPARATOR);

        // args > 0
        boolean useGUI = result.getBoolean("gui", false);
        if (useGUI) {
            UiUtils.setSOMToolboxLookAndFeel();
        }

        if (result.getBoolean("listmains")) {
            if (useGUI) {
                showAvailableRunnables(runnables, args);
            } else {
                printAvailableRunnables(screenWidth, runnables);
            }
        } else if (result.getBoolean("version")) {
            if (result.userSpecified("main")) {
                printVersion(result.getString("main"));
            } else {
                printVersion("somtoolbox");
            }
        } else if (result.userSpecified("main")) {
            String mainClass = result.getString("main");
            String[] cleanArgs = Arrays.copyOfRange(args, 1, args.length);
            if (!invokeMainClass(runnables, mainClass, cleanArgs, useGUI)) {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").severe("Runnable \"" + mainClass + "\" not found.");
                printAvailableRunnables(screenWidth, runnables);
            }
        } else if (result.contains("help")) {
            if (result.getString("help") != null) {
                invokeMainClass(runnables, result.getString("help"), new String[] { "--help" }, useGUI);
            } else {
                printHelp();
            }
        } else {
            if (useGUI) {
                showAvailableRunnables(runnables, args);
            } else {
                printAvailableRunnables(screenWidth, runnables);
            }

        }
    }

    /**
     * @param runnables {@link ArrayList} of available runnables.
     * @param args command line arguments.
     */
    private static void showAvailableRunnables(ArrayList<Class<? extends SOMToolboxApp>> runnables, String[] args) {
        new SOMToolboxAppChooser(runnables, args).setVisible(true);
    }

    /**
     * @param runnables ArrayList of available main-classes
     * @param runable The main class specified on command line
     * @param cleanArgs the arguments for the main class.
     * @param useGUI <code>true</code> if the {@link GenericGUI} should be used.
     * @return <code>true</code> if invocation was successful, <code>false</code> otherwise.
     */
    private static boolean invokeMainClass(ArrayList<Class<? extends SOMToolboxApp>> runnables, String runable,
            String[] cleanArgs, boolean useGUI) {
        boolean exe = false;
        for (Class<? extends SOMToolboxApp> r : runnables) {
            if (r.getSimpleName().equalsIgnoreCase(runable)) {
                exe = tryInvokeMain(r, cleanArgs, useGUI);
                if (exe) {
                    break;
                }
            } else {
            }
        }
        if (!exe) {
            try {
                exe = tryInvokeMain(Class.forName(runable), cleanArgs, useGUI);
            } catch (ClassNotFoundException e) {
                exe = false;
            }
        }
        if (!exe) {
            try {
                exe = tryInvokeMain(Class.forName("at.tuwien.ifs.somtoolbox." + runable), cleanArgs, useGUI);
            } catch (ClassNotFoundException e) {
                exe = false;
            }
        }
        return exe;
    }

    /**
     * @param cls the class to search the main in.
     * @param args command line args for the main class invoked.
     * @param useGUI <code>true</code> if the {@link GenericGUI} should be launched.
     * @return <code>true</code> if the class contains a <code>static main(String[])</code> that was invoked,
     *         <code>false</code> otherwise.
     * @see GenericGUI
     */
    private static boolean tryInvokeMain(Class<?> cls, String[] args, boolean useGUI) {
        try {
            if (useGUI) {
                @SuppressWarnings("unchecked")
                Class<SOMToolboxApp> sta = (Class<SOMToolboxApp>) cls;
                new GenericGUI(sta, args).setVisible(true);
                return true;
            }
        } catch (ClassCastException cce) {
            // Nop, continue...
        }

        try {
            Method main = cls.getMethod("main", String[].class);

            // special handling - if the parameter "--help" is present, also print the description
            if (ArrayUtils.contains(args, "--help")) {
                Object description = null;
                try { // try to get the LONG_DESCRIPTION field
                    description = cls.getField("LONG_DESCRIPTION").get(null) + "\n";
                } catch (Exception e) {
                    try { // fall back using the DESCRIPTION field
                        description = cls.getField("DESCRIPTION").get(null) + "\n";
                    } catch (Exception e1) { // nothing found => write nothing...
                        description = "";
                    }
                }
                System.out.println(description);
                try {
                    Parameter[] options = (Parameter[]) cls.getField("OPTIONS").get(null);
                    JSAP jsap = AbstractOptionFactory.registerOptions(options);
                    JSAPResult jsapResult = OptionFactory.parseResults(args, jsap, cls.getName());
                    AbstractOptionFactory.printUsage(jsap, cls.getName(), jsapResult, null);
                    return true;
                } catch (Exception e1) { // we didn't find the options => let the class be invoked ...
                }
            }

            main.invoke(null, new Object[] { args });
            return true;
        } catch (InvocationTargetException e) {
            // If main throws an error, print it
            e.getCause().printStackTrace();
            return true;
        } catch (Exception e) {
            // Everything else is hidden...
            return false;
        }
    }

    private static void printHelp() {
        System.out.println("somtoolbox " + SOMToolboxMetaConstants.getVersion());
        System.out.println();
        System.out.println("Usage:");
        System.out.println("\tsomtoolbox --list-mains");
        System.out.println("\tsomtoolbox --version");
        System.out.println("\tsomtoolbox --help [<Runnable>]");
        System.out.println("\tsomtoolbox <Runnable> ...");
        System.out.println();
        System.out.println("Description:");
        System.out.println("\tsomtoolbox is a framework for creating, viewing, exploring and");
        System.out.println("\tanalysing Self-Organising Maps (SOMs).");
        System.out.println();
        System.out.println("\tsomtoolbox can create different types of SOMs, including (but not");
        System.out.println("\tlimited to) GrowingSOMs, HierachicalSOMs, ToroidSOMs, 3D-Soms,");
        System.out.println("\tMenomicSOMs.");
        System.out.println();
        System.out.println("\tsomtoolbox can apply different visualisations to a SOM, including");
        System.out.println("\t(but not limited to) U-, U*-, D- and P-Matrix; Smoothed Data Histogram,");
        System.out.println("\tGradient Field and many more");
        System.out.println();
        System.out.println("\tsomtoolbox is an academic prototype under constant development.");
        System.out.println();
        System.out.println("Options:");
        System.out.println("\t--list-mains");
        System.out.println("\t\tList available Runnables and exit.");
        System.out.println();
        System.out.println("\t--version");
        System.out.println("\t\tPrint version and exit.");
        System.out.println();
        System.out.println("\t--help [<Runnable>]");
        System.out.println("\t\tPrint this help or the <Runnable>-specific help message");
        System.out.println();
        System.out.println("\t<Runnable> ...");
        System.out.println("\t\tExecute the specified <Runnable>,");
        System.out.println("\t\tsee somtoolbox --help <Runnable> for details");
        System.out.println();
        System.out.println(SOMToolboxApp.DEV_BY_STRING);
        System.out.println("somtoolbox home page: <" + SOMToolboxApp.HOMEPAGE + ">");
    }

    /**
     * @param screenWidth the with of the screen
     * @param runnables {@link ArrayList} of available runnables.
     */
    private static void printAvailableRunnables(int screenWidth, ArrayList<Class<? extends SOMToolboxApp>> runnables) {
        Collections.sort(runnables, SOMToolboxApp.TYPE_GROUPED_COMPARATOR);

        ArrayList<Class<? extends SOMToolboxApp>> runnableClassList = new ArrayList<Class<? extends SOMToolboxApp>>();
        ArrayList<String> runnableNamesList = new ArrayList<String>();
        ArrayList<String> runnableDeskrList = new ArrayList<String>();

        for (Class<? extends SOMToolboxApp> c : runnables) {
            try {
                // Ignore abstract classes and interfaces
                if (Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) {
                    continue;
                }
                runnableClassList.add(c);
                runnableNamesList.add(c.getSimpleName());

                String desk = null;
                try {
                    Field f = c.getDeclaredField("DESCRIPTION");
                    desk = (String) f.get(null);
                } catch (Exception e) {
                }

                if (desk != null) {
                    runnableDeskrList.add(desk);
                } else {
                    runnableDeskrList.add("");
                }
            } catch (SecurityException e) {
                // Should not happen - no Security
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        StringBuilder sb = new StringBuilder();
        String lineSep = System.getProperty("line.separator", "\n");

        int maxLen = StringUtils.getLongestStringLength(runnableNamesList);
        sb.append("Runnable classes:").append(lineSep);
        for (int i = 0; i < runnableNamesList.size(); i++) {
            final Type cType = Type.getType(runnableClassList.get(i));
            if (i == 0 || !cType.equals(Type.getType(runnableClassList.get(i - 1)))) {
                sb.append(String.format("-- %s %s%s", cType.toString(),
                        StringUtils.repeatString(screenWidth - (8 + cType.toString().length()), "-"), lineSep));
            }
            sb.append("    ");
            sb.append(runnableNamesList.get(i));
            sb.append(StringUtils.getSpaces(4 + maxLen - runnableNamesList.get(i).length())).append("- ");
            sb.append(runnableDeskrList.get(i));
            sb.append(lineSep);
        }
        System.out.println(StringUtils.wrap(sb.toString(), screenWidth, StringUtils.getSpaces(maxLen + 10), true));
    }

    /**
     * 
     */
    public static void printVersion(String executable) {
        System.out.println(executable + " " + SOMToolboxMetaConstants.getVersion());
        System.out.println();
        System.out.println(SOMToolboxApp.DEV_BY_STRING);
        System.out.println();
        System.out.println(SOMToolboxApp.HOMEPAGE);
        System.out.println();
    }

}
