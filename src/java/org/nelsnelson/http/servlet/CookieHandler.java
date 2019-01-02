package org.nelsnelson.http.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.Header;
import org.nelsnelson.http.HttpHeaders;
import org.nelsnelson.org.apache.commons.httpclient.Cookie;

public class CookieHandler {
    public static final String VERSION = "$Version";
    public static final String DOMAIN = "$Domain";
    public static final String PATH = "$Path";
    
    private static CookieHandler instance = null;
    
    private Header[] headers = null;
    
    public CookieHandler() {
        this(null);
    }
    
    public CookieHandler(Header[] headers) {
        this.headers = headers;
    }
    
    public Cookie[] getCookies() {
        return getCookiesFromHeaders(headers);
    }
    
    public static CookieHandler getInstance() {
        if (instance == null) {
            return new CookieHandler();
        }
        
        return instance;
    }
    
    public Cookie[] getCookiesFromHeaders(Header[] headers) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        
        if (headers != null) {
            for (Header header : headers) {
                String name = header.getName();
                String value = header.getValue();
                
                if (name.equalsIgnoreCase(HttpHeaders.COOKIE) || 
                    name.equalsIgnoreCase(HttpHeaders.COOKIE+"2"))
                {
                    cookies.addAll(parseCookieHeaderValue(value));
                }
            }
        }
        
        return (Cookie[]) cookies.toArray(new Cookie[cookies.size()]);
    }
    
    private List parseCookieHeaderValue(String headerValue) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        
        Map parameters = getCookieParameterMap(headerValue);
        
        String domain = null;
        String path = null;
        int version = 0;
        
        // Preliminary important keys
        if (parameters.containsKey(DOMAIN)) {
            domain = (String) parameters.remove(DOMAIN);
        }
        if (parameters.containsKey(PATH)) {
            path = (String) parameters.remove(PATH);
        }
        if (parameters.containsKey(VERSION)) {
            String versionParameter = (String) parameters.get(VERSION);
            try {
                version = Integer.parseInt(versionParameter);
            }
            catch (NumberFormatException ex) {
                version = 0;
            }
        }
        
        // All others must be cookies?
        for (Iterator i = parameters.keySet().iterator(); i.hasNext(); ) {
            String name = (String) i.next();
            String value = (String) parameters.get(name);
            Cookie cookie = new Cookie(domain, name, value);
            cookie.setVersion(version);
            //cookie.setSecure(isSecure());
            if (path != null) cookie.setPath(path);
            cookies.add(cookie);
        }
        
        return cookies;
    }

    private Map getCookieParameterMap(String headerValue) {
        Map parameters = new HashMap();
        
        String[] params = headerValue.split(";");
        
        for (int i = 0; i < params.length; i++) {
            String parameter = params[i];
            
            if (parameter.indexOf("=") >= 0) {
                String[] pair = parameter.split("=");
                String name = pair[0];
                String value = pair[1];
                if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                parameters.put(name, value);
            }
        }
        
        return parameters;
    }

    public String formatCookie(javax.servlet.http.Cookie cookie) {
        return ((org.nelsnelson.org.apache.commons.httpclient.Cookie) cookie).toExternalForm();
    }
}
