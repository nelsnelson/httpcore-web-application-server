/*
 * ClassUtils.java
 *
 * Created on December 13, 2006, 3:54 PM
 */

package org.nelsnelson.toolbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author nelsnelson
 */
public class ClassUtils {
    private ClassUtils() {
        
    }
    
    public static Class loadClass(File classFile, String packageSpec) {
        Class clazz = null;
        
        String classSpec = getClassSpec(classFile, packageSpec);
        
        try {
            // Convert the class root to a URL
            URL url = classFile.getParentFile().toURL();
            
            // Create a new class loader with the directory
            ClassLoader classLoader = new URLClassLoader(new URL[] { url });
            
            // Load in the class
            clazz = classLoader.loadClass(classSpec);
        }
        catch (MalformedURLException e) {
            clazz = e.getClass();
        }
        catch (ClassNotFoundException e) {
            clazz = e.getClass();
        }
        
        return clazz;
    }
    
    public static String getPackagePath(Class clazz) {
        String classPackage = clazz.getPackage().getName();
        
        return "/" + classPackage.replace('.', '/').replace('.', '\\');
    }
    
    public static String getSimpleName(Class clazz) {
        String className = clazz.getName();
        
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public static String getClassPackage(Class clazz) {
        String className = clazz.getName();
        
        return className.substring(0, className.lastIndexOf("."));
    }
    
    public static String getClassSpec(File classFile, String packageSpec) {
        String classSpec = "";
        
        String fileName = classFile.getName();
        
        int lastDotPos = fileName.lastIndexOf(".");
        
        if (lastDotPos >= 0) {
            classSpec = packageSpec + "." + fileName.substring(0, lastDotPos);
        }
        
        return classSpec;
    }
}
