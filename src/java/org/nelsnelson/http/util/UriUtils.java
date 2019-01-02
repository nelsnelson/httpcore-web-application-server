package org.nelsnelson.http.util;

public class UriUtils {
    private UriUtils() {
        
    }
    
    public static String getPath(String uri) {
        String path = uri;
        
        int lastSlashPos = path.lastIndexOf("/");
        
        if (lastSlashPos >= 0) {
            path = path.substring(lastSlashPos);
            
            int questionMarkPos = path.indexOf("?");
            
            if (questionMarkPos >= 0) {
                path = path.substring(0, questionMarkPos);
            }
        }
        
        return path;
    }
}
