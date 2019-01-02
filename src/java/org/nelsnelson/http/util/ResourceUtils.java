/*
 * NetUtilities.java
 *
 * Created on August 2, 2006, 2:03 PM
 */

package org.nelsnelson.http.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.nelsnelson.toolbox.util.StreamUtils;

/**
 *
 * @author nelsnelson
 */
public class ResourceUtils {
    /** Creates a new instance of NetUtils */
    public ResourceUtils() {
        //stub
    }
    
    public static File saveResourceLocally(String url) {
        File destination = null;
        
        try {
            URL source = new URL(url);
            
            destination = new File(source.getFile());
            
            saveResourceLocally(source, destination);
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
            destination = null;
        }
        
        return destination;
    }
    
    public static File saveResourceLocally(String url, File destination) {
        try {
            URL source = new URL(url);
            File file = new File(source.getFile());
            
            destination.mkdirs();
            destination = new File(destination, file.getName());
            
            saveResourceLocally(source, destination);
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
            destination = null;
        }
        
        return destination;
    }
    
    public static File saveResourceLocally(URL source,
        File destination) 
    {
        StreamUtils.pipe(source, destination);
        
        return destination;
    }
}
