package org.nelsnelson.toolbox.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hc.core5.http.ContentType;

/**
 * 
 * @author nelsnelson
 *
 */
public class FileUtils {
    // Work out the filename extension.  If there isn't one, keep
    // it as the empty string ("").
    public static String getExtension(File file) {
        return getExtension(file.getName());
    }
    
    public static String getExtension(String filename) {
        String extension = "";
        int dotPos = filename.lastIndexOf(".");
        if (dotPos >= 0) {
            extension = filename.substring(dotPos);
        }
        return extension.toLowerCase();
    }
    
    public static ContentType getContentType(File file) {
        return getContentType(file.getName());
    }
    
    public static ContentType getContentType(String filename) {
        String mimeType = ContentType.DEFAULT_TEXT.getMimeType();
        
        Map<String,String> mimeTypes = getMimeTypes();
        String extension = FileUtils.getExtension(filename);
        
        if (mimeTypes.containsKey(extension)) {
            mimeType = mimeTypes.get(extension);
        }
        
        ContentType contentType = ContentType.create(mimeType);
        
        return contentType;
    }
    
    // TODO Get these from a mimeType manager that is linked to the 
    // generic application properties
    public static Map<String,String> getMimeTypes() {
        return mimeTypes;
    }

    public static final String DEFAULT_MIME_TYPE = "default.mime.type";
    
    public static final Map<String,String> mimeTypes = new LinkedHashMap<String,String>();
    
    static {
        mimeTypes.put(".gif", "image/gif");
        mimeTypes.put(".jpg", "image/jpeg");
        mimeTypes.put(".jpeg", "image/jpeg");
        mimeTypes.put(".png", "image/png");
        mimeTypes.put(".html", "text/html");
        mimeTypes.put(".htm", "text/html");
        mimeTypes.put(".css", "text/css");
        mimeTypes.put(".txt", "text/plain");
        mimeTypes.put(DEFAULT_MIME_TYPE, "text/plain");
    }
}
