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
package at.tuwien.ifs.somtoolbox.apps.config;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

import at.tuwien.ifs.commons.util.io.ExtensionFileFilterSwing;
import at.tuwien.ifs.somtoolbox.apps.SOMToolboxMain;

/**
 * @author Rudolf Mayer
 * @version $Id: AbstractOptionFactory.java 3753 2010-08-17 09:12:29Z mayer $
 */
public class AbstractOptionFactory {

    private static class FieldComparator implements Comparator<Field> {
        @Override
        public int compare(Field o1, Field o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private static class MethodComparator implements Comparator<Method> {
        @Override
        public int compare(Method o1, Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public static Logger logger;

    public static EnumeratedStringParser makeEnumeratedStringParser(String... validOptionValues) {
        return EnumeratedStringParser.getParser(at.tuwien.ifs.somtoolbox.util.StringUtils.toString(validOptionValues,
                "", "", Character.toString(EnumeratedStringParser.CONSTRUCTOR_VALUE_SEPARATOR)), true, false);
    }

    public static JSAPResult parseResults(Class<?> callingClass, String[] args, Parameter... options) {
        logger = Logger.getLogger(callingClass.getPackage().getName());
        return parseResults(args, registerOptions(options), callingClass.getName());
    }

    public static JSAPResult parseResults(Class<?> callingClass, String[] args, boolean printParameterValues,
            Parameter... options) {
        JSAPResult parseResults = parseResults(args, registerOptions(options), callingClass.getName());
        if (printParameterValues) {
            logger.info(AbstractOptionFactory.toString(parseResults, options));
        }
        return parseResults;
    }

    public static JSAPResult parseResults(String[] args, JSAP jsap) {
        return parseResults(args, jsap, null);
    }

    public static JSAPResult parseResults(String[] args, Parameter... options) {
        return parseResults(args, registerOptions(options), null);
    }

    public static JSAPResult parseResults(String[] args, boolean printParameterValues, Parameter... options) {
        JSAPResult parseResults = parseResults(args, registerOptions(options), null);
        if (printParameterValues) {
            logger.info(AbstractOptionFactory.toString(parseResults, options));
        }
        return parseResults;
    }

    public static JSAPResult parseResults(String[] args, JSAP jsap, String className) {
        try {
            jsap.registerParameter(new Switch("gui", 'G', "gui", "Show the graphical interface for additional options."));
        } catch (JSAPException e3) {
        }
        try {
            jsap.registerParameter(new Switch("help", JSAP.NO_SHORTFLAG, "help", "Print this help and exit."));
        } catch (JSAPException e2) {
        }
        try {
            jsap.registerParameter(new Switch("version", JSAP.NO_SHORTFLAG, "version", "Print the version and exit."));
        } catch (JSAPException e1) {
        }
        JSAPResult config = jsap.parse(args);

        if (className == null) {
            className = computeClassName();
        }

        if (config.getBoolean("help")) {
            printHelp(jsap, className, System.out);
            System.exit(0);
        }
        if (config.getBoolean("version")) {
            printVersion(className);
            System.exit(0);
        }

        if (!config.success()) {
            printUsage(jsap, className, config, null);
        }
        try {
            logger = Logger.getLogger(Class.forName(className).getPackage().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static String toString(JSAPResult result, Parameter[] options) {
        StringBuffer sb = new StringBuffer();
        for (Parameter option : options) {
            String optionsId = option.getID();
            String optionsValue = String.valueOf(result.getObject(optionsId));
            sb.append(optionsId + ": " + optionsValue + "\n");
        }
        return sb.toString();
    }

    private static String computeClassName() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!stackTraceElement.getClassName().equals(AbstractOptionFactory.class.getName())) {
                return stackTraceElement.getClassName();
            }
        }
        return "<unknown class>";
    }

    public static void printUsage(JSAP jsap, String className, JSAPResult config, String errorMessage) {
        // print out specific error messages describing the problems
        // with the command line, THEN print usage, THEN print full
        // help. This is called "beating the user with a clue stick."
        System.err.println();
        for (Iterator<?> errs = config.getErrorMessageIterator(); errs.hasNext();) {
            System.err.println("Error: " + errs.next());
        }
        if (errorMessage != null && !errorMessage.trim().equals("")) {
            System.err.println("Error: " + errorMessage);
        }
        System.err.println();
        printHelp(jsap, className, System.err);
        System.exit(-1);
    }

    public static void printHelp(JSAP jsap, String className, PrintStream outStream) {
        outStream.println("Usage: java " + className + " " + jsap.getUsage());
        outStream.println();
        outStream.println("Options:");
        // Replacements for better man-pages
        outStream.println(jsap.getHelp(120).replaceAll("[\\[\\]()]", "").replaceAll("\\|", ", "));
    }

    private static void printVersion(String className) {
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        SOMToolboxMain.printVersion(simpleClassName);
    }

    /**
     * Register a given set of options to the given JSAP object.
     * 
     * @param jsap The JSAP to register options to.
     * @param options The options to register.
     */
    public static void registerOptions(JSAP jsap, Parameter[] options) {
        for (Parameter element : options) {
            try {
                jsap.registerParameter(element);
            } catch (JSAPException e) {
                logger.severe(e.getMessage());
            }
        }
    }

    public static JSAP registerOptions(Parameter[] options) {
        JSAP jsap = new JSAP();
        for (Parameter element : options) {
            try {
                jsap.registerParameter(element);
            } catch (JSAPException e) {
                logger.severe(e.getMessage());
            }
        }
        return jsap;
    }

    public static String getFilePath(JSAPResult config, String id) {
        if (config.contains(id)) {
            return config.getFile(id).getAbsolutePath();
        } else {
            return null;
        }
    }

    protected static FileStringParser getOuputFileParser() {
        return FileStringParser.getParser().setMustBeFile(true);
    }

    protected static FileStringParser getInputFileParser() {
        // FIXME: setMustExist is a hard condition if we have a file where we also allow skipping the .gz extension
        // this is basically the case for most input files...
        return FileStringParser.getParser().setMustBeFile(true);// .setMustExist(true);
    }

    protected static FileStringParser getInputFileParser(String... extension) {
        return getInputFileParser().setFileFilter(new ExtensionFileFilterSwing(extension));
    }

    protected static FileStringParser getOutputDirectoryParser() {
        return FileStringParser.getParser().setMustBeDirectory(true);
    }

    protected static FileStringParser getInputDirectoryParser() {
        return FileStringParser.getParser().setMustBeDirectory(true).setMustExist(true);
    }

    protected static void testDuplicateOptions(Object o) {
        Class<?> c = o.getClass();
        ArrayList<Field> paramFields = new ArrayList<Field>();
        Field[] fields = c.getFields();
        for (Field field : fields) {
            if (Parameter.class.isAssignableFrom(field.getClass())) {
                paramFields.add(field);
            } else {
                System.out.println("Skipping field: " + field.getName() + " (" + field + ")");
            }
        }
        Collections.sort(paramFields, new FieldComparator());

        ArrayList<Method> paramMethods = new ArrayList<Method>();
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            if (Parameter.class.isAssignableFrom(method.getReturnType())) {
                paramMethods.add(method);
            } else {
                System.out.println("Skipping method: " + method.getName() + " (" + method + ")");
            }
        }
        Collections.sort(paramMethods, new MethodComparator());
        System.out.println("\n");

        HashMap<Parameter, String> shortFlags = new HashMap<Parameter, String>();
        HashMap<Parameter, String> longFlags = new HashMap<Parameter, String>();
        HashMap<Parameter, String> ids = new HashMap<Parameter, String>();

        System.out.println("Fields containing Parameters: ");
        if (paramFields.size() == 0) {
            System.out.println("\tnone");
        }

        for (Field field : paramFields) {
            System.out.println("\t" + field.getName() + " (" + field + ")");
            try {
                Parameter parameter = (Parameter) field.get(o);
                checkParamter(shortFlags, longFlags, ids, parameter);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Methods containing Parameters: ");
        for (Method method : paramMethods) {
            System.out.print("\t" + method.getName() + "\t(");
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> class1 = parameterTypes[i];
                System.out.print(class1.getSimpleName());
                if (i + 1 < parameterTypes.length) {
                    System.out.print(", ");
                }
            }
            System.out.print(")");
            try {
                Class<?>[] paramTypes = method.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    Class<?> class1 = paramTypes[i];
                    if (class1.getName().equalsIgnoreCase("boolean")) {
                        args[i] = true;
                    } else if (class1.getName().equalsIgnoreCase("int")) {
                        args[i] = 1;
                    } else {
                        args[i] = null;
                    }
                }
                System.out.println(" => args: " + Arrays.toString(args));
                Parameter parameter = (Parameter) method.invoke(o, args);
                checkParamter(shortFlags, longFlags, ids, parameter);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }

    private static void checkParamter(HashMap<Parameter, String> shortFlags, HashMap<Parameter, String> longFlags,
            HashMap<Parameter, String> ids, Parameter parameter) {
        checkDuplicates(ids, parameter, "id", parameter.getID());
        if (parameter instanceof FlaggedOption || parameter instanceof Switch) {
            String longFlag;
            char shortFlag;
            if (parameter instanceof FlaggedOption) {
                longFlag = ((FlaggedOption) parameter).getLongFlag();
                shortFlag = ((FlaggedOption) parameter).getShortFlag();
            } else {
                longFlag = ((Switch) parameter).getLongFlag();
                shortFlag = ((Switch) parameter).getShortFlag();
            }
            if (longFlag != JSAP.NO_LONGFLAG) {
                checkDuplicates(longFlags, parameter, "longFlag", longFlag);
            }
            if (shortFlag != JSAP.NO_SHORTFLAG) {
                checkDuplicates(shortFlags, parameter, "shortFlag", shortFlag);
            }
        }
    }

    private static void checkDuplicates(HashMap<Parameter, String> map, Parameter parameter, String propertyName,
            Character propertyValue) {
        checkDuplicates(map, parameter, propertyName, String.valueOf(propertyValue));

    }

    private static void checkDuplicates(HashMap<Parameter, String> map, Parameter parameter, String propertyName,
            String propertyValue) {
        if (map.containsValue(propertyValue)) {
            System.out.println("Duplicate " + propertyName + " '" + propertyValue + "'");
        }
        map.put(parameter, propertyValue);
    }

    protected static ArrayList<Parameter> findDuplicates(HashMap<Parameter, ?> map, Object value) {
        ArrayList<Parameter> res = new ArrayList<Parameter>();
        for (Map.Entry<Parameter, ?> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                res.add(entry.getKey());
            }
        }
        return res;
    }
}