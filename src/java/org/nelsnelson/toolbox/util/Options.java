package org.nelsnelson.toolbox.util;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Options {
    private static final Map options = new LinkedHashMap();
    public static final String ALWAYS_PRINT_USAGE = "always-print-usage";
    private static Usage usage = null;

    public Options() {
        this(options);
    }

    public Options(Map options) {
        setOptions(options);
    }

    public Options(Map options, String[] args) {
        setOptions(options);

        setCommandLineArguments(args);
    }

    public Options(Map options, String[] args, String usage) {
        setOptions(options);
        setUsage(usage);

        setCommandLineArguments(args);
    }

    public Options(String[] args) {
        this();

        setCommandLineArguments(args);
    }

    public Options(String[] args, String usage) {
        this();
        setUsage(usage);

        setCommandLineArguments(args);
    }

    public Options setCommandLineArguments(String[] args) {
        Iterator i = Arrays.asList(args).iterator();

        while (i.hasNext()) {
            String s = (String) i.next();

            if (s.startsWith("--")) {
                s = s.substring(2); // get rid of "--"

                String key = s;
                String value = s;

                if (s.indexOf("=") >= 0) {
                    String[] pair = s.split("=", 2);

                    key = pair[0];
                    value = pair[1];
                }

                options.put(key, dequote(value));
            }
            else if (s.indexOf("-") >= 0) {
                if (s.length() == 2) {
                    String value = (String) i.next();

                    if (value == null) {
                        violation();
                    }

                    options.put(s, dequote(value));
                }
                else {
                    violation();
                }
            }
            else {
                options.put(s, dequote(s));
            }
        }

        return this;
    }

    public final static String dequote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() -1);
        }
        
        return s;
    }

    protected final static void violation() {
        if (usage == null) {
            throw new IllegalArgumentException();
        }

        System.out.println(usage);
        System.exit(0);
    }

    public void setUsage(String usage) {
        if (this.usage == null) {
            this.usage = new Usage(usage);
        }
        else this.usage.setUsage(usage);
    }

    public static Usage getUsage() {
        return usage;
    }

    public static boolean contains(String name) {
        return getOptions().containsKey(name);
    }

    public static String get(int i) {
        Map.Entry entry = (Map.Entry) options.entrySet().toArray()[i];
        String key = (String) entry.getKey();
        String value = (String) entry.getKey();
        return key == null || value == null || key.equals(value) ? null : value;
    }

    public static String get(String name) {
        return (String) options.get(name);
    }

    public static String get(String[] names) {
        String option = null;

        for (int i = 0; i < names.length; i++) { 
            option = get(names[i]);
            if (option != null) break;
        }

        return option;
    }

    public static String demand(int i) {
        Object[] args = options.values().toArray();

        if (args.length <= i || args[i] == null) violation();

        return (String) args[i];
    }

    public static String demand(String name) {
        String arg = (String) options.get(name);

        if (arg == null) violation();

        return arg;
    }

    public static String demand(String[] names) {
        String arg = null;

        for (int i = 0; i < names.length; i++) { 
            arg = get(names[i]);
            if (arg != null) break;
        }

        if (arg == null) violation();

        return arg;
    }

    private static Map getOptions() {
        return options;
    }

    private static void setOptions(Map options) {
        getOptions().clear();
        getOptions().putAll(options);
    }

    public static int size() {
        return getOptions().size();
    }
    
    public String toString() {
        String s = "";

        java.util.Properties p = new java.util.Properties();
        p.putAll(getOptions());
        java.io.StringWriter sw = new java.io.StringWriter();
        p.list(new java.io.PrintWriter(sw));

        return sw.toString();
    }
}

class Usage {
    private String usage = null;

    public Usage(String usage) {
        this.usage = usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String toString() {
        return usage;
    }
}
