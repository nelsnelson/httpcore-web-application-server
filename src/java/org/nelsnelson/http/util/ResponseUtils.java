/*
 * ResponseUtils.java
 *
 * Created on December 13, 2006, 6:15 PM
 */

package org.nelsnelson.http.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.http.header.ContentType;
import org.nelsnelson.http.servlet.DefaultHttpServletResponse;
import org.nelsnelson.toolbox.util.StreamUtils;

/**
 *
 * @author nelsnelson
 */
public class ResponseUtils {
    /** Creates a new instance of ResponseUtils */
    public ResponseUtils() {
        
    }
    
    public static void saveFile(URLConnection connect) {
        if (connect == null) {
            return;
        }
        
        saveFile(connect, getFile(connect));
    }
    
    public static void saveFile(URLConnection connect, File file) {
        if (connect == null) {
            return;
        }
        
        try {
            InputStream in = connect.getInputStream();
            
            StreamUtils.stream(in, file);
            
            in.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static File getFile(URLConnection connect) {
        if (connect == null) {
            return null;
        }
        
        ContentType contentType = new ContentType(connect.getContentType());
        
        return new File(contentType.getFirstParameter("name"));
    }
    
    public static HttpServletResponse getServletResponse(ClassicHttpResponse response, 
        HttpContext context, ServletRequest servletRequest, 
        ServletContext servletContext) 
    {
        return new DefaultHttpServletResponse(response, context, servletRequest, servletContext);
    }
    

    static class DefaultHttpEntityFactory {
        private static DefaultHttpEntityFactory instance = null;
        
        private DefaultHttpEntityFactory() {
            
        }
        
        public static DefaultHttpEntityFactory getInstance() {
            if (instance == null) {
                instance = new DefaultHttpEntityFactory();
            }
            
            return instance;
        }
        
        public static HttpEntity createHttpEntity() {
            return new BasicHttpEntity();
        }
    }
}
