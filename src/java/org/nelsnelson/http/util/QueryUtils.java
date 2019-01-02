/*
 * QueryUtils.java
 *
 * Created on December 11, 2006, 5:14 PM
 */

package org.nelsnelson.http.util;

import org.apache.hc.core5.http.HttpRequest;

/**
 *
 * @author nelsnelson
 */
public class QueryUtils {
    /** Creates a new instance of QueryUtils */
    public QueryUtils() {
        
    }

    public static String getQueryString(HttpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("HttpRequest parameter may not be null");
        }
        
        String queryString = "";
        
        String requestUri = request.getRequestUri();
        
        if (requestUri != null) {
            queryString = getQuery(requestUri);
        }
        
        return queryString;
    }
    
    public static String getQuery(String uri) {
        String queryString = "";
        
        int questionMarkPos = uri.indexOf("?");
        if (questionMarkPos >= 0) {
            queryString = uri.substring(questionMarkPos + 1);
        }
        
        return queryString;
    }
}
