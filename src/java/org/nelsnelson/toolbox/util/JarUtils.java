package org.nelsnelson.toolbox.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarUtils {
    public static final String UTF_8 = "UTF-8";
    public static void extractJar(File jarFile, File destinationDirectory) 
        throws IOException
    {
        JarFile jar = new JarFile(jarFile);
        try {
            Enumeration<JarEntry> enumerator = jar.entries();
            while (enumerator.hasMoreElements()) {
                Object element = enumerator.nextElement();
                JarEntry file = null;

                if (element instanceof JarEntry) {
                    file = (JarEntry) element;
                }

                File f = new File(destinationDirectory, File.separator + file.getName());
                if (file.isDirectory()) {
                    f.mkdir();
                    continue;
                }
                InputStream is = jar.getInputStream(file);
                FileOutputStream fos = new FileOutputStream(f);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        }
        finally {
            jar.close();
        }
    }
    
    public static File getJarFile(String path) {
        File jarFile = null;

        if (path.indexOf("!") >= 0) {
            path = path.substring(0, path.indexOf("!"));

            if (path.startsWith("jar:")) {
                path = path.substring("jar:".length());
            }

            if (path.startsWith("file:")) {
                path = path.substring("file:".length());
            }

            jarFile = new File(path);
        }

        return jarFile;
    }

    public static File getJarFile(File file) {
        File jarFile = null;
        
        try {
            String path = "";
            path = URLDecoder.decode(file.getPath(), UTF_8);
            
            jarFile = getJarFile(path);
        }
        catch (UnsupportedEncodingException ex) {
            
        }
        
        return jarFile;
    }
    
    public static File getJarFile(Class<?> clazz) {
        File jarFile = null;
        
        String resourcePath = getResourcePath(clazz);
        
        URL resource = clazz.getResource(resourcePath);
        
        if (resource != null) {
            String path = "";
            
            try {
                path = URLDecoder.decode(resource.getPath(), UTF_8);

                jarFile = getJarFile(path);
            }
            catch (UnsupportedEncodingException ex) {
                
            }
        }
        
        return jarFile;
    }
    
    public static boolean isJarPath(String path) {
        return path.indexOf("jar:file:") >= 0 && path.indexOf("!") >= 0;
    }
    
    public static boolean isInJar(Class<?> clazz) {
        File jarFile = getJarFile(clazz);
        
        return jarFile != null;
    }
    
    public static String getInternalPath(String path) {
        return path.substring(path.lastIndexOf("!") + 1);
    }
    
    public static String getResourcePath(Class<?> clazz) {
        return "/" + clazz.getName().replace('.', '/') + ".class";
    }
    
    public static String getResourcePath(Class<?> clazz, String resourcePath) {
        URL resource = getResource(clazz, resourcePath);
        
        if (resource != null) {
            return resource.toString();
        }
        else if (isInJar(clazz)) {
            File jarFile = getJarFile(clazz);
            
            return "jar:file:/" + jarFile + "!" + resourcePath;
        }
        else {
            return resourcePath;
        }
    }
    
    public static URL getResource(Class<?> clazz, String name) {
        ClassLoader classLoader = clazz.getClassLoader();
        
        return getResource(classLoader, name);
    }

    public static URL getResource(ClassLoader classLoader, String name) {
        URL resource = null;

        if (classLoader != null) {
            classLoader.getResource(name);
        }

        return resource;
    }

    public static URL extractResource(File jarFile, String target) {
        URL resource = null;
        
        try {
            JarFile jar = new JarFile(jarFile);
            
            try {
                ZipEntry entry = jar.getEntry(target);

                String name = entry.getName();

                System.out.println("resource.name=" + name);

                InputStream in = jar.getInputStream(entry);

                byte[] data = StreamUtils.stream(in);

                System.out.println("resource.data=");
                System.out.println(data);
            }
            finally {
                jar.close();
            }
        }
        catch (IOException ex) {
            
        }
        
        return resource;
    }
}
