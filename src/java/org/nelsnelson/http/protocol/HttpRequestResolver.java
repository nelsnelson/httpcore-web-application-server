package org.nelsnelson.http.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

public class HttpRequestResolver implements HttpRequestHandler {
    private final Map<String, HttpRequestHandler> handlerMap;
    private final List<String> patterns;
    
    public HttpRequestResolver() {
        super();
        this.handlerMap = new HashMap<String, HttpRequestHandler>();
        this.patterns = new ArrayList<String>();
    }
    
    public void register(final String pattern, final HttpRequestHandler handler) {
        if (pattern == null) {
            throw new IllegalArgumentException("URI request pattern may not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("HTTP request handler may not be null");
        }
        this.handlerMap.put(pattern, handler);
        this.patterns.add(0, pattern);
    }
    
    public void unregister(final String pattern) {
        if (pattern == null) {
            return;
        }
        this.handlerMap.remove(pattern);
        this.patterns.remove(pattern);
    }
    
    public HttpRequestHandler lookup(String requestURI) {
        if (requestURI == null) {
            throw new IllegalArgumentException("Request URI may not be null");
        }
        //Strip away the query part part if found
        int index = requestURI.indexOf("?");
        if (index != -1) {
            requestURI = requestURI.substring(0, index);
        }
        
        // direct match?
        Object handler = this.handlerMap.get(requestURI);
        if (handler == null) {
            // pattern match?
            String bestMatch = null;
            for (String pattern : this.patterns) {
                if (matchUriRequestPattern(pattern, requestURI)) {
                    // we have a match. is it any better?
                    if (bestMatch == null 
                            || (bestMatch.length() < pattern.length())
                            || (bestMatch.length() == pattern.length() && pattern.endsWith("*"))) {
                        handler = this.handlerMap.get(pattern);
                        bestMatch = pattern;
                    }
                }
            }
        }
        return (HttpRequestHandler) handler;
    }

    protected boolean matchUriRequestPattern(final String pattern, final String requestUri) {
        if (pattern.equals("*")) {
            return true;
        } else {
            return 
            (pattern.endsWith("*") && requestUri.startsWith(pattern.substring(0, pattern.length() - 1))) ||
            (pattern.startsWith("*") && requestUri.endsWith(pattern.substring(1, pattern.length())));
        }
    }

    @Override
    public void handle(ClassicHttpRequest arg0, ClassicHttpResponse arg1, HttpContext arg2)
            throws HttpException, IOException {
        
    }
}
